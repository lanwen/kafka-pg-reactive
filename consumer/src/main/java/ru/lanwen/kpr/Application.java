package ru.lanwen.kpr;

import java.util.Objects;

public class Application {

    public static final String KAFKA_BOOTSTRAP_SERVERS_PROPERTY = "consumer.kafka.bootstrap.servers";
    public static final String CONSUMER_KAFKA_SECURITY_PROTOCOL_PROPERTY = "consumer.kafka.security.protocol";
    public static final String CONSUMER_KAFKA_KEY_PWD_PROPERTY = "consumer.kafka.key.pwd";
    public static final String CONSUMER_KAFKA_TOPIC_NAME_PROPERTY = "consumer.kafka.topic.name";
    public static final String CONSUMER_PG_CONNECTION_PROPERTY = "consumer.pg.connection";

    public static void main(String[] args) {
        var pgConnectionString = conf(CONSUMER_PG_CONNECTION_PROPERTY, null);
        var topic = conf(CONSUMER_KAFKA_TOPIC_NAME_PROPERTY, "metrics");

        var pg = new PostgresWriter(pgConnectionString);
        var consumer = new MetricsConsumer(
                conf(KAFKA_BOOTSTRAP_SERVERS_PROPERTY, null),
                conf(CONSUMER_KAFKA_SECURITY_PROTOCOL_PROPERTY, "plain"),
                conf(CONSUMER_KAFKA_KEY_PWD_PROPERTY, "")
        );

        pg.prepareTable()
                .then(consumer.consumeMessages(topic, pg))
                .block();
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
