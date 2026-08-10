package com.projectanalytics.reporting.application;

import java.time.Instant;
import java.util.List;

/**
 * Intermediate presentational document built from analytics DTOs.
 * Generators (PDF/Excel) render this structure only — no scoring here.
 */
public record ReportDocument(
        String title,
        String subtitle,
        Instant generatedAt,
        List<ReportSection> sections
) {
    public ReportDocument {
        sections = sections == null ? List.of() : List.copyOf(sections);
    }

    public record ReportSection(
            String heading,
            List<MetricLine> metrics,
            List<String> paragraphs,
            ReportTable table
    ) {
        public ReportSection {
            metrics = metrics == null ? List.of() : List.copyOf(metrics);
            paragraphs = paragraphs == null ? List.of() : List.copyOf(paragraphs);
        }
    }

    public record MetricLine(String label, String value) {
    }

    public record ReportTable(List<String> headers, List<List<String>> rows) {
        public ReportTable {
            headers = headers == null ? List.of() : List.copyOf(headers);
            rows = rows == null ? List.of() : rows.stream().map(List::copyOf).toList();
        }
    }
}
