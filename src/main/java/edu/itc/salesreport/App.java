package edu.itc.salesreport;

import java.nio.file.Path;
import java.time.YearMonth;

import edu.itc.salesreport.ingest.ConsoleProgress;
import edu.itc.salesreport.ingest.CsvTransactionParser;
import edu.itc.salesreport.ingest.MonthLoader;

/** The composition root: the only place that wires the parts together. */
public final class App {
    public static void main(String[] args) {
        var month = YearMonth.parse(args.length > 0 ? args[0] : "2026-09");
        var dataDir = Path.of(args.length > 1 ? args[1] : "data");
        var loader = new MonthLoader(new CsvTransactionParser());
        loader.subscribe(new ConsoleProgress());
        loader.load(month, dataDir);
        loader.rejectedRows()
              .forEach(r -> System.out.println("  rejected " + r));
    }
}