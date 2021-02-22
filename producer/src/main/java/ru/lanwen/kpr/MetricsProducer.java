package ru.lanwen.kpr;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import reactor.core.publisher.Flux;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;
import reactor.kafka.sender.SenderRecord;
import reactor.kafka.sender.SenderResult;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class MetricsProducer {

    private KafkaSender sender;

    public MetricsProducer(String bootstrapServers) {
        Map<String, Object> props = new HashMap<>(Map.of(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
                ProducerConfig.CLIENT_ID_CONFIG, "metrics-producer",
                ProducerConfig.ACKS_CONFIG, "1",
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class
        ));
        sender = KafkaSender.create(SenderOptions.create(props));
    }

    public Flux<SenderResult<String>> publish(String topic, String key, Flux<String> content) {
        return sender.send(content.map(it -> SenderRecord.create(new ProducerRecord<>(topic, key, it), Instant.now().toString())));
    }
}
