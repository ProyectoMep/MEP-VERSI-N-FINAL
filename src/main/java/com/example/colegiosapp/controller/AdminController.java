package com.example.colegiosapp.controller;

import com.example.colegiosapp.entity.Cita;
import com.example.colegiosapp.entity.Institucion;
import com.example.colegiosapp.entity.Rol;
import com.example.colegiosapp.entity.Usuario;
import com.example.colegiosapp.report.ReportService;
import com.example.colegiosapp.repository.CitaRepository;
import com.example.colegiosapp.repository.InstitucionRepository;
import com.example.colegiosapp.repository.RolRepository;
import com.example.colegiosapp.repository.UsuarioRepository;
import com.example.colegiosapp.service.ColegiosApiService;
import com.example.colegiosapp.util.ReportGenerator;

// OpenPDF
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.*;

// Apache POI
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

// Spring
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final InstitucionRepository institucionRepository;
    private final CitaRepository citaRepository;
    private final ReportGenerator reportGenerator;
    private final ReportService reportService;
    private final ColegiosApiService colegiosApiService;
    private final PasswordEncoder passwordEncoder;

    public AdminController(UsuarioRepository usuarioRepository,
                           RolRepository rolRepository,
                           InstitucionRepository institucionRepository,
                           CitaRepository citaRepository,
                           ReportGenerator reportGenerator,
                           ReportService reportService,
                           ColegiosApiService colegiosApiService,
                           PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.institucionRepository = institucionRepository;
        this.citaRepository = citaRepository;
        this.reportGenerator = reportGenerator;
        this.reportService = reportService;
        this.colegiosApiService = colegiosApiService;
        this.passwordEncoder = passwordEncoder;
    }

    /* =================== DASHBOARD =================== */

    @GetMapping("/dashboard")
    public String dashboard() {
        return "admin/dashboard";
    }

    /* =================== CREACIÓN DE USUARIOS =================== */

    @GetMapping("/usuarios/nuevo")
    public String mostrarCrearUsuario(Model model) {
        model.addAttribute("instituciones", institucionRepository.findAll());
        model.addAttribute("roles", rolRepository.findAll());
        return "admin/crear_usuario";
    }

    @PostMapping("/usuarios/nuevo")
    public String crearUsuario(@RequestParam("nombre") String nombre,
                               @RequestParam("apellido") String apellido,
                               @RequestParam("correo") String correo,
                               @RequestParam("telefono") String telefono,
                               @RequestParam("password") String password,
                               @RequestParam("rolId") Integer rolId,
                               @RequestParam(value = "institucionId", required = false) Long institucionId,
                               Model model) {

        if (usuarioRepository.findByCorreo(correo).isPresent()) {
            model.addAttribute("error", "Ya existe un usuario con ese correo.");
            model.addAttribute("instituciones", institucionRepository.findAll());
            model.addAttribute("roles", rolRepository.findAll());
            return "admin/crear_usuario";
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setApellido(apellido);
        usuario.setCorreo(correo);
        usuario.setTelefono(telefono);
        usuario.setPassword(passwordEncoder.encode(password));
        usuario.setTipoDocumento("CC");
        usuario.setNumeroDocumento(correo);

        Rol rol = rolRepository.findById(rolId)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));
        usuario.setRol(rol);

        if (institucionId != null) {
            usuario.setIdInstitucion(institucionId);
        }

        usuarioRepository.save(usuario);
        return "redirect:/admin/dashboard";
    }

    /* =================== SINCRONIZACIÓN DE COLEGIOS =================== */

    @GetMapping("/colegios/sincronizar")
    public String sincronizarColegios(Model model) {
        int total = colegiosApiService.sincronizarColegios();
        model.addAttribute("totalSincronizados", total);
        return "admin/colegios_sincronizar";
    }

    /* =================== REGISTRAR INSTITUCIÓN =================== */

    @GetMapping("/registrar-institucion")
    public String mostrarRegistrarInstitucion(Model model) {
        model.addAttribute("institucion", new Institucion());
        return "admin/registrar_institucion";
    }

    @PostMapping("/registrar-institucion")
    public String registrarInstitucion(@ModelAttribute Institucion institucion) {
        institucionRepository.save(institucion);
        return "redirect:/admin/dashboard";
    }

    /* =================== REPORTES =================== */

    @GetMapping("/reportes")
    public String mostrarReportes(
            @RequestParam(value = "institucionId", required = false) Long institucionId,
            @RequestParam(value = "estado",        required = false) String estado,
            @RequestParam(value = "mes",           required = false) String mes,
            Model model) {

        model.addAttribute("instituciones", institucionRepository.findAll());
        model.addAttribute("estadosDisponibles",
                List.of("PENDIENTE", "CONFIRMADA", "CANCELADA", "REPROGRAMADA",
                        "ASISTIÓ", "NO ASISTIÓ", "PENDIENTE ASISTIR"));

        // Institución
        Institucion selectedInstitucion = null;
        if (institucionId != null) {
            selectedInstitucion = institucionRepository.findById(institucionId).orElse(null);
        }
        model.addAttribute("selectedInstitucion", selectedInstitucion);

        // Estado
        String estadoSel = (estado != null && !estado.isBlank()) ? estado : null;
        model.addAttribute("estadoSeleccionado", estadoSel);

        // Mes (formato "YYYY-MM" del input type="month")
        Integer anioSel = null;
        Integer mesSel  = null;
        if (mes != null && !mes.isBlank()) {
            try {
                YearMonth ym = YearMonth.parse(mes);
                anioSel = ym.getYear();
                mesSel  = ym.getMonthValue();
            } catch (Exception ignored) {}
        }
        model.addAttribute("mesSeleccionado", mes);

        // Citas filtradas
        List<Cita> citas = obtenerCitasFiltradas(
                selectedInstitucion != null ? selectedInstitucion.getId() : null,
                estadoSel, anioSel, mesSel);

        // ── Stats por estado (para KPIs y gráfica) ──
        Map<String, Long> statsPorEstado = citas.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getEstado() != null ? c.getEstado().toString() : "SIN ESTADO",
                        Collectors.counting()));

        // ── Stats por institución top 10 ──
        Map<String, Long> statsPorInstitucion = citas.stream()
                .filter(c -> c.getInstitucion() != null)
                .collect(Collectors.groupingBy(
                        c -> c.getInstitucion().getNombre(),
                        Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toMap(
                        Map.Entry::getKey, Map.Entry::getValue,
                        (e1, e2) -> e1, LinkedHashMap::new));

        // ── Stats por mes ──
        Map<String, Long> statsPorMes = citas.stream()
                .filter(c -> c.getFechaCita() != null)
                .collect(Collectors.groupingBy(
                        c -> c.getFechaCita().getYear() + "-"
                                + String.format("%02d", c.getFechaCita().getMonthValue()),
                        Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(
                        Map.Entry::getKey, Map.Entry::getValue,
                        (e1, e2) -> e1, LinkedHashMap::new));

        // ── Serializar para Chart.js como listas paralelas ──
        // Por estado
        List<String> estadoLabels  = new ArrayList<>(statsPorEstado.keySet());
        List<Long>   estadoValues  = new ArrayList<>(statsPorEstado.values());

        // Por institución
        List<String> instLabels    = new ArrayList<>(statsPorInstitucion.keySet());
        List<Long>   instValues    = new ArrayList<>(statsPorInstitucion.values());

        // Por mes
        List<String> mesLabels     = new ArrayList<>(statsPorMes.keySet());
        List<Long>   mesValues     = new ArrayList<>(statsPorMes.values());

        model.addAttribute("estadoLabels",  estadoLabels);
        model.addAttribute("estadoValues",  estadoValues);
        model.addAttribute("instLabels",    instLabels);
        model.addAttribute("instValues",    instValues);
        model.addAttribute("mesLabels",     mesLabels);
        model.addAttribute("mesValues",     mesValues);

        model.addAttribute("citas",      citas);
        model.addAttribute("totalCitas", citas.size());
        model.addAttribute("stats",      statsPorEstado);

        return "admin/reportes";
    }

    /* =================== EXPORTAR EXCEL =================== */

    @GetMapping("/reportes/download")
    public ResponseEntity<byte[]> descargarExcel(
            @RequestParam(value = "institucionId", required = false) Long institucionId,
            @RequestParam(value = "estado",        required = false) String estado,
            @RequestParam(value = "mes",           required = false) String mes)
            throws IOException {

        Integer anio = null, mesNum = null;
        if (mes != null && !mes.isBlank()) {
            try { YearMonth ym = YearMonth.parse(mes); anio = ym.getYear(); mesNum = ym.getMonthValue(); }
            catch (Exception ignored) {}
        }
        List<Cita> citas = obtenerCitasFiltradas(institucionId,
                (estado != null && !estado.isBlank()) ? estado : null, anio, mesNum);

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Reporte de Citas");

            // Estilo encabezado
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // Estilo datos normal
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);

            // Estilo datos alternado
            CellStyle dataStyleAlt = workbook.createCellStyle();
            dataStyleAlt.cloneStyleFrom(dataStyle);
            dataStyleAlt.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
            dataStyleAlt.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            String[] columns = {
                "ID", "Fecha", "Hora", "Nombre", "Correo",
                "Teléfono", "Cantidad", "Estado", "Institución"
            };

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 5500);
            }

            int rowNum = 1;
            for (Cita c : citas) {
                Row row = sheet.createRow(rowNum);
                CellStyle style = (rowNum % 2 == 0) ? dataStyleAlt : dataStyle;

                Cell c0 = row.createCell(0); c0.setCellValue(c.getId() != null ? c.getId() : 0L); c0.setCellStyle(style);
                Cell c1 = row.createCell(1); c1.setCellValue(c.getFechaCita() != null ? c.getFechaCita().toString() : "-"); c1.setCellStyle(style);
                Cell c2 = row.createCell(2); c2.setCellValue(c.getHoraCita() != null ? c.getHoraCita().toString() : "-"); c2.setCellStyle(style);
                Cell c3 = row.createCell(3); c3.setCellValue(c.getNombreAgenda() != null ? c.getNombreAgenda() : "-"); c3.setCellStyle(style);
                Cell c4 = row.createCell(4); c4.setCellValue(c.getCorreoAgenda() != null ? c.getCorreoAgenda() : "-"); c4.setCellStyle(style);
                Cell c5 = row.createCell(5); c5.setCellValue(c.getTelefonoAgenda() != null ? c.getTelefonoAgenda() : "-"); c5.setCellStyle(style);
                Cell c6 = row.createCell(6); c6.setCellValue(c.getCantidadCitas() != null ? c.getCantidadCitas() : 0); c6.setCellStyle(style);
                Cell c7 = row.createCell(7); c7.setCellValue(c.getEstado() != null ? c.getEstado().toString() : "-"); c7.setCellStyle(style);
                Cell c8 = row.createCell(8); c8.setCellValue(c.getInstitucion() != null ? c.getInstitucion().getNombre() : "-"); c8.setCellStyle(style);

                rowNum++;
            }

            workbook.write(out);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reporte_citas.xlsx")
                    .contentType(MediaType.parseMediaType(
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(out.toByteArray());
        }
    }

    /* =================== EXPORTAR PDF =================== */

    @GetMapping("/reportes/download-pdf")
    public ResponseEntity<byte[]> descargarPdf(
            @RequestParam(value = "institucionId", required = false) Long institucionId,
            @RequestParam(value = "estado",        required = false) String estado,
            @RequestParam(value = "mes",           required = false) String mes)
            throws IOException {

        Integer anio = null, mesNum = null;
        if (mes != null && !mes.isBlank()) {
            try { YearMonth ym = YearMonth.parse(mes); anio = ym.getYear(); mesNum = ym.getMonthValue(); }
            catch (Exception ignored) {}
        }
        List<Cita> citas = obtenerCitasFiltradas(institucionId,
                (estado != null && !estado.isBlank()) ? estado : null, anio, mesNum);

        // Stats para gráficas en PDF
        Map<String, Long> statsPorEstado = citas.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getEstado() != null ? c.getEstado().toString() : "SIN ESTADO",
                        Collectors.counting()));

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont  = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, new Color(13, 59, 142));
            Font subFont    = FontFactory.getFont(FontFactory.HELVETICA, 10,  new Color(100, 100, 100));
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
            Font cellFont   = FontFactory.getFont(FontFactory.HELVETICA, 8,   new Color(51, 51, 51));
            Font sectionFont= FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, new Color(13, 59, 142));
            Font footerFont = FontFactory.getFont(FontFactory.HELVETICA, 8,   new Color(150, 150, 150));

            // Título
            Paragraph title = new Paragraph("Reporte de Citas · MEP", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(4);
            document.add(title);

            Paragraph sub = new Paragraph(
                    "Matrículas y Educación Pública — República de Colombia", subFont);
            sub.setAlignment(Element.ALIGN_CENTER);
            sub.setSpacingAfter(16);
            document.add(sub);

            // ── Resumen estadístico (tabla de stats) ──
            Paragraph secStats = new Paragraph("Resumen por estado", sectionFont);
            secStats.setSpacingAfter(8);
            document.add(secStats);

            PdfPTable statsTable = new PdfPTable(statsPorEstado.size());
            statsTable.setWidthPercentage(60);
            statsTable.setHorizontalAlignment(Element.ALIGN_LEFT);
            statsTable.setSpacingAfter(20);

            Font statLabelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, new Color(13,59,142));
            Font statValFont   = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, new Color(13,59,142));
            Font statSubFont   = FontFactory.getFont(FontFactory.HELVETICA, 8, new Color(100,100,100));

            for (Map.Entry<String, Long> e : statsPorEstado.entrySet()) {
                PdfPCell statCell = new PdfPCell();
                statCell.setPadding(10);
                statCell.setBackgroundColor(new Color(240, 244, 255));
                statCell.setBorderColor(new Color(200, 210, 235));

                Paragraph statLabel = new Paragraph(e.getKey(), statLabelFont);
                statLabel.setAlignment(Element.ALIGN_CENTER);
                Paragraph statVal = new Paragraph(String.valueOf(e.getValue()), statValFont);
                statVal.setAlignment(Element.ALIGN_CENTER);
                Paragraph statSub = new Paragraph("cita(s)", statSubFont);
                statSub.setAlignment(Element.ALIGN_CENTER);

                statCell.addElement(statLabel);
                statCell.addElement(statVal);
                statCell.addElement(statSub);
                statsTable.addCell(statCell);
            }

            // Total
            PdfPCell totalCell = new PdfPCell();
            totalCell.setPadding(10);
            totalCell.setBackgroundColor(new Color(13, 59, 142));
            totalCell.setBorderColor(new Color(7, 37, 92));
            Font totalLabelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
            Font totalValFont   = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.WHITE);
            Font totalSubFont   = FontFactory.getFont(FontFactory.HELVETICA, 8, new Color(200,210,235));
            Paragraph tl = new Paragraph("TOTAL", totalLabelFont); tl.setAlignment(Element.ALIGN_CENTER);
            Paragraph tv = new Paragraph(String.valueOf(citas.size()), totalValFont); tv.setAlignment(Element.ALIGN_CENTER);
            Paragraph ts = new Paragraph("registros", totalSubFont); ts.setAlignment(Element.ALIGN_CENTER);
            totalCell.addElement(tl); totalCell.addElement(tv); totalCell.addElement(ts);

            document.add(statsTable);

            // ── Tabla de citas ──
            Paragraph secTable = new Paragraph("Detalle de citas", sectionFont);
            secTable.setSpacingAfter(8);
            document.add(secTable);

            PdfPTable table = new PdfPTable(9);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1.5f, 2.5f, 2f, 3f, 4f, 2.5f, 1.5f, 2.5f, 4f});
            table.setSpacingBefore(4);

            String[] headers = {
                "ID", "Fecha", "Hora", "Nombre", "Correo",
                "Teléfono", "Cant.", "Estado", "Institución"
            };
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
                cell.setBackgroundColor(new Color(13, 59, 142));
                cell.setPadding(6);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                cell.setBorderColor(new Color(7, 37, 92));
                table.addCell(cell);
            }

            boolean odd = true;
            for (Cita c : citas) {
                Color rowBg = odd ? Color.WHITE : new Color(240, 244, 255);
                String[] values = {
                    String.valueOf(c.getId() != null ? c.getId() : "-"),
                    c.getFechaCita()      != null ? c.getFechaCita().toString()      : "-",
                    c.getHoraCita()       != null ? c.getHoraCita().toString()       : "-",
                    c.getNombreAgenda()   != null ? c.getNombreAgenda()              : "-",
                    c.getCorreoAgenda()   != null ? c.getCorreoAgenda()              : "-",
                    c.getTelefonoAgenda() != null ? c.getTelefonoAgenda()            : "-",
                    String.valueOf(c.getCantidadCitas() != null ? c.getCantidadCitas() : 0),
                    c.getEstado()         != null ? c.getEstado().toString()         : "-",
                    c.getInstitucion()    != null ? c.getInstitucion().getNombre()   : "-"
                };
                for (String v : values) {
                    PdfPCell cell = new PdfPCell(new Phrase(v, cellFont));
                    cell.setBackgroundColor(rowBg);
                    cell.setPadding(5);
                    cell.setBorderColor(new Color(220, 220, 220));
                    cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    table.addCell(cell);
                }
                odd = !odd;
            }

            document.add(table);

            Paragraph footer = new Paragraph(
                    "Total de registros: " + citas.size()
                    + "  ·  Generado por MEP Sistema en Línea",
                    footerFont);
            footer.setAlignment(Element.ALIGN_RIGHT);
            footer.setSpacingBefore(12);
            document.add(footer);

            document.close();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reporte_citas.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(out.toByteArray());
        }
    }

    /* =================== HELPER PRIVADO =================== */

    private List<Cita> obtenerCitasFiltradas(Long institucionId, String estado,
                                              Integer anio, Integer mes) {
        boolean tieneInst   = institucionId != null;
        boolean tieneEstado = estado != null && !estado.isBlank();
        boolean tieneMes    = anio != null && mes != null;

        if (tieneInst && tieneEstado && tieneMes)
            return citaRepository.findByInstitucionIdAndEstadoAndAnioAndMes(
                    institucionId, estado, anio, mes);

        if (tieneInst && tieneMes)
            return citaRepository.findByInstitucionIdAndAnioAndMes(
                    institucionId, anio, mes);

        if (tieneEstado && tieneMes)
            return citaRepository.findByEstadoAndAnioAndMes(estado, anio, mes);

        if (tieneMes)
            return citaRepository.findByAnioAndMes(anio, mes);

        if (tieneInst && tieneEstado)
            return citaRepository.findByInstitucionIdAndEstado(institucionId, estado);

        if (tieneInst)
            return citaRepository.findByInstitucionId(institucionId);

        if (tieneEstado)
            return citaRepository.findAll().stream()
                    .filter(c -> estado.equals(
                            c.getEstado() != null ? c.getEstado().toString() : null))
                    .collect(Collectors.toList());

        return citaRepository.findAll();
    }
}