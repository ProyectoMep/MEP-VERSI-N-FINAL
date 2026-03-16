package com.example.colegiosapp.report;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class ReportService {

    private final ReportStrategy excelReportStrategy;
    private final ReportStrategy pdfReportStrategy;

    public ReportService(@Qualifier("excelReportStrategy") ReportStrategy excelReportStrategy,
                        @Qualifier("pdfReportStrategy") ReportStrategy pdfReportStrategy) {
        this.excelReportStrategy = excelReportStrategy;
        this.pdfReportStrategy = pdfReportStrategy;
    }

    public byte[] generateReport(String format, Long institucionId) throws Exception {
        switch (format == null ? "excel" : format.toLowerCase()) {
            case "pdf":
                return pdfReportStrategy.generateReport(institucionId);
            case "excel":
            default:
                return excelReportStrategy.generateReport(institucionId);
        }
    }
}
