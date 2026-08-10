package com.projectanalytics.reporting.application.generator;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.projectanalytics.common.exception.BusinessException;
import com.projectanalytics.common.exception.ErrorCode;
import com.projectanalytics.reporting.application.ReportDocument;
import com.projectanalytics.reporting.domain.ReportFormat;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * PDF renderer for assembled report documents (OpenPDF).
 */
@Component
public class PdfReportGenerator implements ReportFileGenerator {

    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss 'UTC'").withZone(ZoneOffset.UTC);

    @Override
    public ReportFormat format() {
        return ReportFormat.PDF;
    }

    @Override
    public byte[] generate(ReportDocument document) {
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            Document pdf = new Document(PageSize.A4, 36, 36, 48, 36);
            PdfWriter.getInstance(pdf, output);
            pdf.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, Color.DARK_GRAY);
            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.GRAY);
            Font headingFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.BLACK);
            Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
            Font smallFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.DARK_GRAY);

            pdf.add(new Paragraph(document.title(), titleFont));
            if (document.subtitle() != null && !document.subtitle().isBlank()) {
                pdf.add(new Paragraph(document.subtitle(), subtitleFont));
            }
            pdf.add(new Paragraph("Generated: " + TIMESTAMP_FORMAT.format(document.generatedAt()), smallFont));
            pdf.add(new Paragraph(" "));

            for (ReportDocument.ReportSection section : document.sections()) {
                pdf.add(new Paragraph(section.heading(), headingFont));
                pdf.add(new Paragraph(" "));

                for (ReportDocument.MetricLine metric : section.metrics()) {
                    pdf.add(new Paragraph(metric.label() + ": " + metric.value(), bodyFont));
                }

                for (String paragraph : section.paragraphs()) {
                    if (paragraph != null && !paragraph.isBlank()) {
                        pdf.add(new Paragraph("• " + paragraph, bodyFont));
                    }
                }

                if (section.table() != null && !section.table().headers().isEmpty()) {
                    pdf.add(new Paragraph(" "));
                    pdf.add(buildTable(section.table(), bodyFont, smallFont));
                }
                pdf.add(new Paragraph(" "));
            }

            pdf.close();
            return output.toByteArray();
        } catch (DocumentException exception) {
            throw new BusinessException(ErrorCode.REPORT_004, "PDF export failed.", exception);
        }
    }

    private static PdfPTable buildTable(ReportDocument.ReportTable table, Font bodyFont, Font headerFont) {
        PdfPTable pdfTable = new PdfPTable(table.headers().size());
        pdfTable.setWidthPercentage(100);
        for (String header : table.headers()) {
            PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
            cell.setBackgroundColor(new Color(241, 245, 249));
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            cell.setPadding(4);
            pdfTable.addCell(cell);
        }
        for (java.util.List<String> row : table.rows()) {
            for (String value : row) {
                PdfPCell cell = new PdfPCell(new Phrase(value == null ? "" : value, bodyFont));
                cell.setPadding(3);
                pdfTable.addCell(cell);
            }
        }
        return pdfTable;
    }
}
