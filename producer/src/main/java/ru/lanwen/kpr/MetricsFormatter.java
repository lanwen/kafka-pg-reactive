package ru.lanwen.kpr;

public class MetricsFormatter {

    static String format(MetricsProvider.Metric metric) {
        return String.join("|", "v1", metric.name(), String.valueOf(metric.value()), metric.type());
    }
}
