package ru.lanwen.kpr;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MetricsReader {
    public record Metric(String name, float value, String type) {
    }

    public static List<Metric> parse(String batch) {
        return Arrays.stream(batch.strip().split("\n"))
                .map(line -> line.split("\\|"))
                .filter(parts -> parts.length == 4)
                .map(parts -> switch (parts[0]) {
                    // parsing float could obviously fail, but here we assume everything under control
                    // and for simplicity don't handle
                    case "v1" -> new Metric(parts[1], Float.parseFloat(parts[2]), parts[3]);
                    default -> throw new UnsupportedMetricVersionException();
                })
                .collect(Collectors.toList());
    }

    public static class UnsupportedMetricVersionException extends IllegalArgumentException {
    }
}
