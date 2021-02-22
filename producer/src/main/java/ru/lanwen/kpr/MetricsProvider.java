package ru.lanwen.kpr;

public class MetricsProvider {

    public static final String TYPE_GAUGE = "g";

    public record Metric(String name, float value, String type) {
    }

    public Metric usedMem() {
        return new Metric("mem.used", Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory(), TYPE_GAUGE);
    }

    public Metric freeMem() {
        return new Metric("mem.free", Runtime.getRuntime().freeMemory(), TYPE_GAUGE);
    }

    public Metric totalMem() {
        return new Metric("mem.total", Runtime.getRuntime().totalMemory(), TYPE_GAUGE);
    }
}
