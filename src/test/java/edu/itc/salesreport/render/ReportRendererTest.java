package edu.itc.salesreport.render;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ReportRendererTest {

    @ParameterizedTest
    @ValueSource(strings = {"txt", "html", "csv"})
    void everyFormatShowsEveryBranchAndTheChainTotal(String ext) {
        var renderer = ReportRenderer.forExtension(ext);
        var out = new String(renderer.render(TestReports.september()),
                StandardCharsets.UTF_8);
        assertEquals(ext, renderer.extension());
        for (String code : List.of("PNH", "REP", "BTB")) {
            assertTrue(out.contains(code), ext + " should contain " + code);
        }
        assertTrue(out.contains("2300.50"), ext + " should contain 2300.50");
    }

    @Test
    void csvHasTheExactLineForRep() {
        var out = new String(
                ReportRenderer.forExtension("csv").render(TestReports.september()),
                StandardCharsets.UTF_8);
        assertTrue(out.lines().anyMatch("2026-09,REP,800.50,0.00,50,16.01"::equals));
        assertEquals("month,branch,revenue,discounts,receipts,average_basket",
                out.lines().findFirst().orElseThrow());
    }

    @Test
    void unknownExtensionIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> ReportRenderer.forExtension("pdf"));
    }

    /** Compiles only if the switch covers every permitted renderer. */
    private static String kind(ReportRenderer r) {
        return switch (r) {
            case TextReportRenderer t -> "text";
            case HtmlReportRenderer h -> "html";
            case CsvReportRenderer c -> "csv";
        };
    }

    @Test
    void switchOverTheSealedHierarchyNeedsNoDefault() {
        assertEquals("text", kind(ReportRenderer.forExtension("txt")));
        assertEquals("html", kind(ReportRenderer.forExtension("html")));
        assertEquals("csv", kind(ReportRenderer.forExtension("csv")));
    }
}