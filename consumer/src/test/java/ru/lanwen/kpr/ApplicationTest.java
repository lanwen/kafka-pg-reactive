package ru.lanwen.kpr;

import io.r2dbc.spi.ConnectionFactories;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.utility.DockerImageName;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;
import reactor.kafka.sender.SenderRecord;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationTest {

    public static KafkaContainer kafka;
    public static PostgreSQLContainer<?> pg;

    static {
        kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:5.4.3"));
        pg = new PostgreSQLContainer<>(DockerImageName.parse("postgres:12.6"));
        Startables.deepStart(kafka, pg).join();
    }

    @Test
    void shouldWriteStuffToPostgres() {
        var conn = String.format(
                "r2dbc:postgresql://%s:%s@%s:%d/%s",
                pg.getUsername(),
                pg.getPassword(),
                pg.getContainerIpAddress(),
                pg.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT),
                pg.getDatabaseName()
        );
        var writer = new PostgresWriter(conn);
        writer.prepareTable().block();

        var topic = "metrics";

        new MetricsConsumer(kafka.getBootstrapServers())
                .consumeMessages(topic, writer)
                .takeUntilOther(
                        sender(kafka.getBootstrapServers())
                                .send(Mono.just(of(
                                        topic, """
                                                v1|mem.free|2.42512496E8|g
                                                v1|mem.total|2.70532608E8|g
                                                v1|mem.used|2.8020112E7|g
                                                """
                                )))
                                .then(Mono.delay(Duration.ofSeconds(2)))
                )
                .block();

        var written = Mono.from(ConnectionFactories.get(conn).create()).flatMapMany(c -> c.createStatement("SELECT * FROM metrics").execute())
                .flatMap(result -> result.map((row, meta) -> String.format("%s:%f", row.get(2, String.class), row.get(3, Float.class))))
                .log("result")
                .collectList()
                .block();

        assertThat(written).hasSize(3).contains("mem.free:242512496,000000", "mem.total:270532608,000000", "mem.used:28020112,000000");
    }

    static KafkaSender sender(String servers) {
        Map<String, Object> props = new HashMap<>(Map.of(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, servers,
                ProducerConfig.CLIENT_ID_CONFIG, "metrics-producer",
                ProducerConfig.ACKS_CONFIG, "1",
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class
        ));
        return KafkaSender.create(SenderOptions.create(props));
    }

    static SenderRecord of(String topic, String content) {
        try {
            return SenderRecord.create(new ProducerRecord<>(topic, InetAddress.getLocalHost().getHostName(), content), Instant.now().toString());
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }
}