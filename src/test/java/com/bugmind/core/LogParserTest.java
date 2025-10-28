package com.bugmind.core;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link LogParser}.
 */
public class LogParserTest {

    private final LogParser parser = new LogParser();

    @Test
    void testFullLogParsing() {
        String log = "[2025-10-27 12:00:00] ERROR - NullPointerException occurred";
        ParsedLog result = parser.parseLine(log);

        assertNotNull(result);
        assertEquals("2025-10-27 12:00:00", result.getTimestamp());
        assertEquals("ERROR", result.getLevel());
        assertEquals("NullPointerException occurred", result.getMessage());
        assertEquals("NullPointerException", result.getExceptionType());
    }

    @Test
    void testPartialLogParsing() {
        String log = "[2025-10-27 13:00:00] INFO - User logged in";
        ParsedLog result = parser.parseLine(log);

        assertNotNull(result);
        assertEquals("INFO", result.getLevel());
        assertNull(result.getExceptionType());
    }

    @Test
    void testInvalidLogReturnsNull() {
        String log = "2025-10-27 13:00:00 INFO User logged in";
        assertNull(parser.parseLine(log));
    }

    @Test
    void testBlankOrNullInput() {
        assertNull(parser.parseLine(""));
        assertNull(parser.parseLine(null));
    }
}
