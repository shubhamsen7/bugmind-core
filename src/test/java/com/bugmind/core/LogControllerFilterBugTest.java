package com.bugmind.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for log filtering accuracy, case handling, and deduplication.
 */
public class LogControllerFilterBugTest {

    private LogRepository repo;
    private LogService service;
    private LogController controller;

    @BeforeEach
    void setup() {
        repo = new LogRepository();
        // duplicate an INFO entry to simulate repeated logs
        repo.add(new ParsedLog("2025-10-30 10:00:00", "INFO", "Application started", null));
        service = new LogService(repo);
        controller = new LogController(service);
    }

    @Test
    void testInvalidBlankThrows() {
        Exception ex = assertThrows(IllegalArgumentException.class, () -> controller.getLogsByLevel(" "));
        assertTrue(ex.getMessage().contains("blank"));
    }

    @Test
    void testUnknownLevelReturnsEmptyList() {
        List<ParsedLog> none = controller.getLogsByLevel("TRACE");
        assertTrue(none.isEmpty());
    }

    @Test
    void testMixedCaseAndCommaSeparatedLevelsDeduplicateProperly() {
        List<ParsedLog> result = controller.getLogsByLevel(" info , ERROR ");
        assertEquals(3, result.size(),
                "Should return 3 unique logs ignoring case, spaces, and duplicates");
    }
}
