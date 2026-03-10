package com.example.colegiosapp.controller;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;   // 👈 IMPORTANTE
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.example.colegiosapp.entity.Cita;
import com.example.colegiosapp.entity.Estudiante;
import com.example.colegiosapp.entity.Institucion;
import com.example.colegiosapp.entity.Usuario;
import com.example.colegiosapp.repository.CitaRepository;
import com.example.colegiosapp.repository.EstudianteRepository;
import com.example.colegiosapp.repository.InstitucionRepository;
import com.example.colegiosapp.repository.UsuarioRepository;

@Controller
@RequestMapping("/gestor")
public class GestorController {

    private final UsuarioRepository usuarioRepository;
    private final InstitucionRepository institucionRepository;
    private final CitaRepository citaRepository;
    private final EstudianteRepository estudianteRepository;

    public GestorController(UsuarioRepository usuarioRepository,
                            InstitucionRepository institucionRepository,
                            CitaRepository citaRepository,
                            EstudianteRepository estudianteRepository) {
        this.usuarioRepository = usuarioRepository;
        this.institucionRepository = institucionRepository;
        this.citaRepository = citaRepository;
        this.estudianteRepository = estudianteRepository;
    }

    // ============================================================
    // 🚪 VALIDACIÓN PRIMER INGRESO
    // ============================================================

    @GetMapping("/ingreso")
    public String validarPrimerIngreso(Authentication auth) {

        Usuario gestor = usuarioRepository.findByCorreo(auth.getName())
                .orElseThrow();

        if (gestor.getIdInstitucion() == null) {
            return "redirect:/gestor/error-sin-institucion";
        }

        Institucion inst = institucionRepository.findById(gestor.getIdInstitucion())
                .orElseThrow();

        if (inst.getMision() == null || inst.getMision().isBlank()
                || inst.getVision() == null || inst.getVision().isBlank()) {

            return "redirect:/gestor/primer-ingreso";
        }

        return "redirect:/gestor/dashboard";
    }

    @GetMapping("/primer-ingreso")
    public String primerIngreso(Authentication auth, Model model) {

        Usuario gestor = usuarioRepository.findByCorreo(auth.getName())
                .orElseThrow();

        Institucion inst = institucionRepository.findById(gestor.getIdInstitucion())
                .orElseThrow();

        model.addAttribute("institucion", inst);

        return "gestor/primer_ingreso";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "gestor/dashboard";
    }

    @GetMapping("/error-sin-institucion")
    public String errorSinInstitucion() {
        return "gestor/error_sin_institucion";
    }

    // ============================================================
    // 📅 CITAS PROGRAMADAS (con filtro por fecha)
    // ============================================================

    @GetMapping("/citas")
    public String listarCitas(Authentication auth,
                              @RequestParam(name = "fecha", required = false)
                              @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                              LocalDate fecha,
                              Model model) {

        Usuario gestor = usuarioRepository.findByCorreo(auth.getName())
                .orElseThrow();

        Long idInstitucion = gestor.getIdInstitucion();
        if (idInstitucion == null) {
            return "redirect:/gestor/error-sin-institucion";
        }

        List<Cita> citas;

        if (fecha != null) {
            citas = citaRepository.findByInstitucionIdAndFechaCita(idInstitucion, fecha);
        } else {
            citas = citaRepository.findByInstitucionId(idInstitucion);
        }

        model.addAttribute("citas", citas);
        model.addAttribute("fechaSeleccionada", fecha);

        return "gestor/citas_programadas";
    }

    @PostMapping("/citas/{id}/no-asistio")
    public String marcarNoAsistio(@PathVariable Long id) {

        Cita cita = citaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada"));

        cita.setEstado("No asistió");
        citaRepository.save(cita);

        return "redirect:/gestor/citas";
    }

    @GetMapping("/citas/{id}/asistio")
    public String mostrarRegistroEstudiantes(@PathVariable Long id, Model model) {

        Cita cita = citaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada"));

        model.addAttribute("cita", cita);
        return "gestor/registrar_estudiantes";
    }

    @PostMapping("/citas/{id}/asistio")
    public String registrarEstudiantes(@PathVariable Long id,
                                       @RequestParam("nombres") List<String> nombres,
                                       @RequestParam("apellidos") List<String> apellidos,
                                       @RequestParam("tiposDocumento") List<String> tiposDocumento,
                                       @RequestParam("numerosDocumento") List<String> numerosDocumento,
                                       @RequestParam("grados") List<String> grados,
                                       @RequestParam("correos") List<String> correos,
                                       @RequestParam("telefonos") List<String> telefonos) {

        Cita cita = citaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada"));

        int total = nombres.size();

        for (int i = 0; i < total; i++) {
            Estudiante est = new Estudiante();
            est.setNombre(nombres.get(i));
            est.setApellido(apellidos.get(i));
            est.setTipoDocumento(tiposDocumento.get(i));
            est.setNumeroDocumento(numerosDocumento.get(i));
            est.setGrado(grados.get(i));
            est.setCorreo(correos.get(i));
            est.setTelefono(telefonos.get(i));
            est.setEstado("Pendiente");
            est.setCita(cita);

            estudianteRepository.save(est);
        }

        cita.setEstado("Asistió");
        citaRepository.save(cita);

        return "redirect:/gestor/citas";
    }

    // ============================================================
    // 🧑‍🎓 MÓDULO DE ESTUDIANTES (Pendientes / Matriculados)
    // ============================================================

    @GetMapping("/estudiantes")
    public String listarEstudiantes(@RequestParam(name = "estado", required = false, defaultValue = "Pendiente")
                                    String estado,
                                    @RequestParam(name = "filtro", required = false) String filtro,
                                    @RequestParam(name = "tipoFiltro", required = false, defaultValue = "nombre")
                                    String tipoFiltro,
                                    Model model) {

        List<Estudiante> estudiantes;

        if (filtro != null && !filtro.isBlank()) {

            if ("documento".equalsIgnoreCase(tipoFiltro)) {
                estudiantes = estudianteRepository
                        .findByEstadoAndNumeroDocumentoContainingIgnoreCase(estado, filtro);
            } else {
                estudiantes = estudianteRepository
                        .findByEstadoAndNombreContainingIgnoreCase(estado, filtro);
            }

        } else {
            estudiantes = estudianteRepository.findByEstado(estado);
        }

        model.addAttribute("estudiantes", estudiantes);
        model.addAttribute("estadoSeleccionado", estado);
        model.addAttribute("filtro", filtro);
        model.addAttribute("tipoFiltro", tipoFiltro);

        return "gestor/estudiantes";
    }

    @PostMapping("/estudiantes/aprobar")
    public String aprobarEstudiantes(@RequestParam("idsEstudiantes") List<Long> idsEstudiantes) {

        if (idsEstudiantes != null && !idsEstudiantes.isEmpty()) {

            List<Estudiante> seleccionados = estudianteRepository.findAllById(idsEstudiantes);

            for (Estudiante est : seleccionados) {
                est.setEstado("Matriculado");
                estudianteRepository.save(est);

                // TODO: Enviar correo genérico de aprobación
            }
        }

        return "redirect:/gestor/estudiantes?estado=Pendiente";
    }

    // ============================================================
    // 📤 CARGA MASIVA DE ESTUDIANTES (solo matriculados)
    // ============================================================

    @PostMapping("/estudiantes/cargar")
    @SuppressWarnings("CallToPrintStackTrace")
    public String cargarEstudiantesExcel(@RequestParam("archivo") MultipartFile archivo) {

        if (archivo.isEmpty()) {
            return "redirect:/gestor/estudiantes?estado=Matriculado&error=vacio";
        }

        int totalCreados = 0;

        try (InputStream is = archivo.getInputStream();
             XSSFWorkbook workbook = new XSSFWorkbook(is)) {   // 👈 usamos XSSFWorkbook (.xlsx)

            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();

            // Fila 0 = encabezados, datos desde la 1
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }

                String nombre    = formatter.formatCellValue(row.getCell(0)).trim();
                String apellido  = formatter.formatCellValue(row.getCell(1)).trim();
                String tipoDoc   = formatter.formatCellValue(row.getCell(2)).trim();
                String numeroDoc = formatter.formatCellValue(row.getCell(3)).trim();
                String grado     = formatter.formatCellValue(row.getCell(4)).trim();
                String correo    = formatter.formatCellValue(row.getCell(5)).trim();
                String telefono  = formatter.formatCellValue(row.getCell(6)).trim();

                if (nombre.isBlank() && numeroDoc.isBlank()) {
                    continue;
                }

                Estudiante est = new Estudiante();
                est.setNombre(nombre);
                est.setApellido(apellido);
                est.setTipoDocumento(tipoDoc);
                est.setNumeroDocumento(numeroDoc);
                est.setGrado(grado.isBlank() ? "Sin grado" : grado);
                est.setCorreo(correo);
                est.setTelefono(telefono);
                est.setEstado("Matriculado");
                // est.setCita(null); // carga masiva sin cita

                estudianteRepository.save(est);
                totalCreados++;
            }

            System.out.println("✅ Estudiantes creados desde Excel: " + totalCreados);

        } catch (Exception e) {
            System.out.println("❌ Error al procesar el archivo Excel:");
            e.printStackTrace();
            return "redirect:/gestor/estudiantes?estado=Matriculado&error=procesar";
        }

        return "redirect:/gestor/estudiantes?estado=Matriculado&carga=ok";
    }

}
