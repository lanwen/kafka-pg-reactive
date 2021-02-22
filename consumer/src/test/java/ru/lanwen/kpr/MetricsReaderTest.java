package ru.lanwen.kpr;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MetricsReaderTest {

    @Test
    void shoudParseMetrics() {
        var metrics = """
                v1|mem.free|2.42512496E8|g
                v1|mem.total|2.70532608E8|g
                v1|mem.used|2.8020112E7|g
                """;
        assertThat(MetricsReader.parse(metrics)).hasSize(3)
                .contains(
                        new MetricsReader.Metric("mem.free", 2.42512496E8f, "g"),
                        new MetricsReader.Metric("mem.total", 2.70532608E8f, "g"),
                        new MetricsReader.Metric("mem.used", 2.8020112E7f, "g")
                );
    }

    @Test
    void couldFailOnVersion() {
        var metrics = """
                v2|mem.free|2.42512496E8|g
                v1|mem.total|2.70532608E8|g
                v1|mem.used|2.8020112E7|g
                """;
        assertThatThrownBy(() -> MetricsReader.parse(metrics)).isInstanceOf(MetricsReader.UnsupportedMetricVersionException.class);
    }

    @Test
    void couldFailOnParse() {
        var metrics = """
                v1|mem.free|2.42512496E8|g
                v1|mem.total|unknown|g
                v1|mem.used|2.8020112E7|g
                """;
        assertThatThrownBy(() -> MetricsReader.parse(metrics)).isInstanceOf(NumberFormatException.class);
    }
}