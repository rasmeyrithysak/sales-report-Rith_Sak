package edu.itc.salesreport.render;

import java.nio.charset.StandardCharsets;

import edu.itc.salesreport.model.BranchSummary;
import edu.itc.salesreport.model.MonthlyReport;

public final class CsvReportRenderer implements ReportRenderer {

    @Override
    public String extension() {
        return "csv";
    }

    @Override
    public byte[] render(MonthlyReport report) {
        var sb = new StringBuilder();
        sb.append("month,branch,revenue,discounts,receipts,average_basket\n");
        for (BranchSummary b : report.branches()) {
            line(sb, report, b);
        }
        line(sb, report, report.chain());
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static void line(StringBuilder sb, MonthlyReport report,
            BranchSummary b) {
        sb.append(report.month()).append(',').append(b.branch())
          .append(',').append(b.revenue())
          .append(',').append(b.discounts())
          .append(',').append(b.receipts())
          .append(',').append(b.averageBasket()).append('\n');
    }
}