package com.projectanalytics.reporting.application.generator;

import com.projectanalytics.common.exception.BusinessException;
import com.projectanalytics.common.exception.ErrorCode;
import com.projectanalytics.reporting.application.ReportDocument;
import com.projectanalytics.reporting.domain.ReportFormat;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * Excel (.xlsx) renderer for assembled report documents (Apache POI).
 */
@Component
public class ExcelReportGenerator implements ReportFileGenerator {

    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss 'UTC'").withZone(ZoneOffset.UTC);

    @Override
    public ReportFormat format() {
        return ReportFormat.EXCEL;
    }

    @Override
    public byte[] generate(ReportDocument document) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Report");
            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleStyle.setFont(titleFont);

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            int rowIndex = 0;
            rowIndex = writeCell(sheet, rowIndex, 0, document.title(), titleStyle);
            if (document.subtitle() != null && !document.subtitle().isBlank()) {
                rowIndex = writeCell(sheet, rowIndex, 0, document.subtitle(), null);
            }
            rowIndex = writeCell(sheet, rowIndex, 0, "Generated: " + TIMESTAMP_FORMAT.format(document.generatedAt()), null);
            rowIndex++;

            for (ReportDocument.ReportSection section : document.sections()) {
                rowIndex = writeCell(sheet, rowIndex, 0, section.heading(), headerStyle);
                for (ReportDocument.MetricLine metric : section.metrics()) {
                    Row row = sheet.createRow(rowIndex++);
                    row.createCell(0).setCellValue(metric.label());
                    row.createCell(1).setCellValue(metric.value());
                }
                for (String paragraph : section.paragraphs()) {
                    if (paragraph != null && !paragraph.isBlank()) {
                        rowIndex = writeCell(sheet, rowIndex, 0, paragraph, null);
                    }
                }
                if (section.table() != null && !section.table().headers().isEmpty()) {
                    Row headerRow = sheet.createRow(rowIndex++);
                    for (int i = 0; i < section.table().headers().size(); i++) {
                        Cell cell = headerRow.createCell(i);
                        cell.setCellValue(section.table().headers().get(i));
                        cell.setCellStyle(headerStyle);
                    }
                    for (java.util.List<String> values : section.table().rows()) {
                        Row dataRow = sheet.createRow(rowIndex++);
                        for (int i = 0; i < values.size(); i++) {
                            dataRow.createCell(i).setCellValue(values.get(i) == null ? "" : values.get(i));
                        }
                    }
                }
                rowIndex++;
            }

            for (int i = 0; i < 10; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(output);
            return output.toByteArray();
        } catch (IOException exception) {
            throw new BusinessException(ErrorCode.REPORT_004, "Excel export failed.", exception);
        }
    }

    private static int writeCell(Sheet sheet, int rowIndex, int column, String value, CellStyle style) {
        Row row = sheet.createRow(rowIndex);
        Cell cell = row.createCell(column);
        cell.setCellValue(value == null ? "" : value);
        if (style != null) {
            cell.setCellStyle(style);
        }
        return rowIndex + 1;
    }
}
