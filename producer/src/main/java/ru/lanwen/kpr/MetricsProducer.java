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

    public MetricsProducer(String bootstrapServers, String secProtocol, String pwd) {
        Map<String, Object> props = new HashMap<>(Map.of(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
                ProducerConfig.CLIENT_ID_CONFIG, "metrics-producer",
                ProducerConfig.ACKS_CONFIG, "1",
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class
        ));

        if ("ssl".equals(secProtocol)) {
            props.putAll(Map.of(
                    "security.protocol", "SSL",
                    "ssl.endpoint.identification.algorithm", "",
                    "ssl.truststore.location", "./certs/kafka/client.truststore.jks",
                    "ssl.truststore.password", pwd,
                    "ssl.keystore.type", "PKCS12",
                    "ssl.keystore.location", "./certs/kafka/client.keystore.p12",
                    "ssl.keystore.password", pwd,
                    "ssl.key.password", pwd
            ));
        }
        sender = KafkaSender.create(SenderOptions.create(props));
    }

    public Flux<SenderResult<String>> publish(String topic, String key, Flux<String> content) {
        return sender.send(content.map(it -> SenderRecord.create(new ProducerRecord<>(topic, key, it), Instant.now().toString())));
    }
}
