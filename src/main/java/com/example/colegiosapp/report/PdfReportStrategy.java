package com.example.colegiosapp.report;

import java.io.ByteArrayOutputStream;
import java.util.List;

import org.springframework.stereotype.Component;

import com.example.colegiosapp.entity.Institucion;
import com.example.colegiosapp.repository.CitaRepository;
import com.example.colegiosapp.repository.InstitucionRepository;
import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.PdfWriter;

@Component("pdfReportStrategy")
public class PdfReportStrategy implements ReportStrategy {

    @SuppressWarnings("unused")
    private final CitaRepository citaRepository;
    private final InstitucionRepository institucionRepository;

    public PdfReportStrategy(CitaRepository citaRepository,
                            InstitucionRepository institucionRepository) {
        this.citaRepository = citaRepository;
        this.institucionRepository = institucionRepository;
    }

    @Override
    @SuppressWarnings("ConvertToTryWithResources")
    public byte[] generateReport(Long institucionId) throws Exception {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate(), 36, 36, 36, 36);
            PdfWriter.getInstance(document, baos);
            document.open();

            if (institucionId != null) {
                Institucion inst = institucionRepository.findById(institucionId).orElse(null);
                addInstitutionSection(document, inst);
            } else {
                List<Institucion> instituciones = institucionRepository.findAll();
                for (int i = 0; i < instituciones.size(); i++) {
                    addInstitutionSection(document, instituciones.get(i));
                    // Crea una nueva página entre instituciones, excepto al final
                    if (i < instituciones.size() - 1) document.newPage();
                }
            }
            document.close();
            return baos.toByteArray();
        }
    }

    // Copia aquí el método addInstitutionSection() y cualquier otro método usado para PDF
    private void addInstitutionSection(@SuppressWarnings("unused") Document doc, @SuppressWarnings("unused") Institucion institucion) {
        // … (toda la lógica para crear las tablas y contenido del PDF)
    }
}
