package com.bugmind.core;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Objects;

/**
 * Computes summary metrics from a collection of parsed logs.
 */
public class LogMetricsAnalyzer {

    /**
     * Holds computed metrics for quick reporting.
     */
    public static class LogMetrics {
        private final int totalCount;
        private final int errorCount;
        private final double errorPercentage;
        private final double avgMessageLength;
        private final String firstTimestamp;
        private final String lastTimestamp;

        public LogMetrics(int totalCount, int errorCount, double errorPercentage,
                          double avgMessageLength, String firstTimestamp, String lastTimestamp) {
            this.totalCount = totalCount;
            this.errorCount = errorCount;
            this.errorPercentage = errorPercentage;
            this.avgMessageLength = avgMessageLength;
            this.firstTimestamp = firstTimestamp;
            this.lastTimestamp = lastTimestamp;
        }

        @Override
        public String toString() {
            return String.format(
                    "Total=%d | Errors=%d (%.2f%%) | AvgMsgLen=%.1f | Range=%s → %s",
                    totalCount, errorCount, errorPercentage, avgMessageLength,
                    firstTimestamp, lastTimestamp
            );
        }

        // Getters
        public int getTotalCount() { return totalCount; }
        public int getErrorCount() { return errorCount; }
        public double getErrorPercentage() { return errorPercentage; }
        public double getAvgMessageLength() { return avgMessageLength; }
        public String getFirstTimestamp() { return firstTimestamp; }
        public String getLastTimestamp() { return lastTimestamp; }
    }

    /**
     * Computes high-level metrics for a set of logs.
     */
    public LogMetrics computeMetrics(List<ParsedLog> logs) {
        if (logs == null || logs.isEmpty()) {
            return new LogMetrics(0, 0, 0, 0, "-", "-");
        }

        int total = logs.size();
        int errors = (int) logs.stream()
                .filter(l -> "ERROR".equalsIgnoreCase(l.getLevel()))
                .count();

        double avgLength = logs.stream()
                .map(ParsedLog::getMessage)
                .filter(Objects::nonNull)
                .mapToInt(String::length)
                .average().orElse(0);

        double errorPercent = (total == 0) ? 0 : (errors * 100.0 / total);

        List<String> timestamps = logs.stream()
                .map(ParsedLog::getTimestamp)
                .filter(Objects::nonNull)
                .sorted()
                .toList();

        String first = timestamps.isEmpty() ? "-" : timestamps.get(0);
        String last = timestamps.isEmpty() ? "-" : timestamps.get(timestamps.size() - 1);

        return new LogMetrics(total, errors, errorPercent, avgLength, first, last);
    }
}
