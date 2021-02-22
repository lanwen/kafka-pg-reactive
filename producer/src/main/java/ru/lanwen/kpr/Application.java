package ru.lanwen.kpr;


import reactor.core.publisher.Flux;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.Objects;
import java.util.stream.Collectors;

public class Application {

    public static final String KAFKA_BOOTSTRAP_SERVERS_PROPERTY = "producer.kafka.bootstrap.servers";
    public static final String PRODUCER_TOPIC_NAME_PROPERTY = "producer.topic.name";
    public static final String PRODUCER_INTERVAL_SECONDS_PROPERTY = "producer.interval.seconds";

    public static void main(String[] args) throws UnknownHostException {
        var servers = Objects.requireNonNull(System.getProperty(KAFKA_BOOTSTRAP_SERVERS_PROPERTY), "property " + KAFKA_BOOTSTRAP_SERVERS_PROPERTY + " should be defined");
        var topic = System.getProperty(PRODUCER_TOPIC_NAME_PROPERTY, "metrics");
        var interval = Duration.ofSeconds(Integer.parseInt(System.getProperty(PRODUCER_INTERVAL_SECONDS_PROPERTY, "1")));

        var producer = new MetricsProducer(servers);
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
}