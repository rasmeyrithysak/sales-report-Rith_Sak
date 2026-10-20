package edu.itc.salesreport.ingest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MonthLoaderTest {

    private static final String VALID = "PNH,2026-09-01,PNH-000123,SKU-1001,"
            + "Jasmine Rice 5kg,Grocery,2,6.50,0.00,KHQR";
    private static final String INVALID = "REP,2026-09-01,REP-000001,SKU-1,"
            + "Rice,Grocery,two,6.50,0.00,CASH";

    private static void twoFiles(Path dataDir) throws IOException {
        Path dir = Files.createDirectories(dataDir.resolve("2026-09"));
        Files.writeString(dir.resolve("PNH-2026-09-01.csv"),
                CsvTransactionParser.HEADER + "\n" + VALID + "\n" + VALID + "\n");
        Files.writeString(dir.resolve("REP-2026-09-01.csv"),
                CsvTransactionParser.HEADER + "\n" + VALID + "\n" + INVALID + "\n");
    }

    @Test
    void publishesOneEventPerFileAndAFinalEvent(@TempDir Path dataDir)
            throws IOException {
        twoFiles(dataDir);
        var loader = new MonthLoader(new CsvTransactionParser());
        List<LoadEvent> events = new ArrayList<>();
        loader.subscribe(events::add);

        var rows = loader.load(YearMonth.of(2026, 9), dataDir);

        assertEquals(3, events.size());
        var first = assertInstanceOf(LoadEvent.FileLoaded.class, events.get(0));
        var second = assertInstanceOf(LoadEvent.FileLoaded.class, events.get(1));
        var last = assertInstanceOf(LoadEvent.LoadFinished.class, events.get(2));
        assertEquals("PNH-2026-09-01.csv", first.file().getFileName().toString());
        assertEquals(2, first.accepted());
        assertEquals(0, first.rejected());
        assertEquals(1, first.filesDone());
        assertEquals(2, first.filesTotal());
        assertEquals("REP-2026-09-01.csv", second.file().getFileName().toString());
        assertEquals(1, second.accepted());
        assertEquals(1, second.rejected());
        assertEquals(2, second.filesDone());
        assertEquals(3, last.accepted());
        assertEquals(1, last.rejected());
        assertEquals(3, rows.size());
        assertEquals(1, loader.rejectedRows().size());
        assertTrue(loader.rejectedRows().get(0)
                .startsWith("REP-2026-09-01.csv:3 bad quantity"));
    }

    @Test
    void aListenerThatUnsubscribedReceivesNothing(@TempDir Path dataDir)
            throws IOException {
        twoFiles(dataDir);
        var loader = new MonthLoader(new CsvTransactionParser());
        List<LoadEvent> events = new ArrayList<>();
        Runnable unsubscribe = loader.subscribe(events::add);
        unsubscribe.run();

        loader.load(YearMonth.of(2026, 9), dataDir);

        assertTrue(events.isEmpty());
    }
}