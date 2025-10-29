package com.bugmind.core;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.OptionalDouble;

/**
 * Computes statistics and health metrics from parsed logs.
 */
public class LogMetricsAnalyzer {

    public static class LogMetrics {
        private final int totalLogs;
        private final int errorCount;
        private final double errorPercentage;
        private final double avgMessageLength;
        private final String firstTimestamp;
        private final String lastTimestamp;

        public LogMetrics(int totalLogs, int errorCount, double errorPercentage,
                          double avgMessageLength, String firstTimestamp, String lastTimestamp) {
            this.totalLogs = totalLogs;
            this.errorCount = errorCount;
            this.errorPercentage = errorPercentage;
            this.avgMessageLength = avgMessageLength;
            this.firstTimestamp = firstTimestamp;
            this.lastTimestamp = lastTimestamp;
        }

        public int getTotalLogs() { return totalLogs; }
        public int getErrorCount() { return errorCount; }
        public double getErrorPercentage() { return errorPercentage; }
        public double getAvgMessageLength() { return avgMessageLength; }
        public String getFirstTimestamp() { return firstTimestamp; }
        public String getLastTimestamp() { return lastTimestamp; }

        @Override
        public String toString() {
            return String.format("Logs=%d, Errors=%d, Err%%=%.2f, AvgMsgLen=%.1f, Range=%s→%s",
                    totalLogs, errorCount, errorPercentage, avgMessageLength, firstTimestamp, lastTimestamp);
        }
    }

    /**
     * Computes metrics from parsed log entries.
     * @param logs Parsed logs
     * @return LogMetrics summary
     */
    public LogMetrics computeMetrics(List<ParsedLog> logs) {
        if (logs == null || logs.isEmpty()) {
            return new LogMetrics(0, 0, 0.0, 0.0, "-", "-");
        }

        int total = logs.size();
        int errors = (int) logs.stream()
                .filter(l -> l.getLevel() != null && l.getLevel().equalsIgnoreCase("ERROR"))
                .count();

        double avgLen = logs.stream()
                .filter(l -> l.getMessage() != null)
                .mapToInt(l -> l.getMessage().length())
                .average()
                .orElse(0.0);

        String first = logs.stream()
                .map(ParsedLog::getTimestamp)
                .filter(ts -> ts != null && !ts.isBlank())
                .min(Comparator.naturalOrder())
                .orElse("-");

        String last = logs.stream()
                .map(ParsedLog::getTimestamp)
                .filter(ts -> ts != null && !ts.isBlank())
                .max(Comparator.naturalOrder())
                .orElse("-");

        double errorPct = total == 0 ? 0.0 : (errors * 100.0 / total);
        return new LogMetrics(total, errors, errorPct, avgLen, first, last);
    }

    /** Converts a list of log strings into ParsedLogs using LogParser */
    public LogMetrics computeFromRaw(String rawLogs) {
        LogParser parser = new LogParser();
        List<ParsedLog> parsed = parser.parseLogs(rawLogs);
        return computeMetrics(parsed);
    }

    /** Quick manual test */
    public static void main(String[] args) {
        String logs = """
            [2025-10-28 09:00:00] INFO - Startup complete
            [2025-10-28 09:05:00] ERROR - NullPointerException in service
            [2025-10-28 09:10:00] WARN - Low disk space
            """;

        LogMetricsAnalyzer analyzer = new LogMetricsAnalyzer();
        System.out.println(analyzer.computeFromRaw(logs));
    }
}
