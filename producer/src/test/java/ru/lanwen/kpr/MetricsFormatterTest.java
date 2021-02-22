package ru.lanwen.kpr;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MetricsFormatterTest {

    @Test
    void shouldFormatMetric() {
        assertThat(MetricsFormatter.format(new MetricsProvider.Metric("money.spent", 9.1f, MetricsProvider.TYPE_GAUGE)))
                .isEqualTo("v1|money.spent|9.1|g");
    }
}