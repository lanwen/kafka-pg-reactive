package ru.lanwen.kpr;

import java.util.Objects;

public class Application {

    public static final String KAFKA_BOOTSTRAP_SERVERS_PROPERTY = "consumer.kafka.bootstrap.servers";
    public static final String CONSUMER_TOPIC_NAME_PROPERTY = "consumer.topic.name";
    public static final String CONSUMER_PG_CONNECTION_PROPERTY = "consumer.pg.connection";

    public static void main(String[] args) {
        var servers = Objects.requireNonNull(System.getProperty(KAFKA_BOOTSTRAP_SERVERS_PROPERTY), "property " + KAFKA_BOOTSTRAP_SERVERS_PROPERTY + " should be defined");
        var pgConnectionString = Objects.requireNonNull(System.getProperty(CONSUMER_PG_CONNECTION_PROPERTY), "property " + CONSUMER_PG_CONNECTION_PROPERTY + " should be defined");
        var topic = System.getProperty(CONSUMER_TOPIC_NAME_PROPERTY, "metrics");

        var pg = new PostgresWriter(pgConnectionString);
        var consumer = new MetricsConsumer(servers);

        pg.prepareTable()
                .then(consumer.consumeMessages(topic, pg))
                .block();
    }
}
