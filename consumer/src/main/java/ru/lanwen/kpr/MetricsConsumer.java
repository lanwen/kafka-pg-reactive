package ru.lanwen.kpr;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MetricsConsumer {
    private static final Logger LOG = LoggerFactory.getLogger(MetricsConsumer.class);

    ReceiverOptions<String, String> receiverOptions;

    public MetricsConsumer(String bootstrapServers) {
        Map<String, Object> props = new HashMap<>(Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
                ConsumerConfig.CLIENT_ID_CONFIG, "metrics-consumer",
                ConsumerConfig.GROUP_ID_CONFIG, "metrics",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"
        ));
        receiverOptions = ReceiverOptions.create(props);
    }

    public Mono<Void> consumeMessages(String topic, PostgresWriter writer) { // shouldn't be writer directly, but a better abstraction actually
        ReceiverOptions<String, String> options = receiverOptions.subscription(Collections.singleton(topic))
                .addAssignListener(partitions -> LOG.debug("partitions assigned {}", partitions))
                .addRevokeListener(partitions -> LOG.debug("partitions revoked {}", partitions));

        return KafkaReceiver.create(options).receive()
                .delayUntil(record -> {
                    var metrics = MetricsReader.parse(record.value());
                    var host = record.key();
                    var ts = record.timestamp();

                    return writer.writeMetrics(host, ts, metrics);
                })
                .doOnNext(record -> record.receiverOffset().acknowledge())
                .sample(Duration.ofSeconds(5)) // explicitly commit once per 5 sec
                .delayUntil(record -> record.receiverOffset().commit())
                .then();
    }
}
