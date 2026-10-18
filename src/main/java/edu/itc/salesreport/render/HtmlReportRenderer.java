package edu.itc.salesreport.render;

import java.nio.charset.StandardCharsets;

import edu.itc.salesreport.model.BranchSummary;
import edu.itc.salesreport.model.MonthlyReport;

public final class HtmlReportRenderer implements ReportRenderer {

    @Override
    public String extension() {
        return "html";
    }

    @Override
    public byte[] render(MonthlyReport report) {
        var sb = new StringBuilder();
        sb.append("<!DOCTYPE html>\n<html>\n<head><meta charset=\"UTF-8\">")
          .append("<title>Angkor Mart monthly sales ")
          .append(escape(report.month().toString()))
          .append("</title></head>\n<body>\n<h1>Angkor Mart monthly sales ")
          .append(escape(report.month().toString())).append("</h1>\n");
        sb.append("<table>\n<tr><th>Branch</th><th>Revenue</th>")
          .append("<th>Receipts</th><th>Average basket</th></tr>\n");
        for (BranchSummary b : report.branches()) {
            row(sb, b);
        }
        row(sb, report.chain());
        sb.append("</table>\n</body>\n</html>\n");
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static void row(StringBuilder sb, BranchSummary b) {
        sb.append("<tr><td>").append(escape(b.branch()))
          .append("</td><td>").append(b.revenue())
          .append("</td><td>").append(b.receipts())
          .append("</td><td>").append(b.averageBasket())
          .append("</td></tr>\n");
    }

    /** Escapes the three characters that would break the markup. */
    private static String escape(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}