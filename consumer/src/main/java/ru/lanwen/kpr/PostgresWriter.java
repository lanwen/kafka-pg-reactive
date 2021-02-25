package ru.lanwen.kpr;

import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactories;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public class PostgresWriter {

    Mono<? extends Connection> connection;

    public PostgresWriter(String connstring) {
        var connectionFactory = ConnectionFactories.get(connstring);
        connection = Mono.from(connectionFactory.create());
    }

    public Mono<Void> prepareTable() {
        return connection
                .flatMapMany(conn -> Flux
                        .from(conn
                                .createStatement("""
                                        CREATE TABLE IF NOT EXISTS metrics (
                                            timestamp bigint NOT NULL,
                                        	host TEXT NOT NULL,
                                         	metric TEXT NOT NULL,
                                          	value DOUBLE PRECISION NULL
                                        );
                                        """)
                                .execute()
                        )
                        .delayUntil(result -> result.getRowsUpdated())
                        .then(Mono.from(conn.close()))
                )
                .then();
    }

    public Mono<Void> writeMetrics(String host, long ts, List<MetricsReader.Metric> metrics) {
        return connection
                .flatMapMany(conn -> {
                    var statement = conn.createStatement("INSERT INTO metrics (timestamp, host, metric, value) VALUES ($1, $2, $3, $4)");

                    metrics.forEach(metric -> statement
                            .bind(0, ts)
                            .bind(1, host)
                            .bind(2, metric.name())
                            .bind(3, metric.value())
                            .add()
                    );

                    return Flux.from(statement.execute())
                            .delayUntil(result -> result.getRowsUpdated()) // to wait until result arrives
                            .then(Mono.from(conn.close()));
                })
                .then();
    }
}
