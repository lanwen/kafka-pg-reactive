package ru.lanwen.kpr;


import reactor.core.publisher.Flux;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.Objects;
import java.util.stream.Collectors;

public class Application {

    public static final String KAFKA_BOOTSTRAP_SERVERS_PROPERTY = "producer.kafka.bootstrap.servers";
    public static final String PRODUCER_KAFKA_SECURITY_PROTOCOL_PROPERTY = "producer.kafka.security.protocol";
    public static final String PRODUCER_KAFKA_KEY_PWD_PROPERTY = "producer.kafka.key.pwd";
    public static final String PRODUCER_KAFKA_TOPIC_NAME_PROPERTY = "producer.kafka.topic.name";
    public static final String PRODUCER_INTERVAL_SECONDS_PROPERTY = "producer.interval.seconds";

    public static void main(String[] args) throws UnknownHostException {
        var topic = conf(PRODUCER_KAFKA_TOPIC_NAME_PROPERTY, "metrics");
        var interval = Duration.ofSeconds(Integer.parseInt(conf(PRODUCER_INTERVAL_SECONDS_PROPERTY, "1")));

        var producer = new MetricsProducer(
                conf(KAFKA_BOOTSTRAP_SERVERS_PROPERTY, null),
                conf(PRODUCER_KAFKA_SECURITY_PROTOCOL_PROPERTY, "plain"),
                conf(PRODUCER_KAFKA_KEY_PWD_PROPERTY, "")
        );
        var metrics = new MetricsProvider();

        producer
                .publish(topic, InetAddress.getLocalHost().getHostName(), metrics(interval, metrics))
                .blockLast();
    }

    static Flux<String> metrics(Duration interval, MetricsProvider metrics) {
        return Flux.interval(interval)
                .concatMap(i -> Flux
                        .just(
                                metrics.freeMem(),
                                metrics.totalMem(),
                                metrics.usedMem()
                        )
                        .map(MetricsFormatter::format)
                        .collect(Collectors.joining("\n"))
                )
                .log("metrics");
    }

    static String conf(String prop, String defaultValue) {
        var env = prop.replace(".", "_");

        return defaultValue == null
                ? Objects
                .requireNonNull(
                        System.getProperty(prop, System.getenv(env)),
                        "property " + prop + " should be defined"
                )
                : System.getProperty(prop, System.getenv().getOrDefault(env, defaultValue));
    }
}