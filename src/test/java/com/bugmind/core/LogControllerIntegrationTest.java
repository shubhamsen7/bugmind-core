package com.bugmind.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for LogController and LogService.
 */
public class LogControllerIntegrationTest {

    private LogRepository repository;
    private LogService service;
    private LogController controller;

    @BeforeEach
    void setup() {
        repository = new LogRepository();
        service = new LogService(repository);
        controller = new LogController(service);
    }

    @Test
    void testGetLogsByLevelReturnsCorrectLogs() {
        List<ParsedLog> errors = controller.getLogsByLevel("ERROR");
        assertEquals(1, errors.size(), "Should return only one ERROR log");
        assertEquals("NullPointerException in Service", errors.get(0).getMessage());
    }

    @Test
    void testGetLogsByLevelIsCaseInsensitive() {
        List<ParsedLog> infos = controller.getLogsByLevel("info");
        assertEquals(2, infos.size(), "Should return 2 INFO logs regardless of case");
    }

    @Test
    void testGetLogsByLevelForUnknownLevelReturnsEmptyList() {
        List<ParsedLog> debugLogs = controller.getLogsByLevel("DEBUG");
        assertTrue(debugLogs.isEmpty(), "No DEBUG logs should be present");
    }

    @Test
    void testInvalidInputThrowsException() {
        Exception ex = assertThrows(IllegalArgumentException.class, () -> controller.getLogsByLevel(" "));
        assertEquals("Log level must not be blank", ex.getMessage());
    }
}
