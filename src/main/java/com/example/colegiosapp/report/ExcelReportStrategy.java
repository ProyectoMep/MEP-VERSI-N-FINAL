package com.example.colegiosapp.report;

import java.io.ByteArrayOutputStream;
import java.util.List;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import com.example.colegiosapp.entity.Institucion;
import com.example.colegiosapp.repository.CitaRepository;
import com.example.colegiosapp.repository.InstitucionRepository;

@Component("excelReportStrategy")
public class ExcelReportStrategy implements ReportStrategy {

    private final InstitucionRepository institucionRepository;

    public ExcelReportStrategy(CitaRepository citaRepository,
                            InstitucionRepository institucionRepository) {
        this.institucionRepository = institucionRepository;
    }

    @Override
    public byte[] generateReport(Long institucionId) throws Exception {
        Workbook workbook = new XSSFWorkbook();
        if (institucionId != null) {
            Institucion institucion = institucionRepository.findById(institucionId).orElse(null);
            createSheetForInstitucion(workbook, institucion);
        } else {
            List<Institucion> instituciones = institucionRepository.findAll();
            for (Institucion inst : instituciones) {
                createSheetForInstitucion(workbook, inst);
            }
        }
        // Convertir a bytes
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            workbook.write(baos);
            return baos.toByteArray();
        }
    }

    // Copia aquí el método createSheetForInstitucion() tal como lo tienes en ReportGenerator
    private void createSheetForInstitucion(@SuppressWarnings("unused") Workbook workbook, @SuppressWarnings("unused") Institucion institution) {
        // … (toda la lógica de creación de la hoja, cabecera y filas)
    }
}
