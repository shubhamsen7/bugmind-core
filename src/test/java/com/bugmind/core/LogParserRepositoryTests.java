package com.bugmind.core;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SWE-Bench Task:
 *  F2P #1 reproduces bug (dedup toggle ignored and timestamp bug).
 *  P2P #2–#4 verify parser normalization + dedup logic after fix.
 */
public class LogParserRepositoryTests {

    @BeforeEach
    void clearProps() {
        System.clearProperty("enableDedup");
        System.clearProperty("includeInvalidLog");
    }

    @AfterEach
    void resetProps() {
        System.clearProperty("enableDedup");
        System.clearProperty("includeInvalidLog");
    }

    @Test
    void testRepositoryRespectsDedupToggle_F2P() {
        System.setProperty("enableDedup", "false");   // ignored in buggy version
        LogRepository repo = new LogRepository();
        List<ParsedLog> all = repo.findByLevelsSorted(List.of("INFO", "ERROR", "WARN"), false);
        long total = all.size();
        // ❌ before fix → 4, ✅ after fix → >4
        assertTrue(total > 4, "Duplicates should remain when dedup is disabled");
    }

    @Test
    void testNormalizeTimestampHandlesOffsetAndMillis_P2P() {
        LogParser parser = new LogParser();
        ParsedLog log = parser.parseLine("[2025-10-27T21:10:00.123+05:30] ERROR - Service down");
        assertEquals("2025-10-27 21:10:00", log.getTimestamp(),
                "Parser should strip millis and normalize offset");
    }


    @Test
    void testRepositoryDedupEnabledRemovesDuplicates_P2P() {
        System.setProperty("enableDedup", "true");
        LogRepository repo = new LogRepository();
        List<ParsedLog> all = repo.findByLevelsSorted(List.of("INFO", "WARN", "ERROR"), false);
        long total = all.size();
        assertTrue(total <= 4, "Dedup=true should remove duplicate entries");
    }

    @Test
    void testParserMultiLineStackTraceCollapsing_P2P() {
        String sample = """
            [2025-10-27T10:00:00Z] ERROR - Failure detected
                at com.example.Main.run(Main.java:10)
                at com.example.App.exec(App.java:22)
            Caused by: java.lang.NullPointerException
                at com.example.Helper.call(Helper.java:5)
            """;
        LogParser parser = new LogParser();
        List<ParsedLog> logs = parser.parseLogs(sample);
        assertEquals(1, logs.size());
        ParsedLog log = logs.get(0);
        assertTrue(log.getMessage().contains("Caused by: java.lang.NullPointerException"));
    }
}
