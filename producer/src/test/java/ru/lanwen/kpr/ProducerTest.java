package ru.lanwen.kpr;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class ProducerTest {

    public static KafkaContainer kafka;

    static {
        kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:5.4.3"));
        kafka.start();
    }

    @Test
    void shouldPublishToKafka() throws UnknownHostException {
        var published = new MetricsProducer(kafka.getBootstrapServers())
                .publish(
                        "metrics",
                        InetAddress.getLocalHost().getHostName(),
                        Application
                                .metrics(
                                        Duration.ofMillis(100),
                                        new MetricsProvider()
                                )
                                .take(30)
                )
                .map(result -> String.format("%d:%d at %d", result.recordMetadata().partition(), result.recordMetadata().offset(), result.recordMetadata().timestamp()))
                .log()
                .collectList()
                .block(Duration.ofSeconds(10));

        assertThat(published).isNotEmpty();

    }
}
