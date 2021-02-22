package ru.lanwen.kpr;


import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class MetricsFluxTest {
    @Test
    void shouldReturnMetrics() {
        var metrics = Application.metrics(Duration.ofMillis(10), new MetricsProvider())
                .log()
                .take(10)
                .collectList()
                .block(Duration.ofSeconds(1));

        assertThat(metrics).hasSize(10)
                .allSatisfy(metric -> assertThat(metric).containsPattern("v1\\|mem\\..*\\|\\d+.*\\|g"));
    }
}