package com.bugmind.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for chronological sorting and timestamp normalization.
 */
public class LogControllerTimestampBugTest {

    private LogController controller;

    @BeforeEach
    void setup() {
        System.setProperty("includeInvalidLog", "true");
        LogRepository repo = new LogRepository();
        LogService service = new LogService(repo);
        controller = new LogController(service);
    }

    @Test
    void testLogsAreChronologicallySortedAscending() {
        List<ParsedLog> logs = controller.getLogsByLevelAndSort("INFO", "asc");
        assertFalse(logs.isEmpty());
        assertEquals("Application started", logs.get(0).getMessage());
        assertTrue(
                logs.get(0).getParsedTimestamp().isBefore(logs.get(1).getParsedTimestamp()),
                "Logs should be sorted ascending by timestamp"
        );
    }

    @Test
    void testDescendingOrderWorks() {
        List<ParsedLog> logs = controller.getLogsByLevelAndSort("INFO", "desc");
        assertFalse(logs.isEmpty());
        assertEquals("Background task executed", logs.get(0).getMessage());
    }

    @Test
    void testInvalidTimestampsHandledGracefully() {
        List<ParsedLog> logs = controller.getLogsByLevelAndSort("INFO", "asc");
        assertTrue(logs.stream().anyMatch(l -> l.getTimestamp().isBlank()));
        assertDoesNotThrow(() -> controller.getLogsByLevelAndSort("INFO", "asc"));
    }

    @Test
    void testMixedFormatsParsedCorrectly() {
        List<ParsedLog> logs = controller.getLogsByLevelAndSort("ERROR,WARN", "asc");
        assertEquals(2, logs.size(), "Should return both ERROR and WARN logs");
        assertNotNull(logs.get(0).getParsedTimestamp());
    }
}
