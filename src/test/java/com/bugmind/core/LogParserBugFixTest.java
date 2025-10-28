package com.bugmind.core;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Regression tests for {@link LogParser} after timestamp and spacing fixes.
 * Ensures the parser handles ISO8601 timestamps, extra whitespace,
 * and malformed inputs safely.
 */
public class LogParserBugFixTest {

    private final LogParser parser = new LogParser();

    @Test // ISO8601 timestamps are now supported
    void testIsoTimestampParsing() {
        String log = "[2025-10-27T21:10:00Z] ERROR - Module failed";
        ParsedLog result = parser.parseLine(log);

        assertNotNull(result, "Parser should handle ISO8601 timestamp");
        assertEquals("ERROR", result.getLevel());
        assertTrue(result.getTimestamp().contains("2025-10-27"));
    }

    @Test //  Extra spaces and tabs should not break parsing
    void testExtraSpacesAndTabs() {
        String log = "[2025-10-27 21:10]   WARN   -   spaced out message   ";
        ParsedLog result = parser.parseLine(log);

        assertNotNull(result, "Parser should ignore irregular spacing");
        assertEquals("WARN", result.getLevel());
        assertEquals("spaced out message", result.getMessage().trim());
    }

    @Test //  Detects and extracts exception type properly
    void testDetectsExceptionType() {
        String log = "[2025-10-27 21:10:00] ERROR - IllegalStateException raised in module";
        ParsedLog result = parser.parseLine(log);

        assertNotNull(result);
        assertEquals("IllegalStateException", result.getExceptionType());
    }

    @Test //  Malformed lines should safely return null
    void testMalformedInputReturnsNull() {
        String log = "2025-10-27 21:10 ERROR missing brackets";
        assertNull(parser.parseLine(log), "Malformed line without brackets should return null");
    }
}
