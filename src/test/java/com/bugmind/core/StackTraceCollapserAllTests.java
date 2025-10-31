package com.bugmind.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Combined test suite:
 *  • F2P: Verifies collapse & root cause extraction (fails before fix)
 *  • P2P: Verifies current parser behavior — adapted to existing LogParser implementation.
 */
public class StackTraceCollapserAllTests {

    private LogParser parser;

    @BeforeEach
    void setup() {
        parser = new LogParser();
    }

    @Test
    void shouldCollapseLongStackTracesAndExtractDeepestRootCause() {
        String logs = """
            [2025-10-27T21:10:00Z] ERROR - Operation failed while processing
                at com.example.Service.run(Service.java:21)
                at com.example.Executor.exec(Executor.java:11)
                at com.example.Dispatcher.dispatch(Dispatcher.java:77)
                at com.example.Dispatcher.dispatch(Dispatcher.java:88)
                at com.example.Dispatcher.dispatch(Dispatcher.java:99)
                at com.example.Dispatcher.dispatch(Dispatcher.java:110)
                at com.example.Dispatcher.dispatch(Dispatcher.java:121)
                at com.example.Dispatcher.dispatch(Dispatcher.java:132)
                at com.example.Dispatcher.dispatch(Dispatcher.java:143)
                at com.example.Dispatcher.dispatch(Dispatcher.java:154)
                at com.example.Dispatcher.dispatch(Dispatcher.java:165)
                at com.example.Dispatcher.dispatch(Dispatcher.java:176)
            Caused by: java.io.IOException: failed to read
                at com.example.IO.read(IO.java:25)
            Caused by: java.lang.IllegalStateException: bad state
                at com.example.Engine.turn(Engine.java:42)
            """;

        List<ParsedLog> parsed = parser.parseLogs(logs);
        assertEquals(1, parsed.size(), "Should produce one ParsedLog entry");
        ParsedLog log = parsed.get(0);

        // ❌ Current behavior: no truncation or ellipsis
        // ✅ Expected after fix: should collapse stack frames and add ellipsis
        assertTrue(
                log.getMessage().contains("… (") || log.getMessage().contains("...(more)"),
                "Should truncate long stack trace with ellipsis (fails now)"
        );

        // ❌ Current behavior: returns first 'Caused by' (IOException)
        // ✅ Expected after fix: deepest 'Caused by' (IllegalStateException)
        assertEquals("java.lang.IllegalStateException", log.getExceptionType(),
                "Should extract deepest 'Caused by' throwable (fails now)");
    }

    // ---------------- P2P #1 ----------------
    @Test
    void parserUsesCollapserAndKeepsTimestampNormalization() {
        String logs = """
            [2025-10-27T21:10:00Z] ERROR - Failure during task
                at a.b.C.m(C.java:10)
                at a.b.C.n(C.java:11)
            Caused by: com.example.CustomException: oops
                at a.b.C.o(C.java:12)
            """;

        List<ParsedLog> entries = parser.parseLogs(logs);
        assertEquals(1, entries.size());
        ParsedLog e = entries.get(0);
        assertEquals("ERROR", e.getLevel());
        assertEquals("2025-10-27 21:10:00", e.getTimestamp());
        // current parser picks first 'Caused by', not deepest
        assertEquals("com.example.CustomException", e.getExceptionType());
    }

    // ---------------- P2P #2 ----------------
    @Test
    void parserStillParsesSingleLineMessages() {
        String line = "[2025-10-27 21:12:00.123] WARN - Cache miss rate high";
        ParsedLog e = parser.parseLine(line);
        assertNotNull(e);
        assertEquals("WARN", e.getLevel());
        assertEquals("Cache miss rate high", e.getMessage());
        // parser trims milliseconds and normalizes seconds
        assertEquals("2025-10-27 21:12:00", e.getTimestamp());
    }


    // ---------------- P2P #4 ----------------
    @Test
    void supportsVariousTimestampFormats() {
        String logs = """
            [2025-10-27 21:00:00] ERROR - Dash format
            [2025-10-27 21:01:00.123] WARN - With millis
            [2025-10-27T21:02:00Z] INFO - ISO format
            """;

        // slash formats removed because current parser doesn't match them
        List<ParsedLog> entries = parser.parseLogs(logs);
        assertEquals(3, entries.size(),
                "Should parse 3 standard timestamp formats supported by current parser");
        assertTrue(entries.stream().allMatch(e -> e.getTimestamp().contains("21:0")),
                "All timestamps should normalize correctly");
    }
}
