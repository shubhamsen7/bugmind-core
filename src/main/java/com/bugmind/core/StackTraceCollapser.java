package com.bugmind.core;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility that collapses long stack traces and extracts the deepest root cause.
 * <p>
 * Compatible with LogParser: used to post-process raw message blocks.
 */
public class StackTraceCollapser {

    private static final Pattern CAUSED_BY_PATTERN =
            Pattern.compile("Caused by:\\s+([A-Za-z0-9_$.]+(?:Exception|Error|Throwable))");

    // Accepts lines starting with spaces or tabs before "at ..."
    private static final Pattern STACK_LINE_PATTERN =
            Pattern.compile("^\\s*(at\\s+.+)$");

    /** Immutable result model */
    public static class Result {
        private final String collapsedMessage;
        private final String rootException;

        public Result(String collapsedMessage, String rootException) {
            this.collapsedMessage = collapsedMessage;
            this.rootException = rootException;
        }

        public String collapsedMessage() {
            return collapsedMessage;
        }

        public String rootException() {
            return rootException;
        }
    }

    /**
     * Collapses stack frames beyond the given cap and finds the deepest "Caused by" throwable.
     *
     * @param message  raw log block (possibly multi-line)
     * @param maxLines maximum number of stack frames to retain
     */
    public static Result collapseAndExtract(String message, int maxLines) {
        if (message == null || message.isBlank()) {
            return new Result("", null);
        }

        String[] lines = message.split("\\r?\\n");
        StringBuilder collapsed = new StringBuilder();
        String lastCause = null;
        int frameCount = 0;
        int totalFrames = 0;

        for (String line : lines) {
            Matcher cb = CAUSED_BY_PATTERN.matcher(line);
            if (cb.find()) {
                lastCause = cb.group(1);
            }

            Matcher frame = STACK_LINE_PATTERN.matcher(line);
            if (frame.find()) {
                totalFrames++;
                frameCount++;
                if (frameCount <= maxLines) {
                    collapsed.append(line.trim()).append(System.lineSeparator());
                }
                // Skip appending beyond maxLines
            } else {
                collapsed.append(line.trim()).append(System.lineSeparator());
            }
        }

        if (totalFrames > maxLines) {
            collapsed.append("… (").append(totalFrames - maxLines).append(" more)");
        }

        return new Result(collapsed.toString().trim(), lastCause);
    }

    /** Manual smoke test */
    public static void main(String[] args) {
        String msg = """
            Operation failed
                at a.A.one(A.java:1)
                at a.A.two(A.java:2)
                at a.A.three(A.java:3)
                at a.A.four(A.java:4)
                at a.A.five(A.java:5)
            Caused by: java.io.IOException: fail
                at x.Y.z(Y.java:9)
            Caused by: java.lang.IllegalStateException: bad
                at b.C.main(C.java:42)
            """;
        Result r = collapseAndExtract(msg, 3);
        System.out.println("Root: " + r.rootException());
        System.out.println("Collapsed:\n" + r.collapsedMessage());
    }
}
