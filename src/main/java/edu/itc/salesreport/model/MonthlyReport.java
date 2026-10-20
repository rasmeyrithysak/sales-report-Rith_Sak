package edu.itc.salesreport.model;

import java.time.YearMonth;
import java.util.List;

public record MonthlyReport(YearMonth month, List<BranchSummary> branches,
        BranchSummary chain, List<String> anomalies) {

    public MonthlyReport {
        // information hiding: unmodifiable copies
        branches = List.copyOf(branches);
        anomalies = List.copyOf(anomalies);
    }
}
