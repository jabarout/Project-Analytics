package com.projectanalytics.reporting.application.generator;

import com.projectanalytics.reporting.application.ReportDocument;
import com.projectanalytics.reporting.domain.ReportFormat;

/**
 * Renders a {@link ReportDocument} into binary export bytes.
 */
public interface ReportFileGenerator {

    ReportFormat format();

    byte[] generate(ReportDocument document);
}
