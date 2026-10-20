package edu.itc.salesreport.ingest;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

import edu.itc.salesreport.model.SaleTransaction;

/** The subject of the Observer pattern: loads a month and reports progress. */
public final class MonthLoader {

    private final TransactionParser parser;
    private final List<LoadListener> listeners =
            new CopyOnWriteArrayList<>();
    private final List<String> rejectedRows = new ArrayList<>();

    public MonthLoader(TransactionParser parser) {
        this.parser = parser;
    }

    /** Registers a listener; the returned handle unsubscribes it. */
    public Runnable subscribe(LoadListener listener) {
        listeners.add(listener);
        return () -> listeners.remove(listener);
    }

    public List<String> rejectedRows() {
        return List.copyOf(rejectedRows);
    }

    public List<SaleTransaction> load(YearMonth month, Path dataDir) {
        Instant start = Instant.now();
        rejectedRows.clear();
        List<Path> files = csvFiles(dataDir.resolve(month.toString()));
        List<SaleTransaction> all = new ArrayList<>();
        int totalAccepted = 0;
        int totalRejected = 0;
        int done = 0;
        for (Path file : files) {
            List<String> lines = readLines(file);
            int accepted = 0;
            int rejected = 0;
            for (int i = 1; i < lines.size(); i++) {      // index 0 is the header
                try {
                    all.add(parser.parse(lines.get(i)));
                    accepted++;
                } catch (InvalidRowException e) {
                    rejected++;
                    rejectedRows.add(file.getFileName() + ":" + (i + 1)
                            + " " + e.getMessage());
                }
            }
            done++;
            totalAccepted += accepted;
            totalRejected += rejected;
            publish(new LoadEvent.FileLoaded(file, accepted, rejected,
                    done, files.size()));
        }
        publish(new LoadEvent.LoadFinished(totalAccepted, totalRejected,
                Duration.between(start, Instant.now())));
        return all;
    }

    private static List<Path> csvFiles(Path dir) {
        try (Stream<Path> s = Files.list(dir)) {
            return s.filter(p -> p.getFileName().toString().endsWith(".csv"))
                    .sorted()
                    .toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static List<String> readLines(Path file) {
        try {
            return Files.readAllLines(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void publish(LoadEvent event) {
        listeners.forEach(l -> l.onEvent(event));
    }
}