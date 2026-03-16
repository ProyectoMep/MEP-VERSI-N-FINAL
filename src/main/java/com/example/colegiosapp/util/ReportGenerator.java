package com.example.colegiosapp.util;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import com.example.colegiosapp.entity.Cita;
import com.example.colegiosapp.entity.Institucion;
import com.example.colegiosapp.repository.CitaRepository;
import com.example.colegiosapp.repository.InstitucionRepository;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

/**
 * Utility class to generate reports of appointments (citas) in Excel or PDF.
 * If {@code institucionId} is null, it generates a report for all institutions.
 */
@Component
public class ReportGenerator {

    private final CitaRepository citaRepository;
    private final InstitucionRepository institucionRepository;

    public ReportGenerator(CitaRepository citaRepository,
                    InstitucionRepository institucionRepository) {
        this.citaRepository = citaRepository;
        this.institucionRepository = institucionRepository;
    }

    /**
     * Computes a summary of appointment counts grouped by status for the
     * specified institution. If {@code institucionId} is null, the summary
     * counts across all institutions are returned.
     */
    public Map<String, Long> generateCitaStatusSummary(Long institucionId) {
        Map<String, Long> summary = new HashMap<>();
        List<String> estados = List.of("Pendiente asistir", "Reprogramada", "Cancelada", "Asistió");
        for (String estado : estados) {
            long count;
            if (institucionId == null) {
                count = citaRepository.findAll()
                        .stream()
                        .filter(c -> estado.equals(c.getEstado()))
                        .count();
            } else {
                count = citaRepository.findByInstitucionIdAndEstado(institucionId, estado).size();
            }
            summary.put(estado, count);
        }
        return summary;
    }

    /**
     * Generates an Excel workbook listing appointments. If {@code institucionId}
     * is provided, only that institution is included; otherwise, one sheet per institution.
     */
    public Workbook generateCitasWorkbook(Long institucionId) {
        Workbook workbook = new XSSFWorkbook();
        if (institucionId != null) {
            Institucion institucion = institucionRepository.findById(institucionId).orElse(null);
            if (institucion != null) {
                createSheetForInstitucion(workbook, institucion);
            }
        } else {
            List<Institucion> instituciones = institucionRepository.findAll();
            for (Institucion inst : instituciones) {
                createSheetForInstitucion(workbook, inst);
            }
        }
        return workbook;
    }

    /**
     * Creates a sheet listing all appointments for the given institution plus a status summary.
     */
    private void createSheetForInstitucion(Workbook workbook, Institucion institucion) {
        String sheetName = "Inst_" + institucion.getId();
        Sheet sheet = workbook.createSheet(sheetName);
        int rowNum = 0;

        // Title
        Row titleRow = sheet.createRow(rowNum++);
        titleRow.createCell(0).setCellValue("Reporte de citas para " + institucion.getNombre());

        // Summary
        Map<String, Long> summary = generateCitaStatusSummary(institucion.getId());
        for (Map.Entry<String, Long> entry : summary.entrySet()) {
            Row summaryRow = sheet.createRow(rowNum++);
            summaryRow.createCell(0).setCellValue(entry.getKey());
            summaryRow.createCell(1).setCellValue(entry.getValue());
        }

        rowNum++; // Empty row before details

        // Header
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"ID", "Fecha", "Hora", "Nombre", "Correo", "Teléfono", "Cantidad", "Estado"};
        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }

        // Data rows
        List<Cita> citas = citaRepository.findByInstitucionId(institucion.getId());
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        for (Cita c : citas) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(c.getId());
            row.createCell(1).setCellValue(c.getFechaCita() != null ? c.getFechaCita().format(dateFormatter) : "");
            row.createCell(2).setCellValue(c.getHoraCita() != null ? c.getHoraCita().format(timeFormatter) : "");
            row.createCell(3).setCellValue(nullSafe(c.getNombreAgenda()));
            row.createCell(4).setCellValue(nullSafe(c.getCorreoAgenda()));
            row.createCell(5).setCellValue(nullSafe(c.getTelefonoAgenda()));
            row.createCell(6).setCellValue(c.getCantidadCitas());
            row.createCell(7).setCellValue(nullSafe(c.getEstado()));
        }

        // Ajustar automáticamente el tamaño de las columnas
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    // ================= PDF =================

    /**
     * Genera un informe en PDF. Si {@code institucionId} es nulo,
     * genera un PDF con varias secciones (una sección por institución), separadas por páginas.
     */
    @SuppressWarnings("ConvertToTryWithResources")
    public byte[] generateCitasPdf(Long institucionId) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4.rotate(), 36, 36, 36, 36);
            PdfWriter.getInstance(doc, baos);
            doc.open();

            if (institucionId != null) {
                Institucion inst = institucionRepository.findById(institucionId).orElse(null);
                addInstitutionSection(doc, inst);
            } else {
                List<Institucion> instituciones = institucionRepository.findAll();
                for (int i = 0; i < instituciones.size(); i++) {
                    addInstitutionSection(doc, instituciones.get(i));
                    if (i < instituciones.size() - 1) doc.newPage();
                }
            }

            doc.close();
            return baos.toByteArray();
        } catch (DocumentException e) {
            throw new RuntimeException("Error generando PDF: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Error inesperado generando PDF: " + e.getMessage(), e);
        }
    }

    // ---- ayudantes PDF ----

    private void addInstitutionSection(Document doc, Institucion inst) throws DocumentException {
        Font H1 = new Font(Font.HELVETICA, 16, Font.BOLD);
        Font H2 = new Font(Font.HELVETICA, 12, Font.BOLD);
        Font TH = new Font(Font.HELVETICA, 10, Font.BOLD);
        Font TD = new Font(Font.HELVETICA, 10);

        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter tf = DateTimeFormatter.ofPattern("HH:mm");

        if (inst == null) {
            doc.add(new Paragraph("Institución no encontrada.", H2));
            return;
        }

        // Titulo
        doc.add(new Paragraph("Reporte de citas para " + nullSafe(inst.getNombre()), H1));
        doc.add(new Paragraph(" ", H2));

        Map<String, Long> summary = generateCitaStatusSummary(inst.getId());
        PdfPTable sum = new PdfPTable(2);
        sum.setWidthPercentage(30);
        sum.setWidths(new float[]{70, 30});
        addHeader(sum, "Estado", TH);
        addHeader(sum, "Cantidad", TH);
        for (Map.Entry<String, Long> e : summary.entrySet()) {
            addCell(sum, e.getKey(), TD);
            addCell(sum, String.valueOf(e.getValue()), TD);
        }
        doc.add(sum);

        doc.add(new Paragraph(" ", H2));
        doc.add(new Paragraph("Detalle", H2));
        doc.add(new Paragraph(" ", H2));

        // Tabla de detalles
        String[] headers = {"ID", "Fecha", "Hora", "Nombre", "Correo", "Teléfono", "Cantidad", "Estado"};
        float[] widths = {8, 14, 12, 26, 28, 20, 12, 16};

        PdfPTable tbl = new PdfPTable(headers.length);
        tbl.setWidthPercentage(100);
        tbl.setWidths(widths);
        for (String h : headers) addHeader(tbl, h, TH);

        List<Cita> citas = citaRepository.findByInstitucionId(inst.getId());
        for (Cita c : citas) {
            addCell(tbl, String.valueOf(c.getId()), TD);
            addCell(tbl, c.getFechaCita() != null ? c.getFechaCita().format(df) : "-", TD);
            addCell(tbl, c.getHoraCita() != null ? c.getHoraCita().format(tf) : "-", TD);
            addCell(tbl, nullSafe(c.getNombreAgenda()), TD);
            addCell(tbl, nullSafe(c.getCorreoAgenda()), TD);
            addCell(tbl, nullSafe(c.getTelefonoAgenda()), TD);
            addCell(tbl, String.valueOf(c.getCantidadCitas()), TD);
            addCell(tbl, nullSafe(c.getEstado()), TD);
        }

        doc.add(tbl);
    }

    private void addHeader(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(5f);
        table.addCell(cell);
    }

    private void addCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(5f);
        table.addCell(cell);
    }

    private String nullSafe(String s) { return s == null ? "" : s; }
}
