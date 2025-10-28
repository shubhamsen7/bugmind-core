package com.bugmind.core;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for structured LogParser.parseLine()
 */
class LogParserTest {

    // P2P 1: valid line with timestamp, level, and exception
    @Test
    void parseLine_validErrorLine_returnsAllFields() {
        String line = "2025-10-27 12:10:00 ERROR NullPointerException occurred";
        ParsedLog p = LogParser.parseLine(line);
        assertNotNull(p);
        assertEquals("2025-10-27 12:10:00", p.getTimestamp());
        assertEquals("ERROR", p.getLevel());
        assertEquals("NullPointerException", p.getErrorType());
    }

    // P2P 2: line without exception
    @Test
    void parseLine_noException_returnsPartial() {
        String line = "2025-10-27 INFO Starting server on port 8080";
        ParsedLog p = LogParser.parseLine(line);
        assertNotNull(p);
        assertEquals("INFO", p.getLevel());
        assertNull(p.getErrorType());
    }

    // P2P 3: line with whitespace and control chars
    @Test
    void parseLine_withTabsAndSpaces_trimsProperly() {
        String line = "\n\t2025-10-27 12:00:01\t WARN AssertionError: failed\t\n";
        ParsedLog p = LogParser.parseLine(line);
        assertNotNull(p);
        assertEquals("AssertionError", p.getErrorType());
        assertEquals("WARN", p.getLevel());
    }

    // P2P 4: null input returns null
    @Test
    void parseLine_null_returnsNull() {
        assertNull(LogParser.parseLine(null));
    }
}
