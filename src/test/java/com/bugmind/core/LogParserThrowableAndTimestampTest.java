package com.bugmind.core;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Regression + stability tests:
 *  - F2P: Error/Throwable extraction (used to be null)
 *  - P2P: millisecond and offset timestamps, and classic Exception
 */
public class LogParserThrowableAndTimestampTest {

    private final LogParser parser = new LogParser();

    // ---------- F2P: previously failed, now passes ----------
    @Test
    void parsesErrorThrowableTypes_previouslyNullNowExtracted() {
        String logs = """
            [2025-10-27 21:10:00] ERROR - OutOfMemoryError occurred in worker
            """;
        List<ParsedLog> result = parser.parseLogs(logs);
        assertEquals(1, result.size());
        ParsedLog p = result.get(0);
        assertEquals("ERROR", p.getLevel());
        assertEquals("OutOfMemoryError", p.getExceptionType()); // was null before fix
        assertTrue(p.getMessage().contains("worker"));
    }

    // ---------- P2P regression tests ----------
    @Test
    void parsesMillisecondTimestamp() {
        String logs = """
            [2025-10-27 21:10:00.123] ERROR - NullPointerException occurred
            """;
        List<ParsedLog> result = parser.parseLogs(logs);
        assertEquals(1, result.size());
        ParsedLog p = result.get(0);
        // normalized to seconds
        assertEquals("2025-10-27 21:10:00", p.getTimestamp());
        assertEquals("NullPointerException", p.getExceptionType());
    }

    @Test
    void parsesIsoWithOffset() {
        String logs = """
            [2025-10-27T21:10:00+05:30] WARN - Disk threshold close
            """;
        List<ParsedLog> result = parser.parseLogs(logs);
        assertEquals(1, result.size());
        ParsedLog p = result.get(0);
        assertEquals("WARN", p.getLevel());
        assertTrue(p.getMessage().contains("Disk threshold"));
    }

    @Test
    void stillParsesClassicException() {
        String logs = """
            [2025-10-27 21:10:00] ERROR - IllegalStateException at stage 2
            """;
        List<ParsedLog> result = parser.parseLogs(logs);
        assertEquals(1, result.size());
        ParsedLog p = result.get(0);
        assertEquals("IllegalStateException", p.getExceptionType());
        assertTrue(p.getMessage().contains("stage 2"));
    }
}
