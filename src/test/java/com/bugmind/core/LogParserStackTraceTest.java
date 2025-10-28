package com.bugmind.core;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class LogParserStackTraceTest {

    private final LogParser parser = new LogParser();

    @Test
    void testMultiLineStackTraceMerged() {
        String log = """
            [2025-10-27 12:00:00] ERROR - NullPointerException occurred
                at com.example.Main.methodA(Main.java:42)
                at com.example.Main.main(Main.java:20)
            """;
        List<ParsedLog> result = parser.parseLogs(log);
        assertEquals(1, result.size());
        ParsedLog entry = result.get(0);
        assertTrue(entry.getMessage().contains("methodA"));
        assertEquals("NullPointerException", entry.getExceptionType());
    }

    @Test
    void testSingleLineStillWorks() {
        String log = "[2025-10-27 12:05:00] INFO - User logged in";
        List<ParsedLog> result = parser.parseLogs(log);
        assertEquals(1, result.size());
        assertEquals("INFO", result.get(0).getLevel());
    }

    @Test
    void testInterleavedStackTraces() {
        String logs = """
            [2025-10-27 12:00:00] ERROR - NullPointerException occurred
                at com.example.Main.methodA(Main.java:42)
            [2025-10-27 12:10:00] WARN - Low memory warning
            """;
        List<ParsedLog> result = parser.parseLogs(logs);
        assertEquals(2, result.size());
        assertEquals("ERROR", result.get(0).getLevel());
        assertEquals("WARN", result.get(1).getLevel());
    }

    @Test
    void testMalformedWithoutHeaderReturnsEmpty() {
        String log = "Exception: Something bad happened";
        List<ParsedLog> result = parser.parseLogs(log);
        assertTrue(result.isEmpty());
    }
}
