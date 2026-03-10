package com.example.colegiosapp.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import com.example.colegiosapp.entity.Cita;
import com.example.colegiosapp.entity.Institucion;
import com.example.colegiosapp.entity.Usuario;
import com.example.colegiosapp.repository.CitaRepository;
import com.example.colegiosapp.repository.InstitucionRepository;
import com.example.colegiosapp.repository.UsuarioRepository;

import jakarta.servlet.http.HttpSession;

/**
 * Gestiona páginas específicas del tutor, como el panel de control, la
 * programación de citas y la visualización de citas programadas.
 * El flujo de programación de citas está implementado en dos pasos:
 * 1) el usuario completa el formulario y
 * 2) confirma los datos antes de guardarlos.
 */
@Controller
@RequestMapping("/tutor")
@SessionAttributes("citaPendiente")
public class TutorController {

    private final InstitucionRepository institucionRepository;
    private final UsuarioRepository usuarioRepository;
    private final CitaRepository citaRepository;

    public TutorController(InstitucionRepository institucionRepository,
                           UsuarioRepository usuarioRepository,
                           CitaRepository citaRepository) {
        this.institucionRepository = institucionRepository;
        this.usuarioRepository = usuarioRepository;
        this.citaRepository = citaRepository;
    }

    /**
     * Muestra el panel del tutor con botones de navegación.
     */
    @GetMapping("/dashboard")
    public String dashboard() {
        return "tutor/dashboard";
    }

    /**
     * Vista "Encuentra tu colegio":
     * - Muestra un listado de localidades disponibles (distinct en BD).
     * - Si el usuario selecciona una localidad, se listan las instituciones de esa localidad.
     *
     * URL: GET /tutor/encuentra-colegio
     */
    @GetMapping("/encuentra-colegio")
    public String mostrarEncuentraColegio(
            @RequestParam(value = "localidad", required = false) String localidadSeleccionada,
            Model model) {

        // 1. Todas las localidades disponibles en la tabla instituciones
        List<String> localidades = institucionRepository.findAllLocalidades();
        model.addAttribute("localidades", localidades);
        model.addAttribute("localidadSeleccionada", localidadSeleccionada);

        // 2. Lista de colegios según la localidad elegida (si existe selección)
        List<Institucion> colegios = List.of();
        if (localidadSeleccionada != null && !localidadSeleccionada.isBlank()) {
            colegios = institucionRepository.findByLocalidad(localidadSeleccionada);
        }
        model.addAttribute("colegios", colegios);

        // 3. Retorna la vista del tutor para esta funcionalidad
        return "tutor/encuentra_colegio";
    }

    /**
     * Muestra el formulario de programación de citas.
     * Si se proporciona una localidad, se recuperan solo las instituciones
     * de esa localidad; de lo contrario, no se muestran instituciones.
     */
    @GetMapping("/agendar-cita")
    public String showAgendarCita(
            @RequestParam(value = "localidad", required = false) String localidad,
            Model model) {

        // Localidades disponibles
        List<String> localidades = institucionRepository.findAllLocalidades();
        model.addAttribute("localidades", localidades);

        // Instituciones filtradas por localidad
        if (localidad != null && !localidad.isEmpty()) {
            List<Institucion> instituciones = institucionRepository.findByLocalidad(localidad);
            model.addAttribute("instituciones", instituciones);
            model.addAttribute("selectedLocalidad", localidad);
        }

        // Grados académicos (Primero a Undécimo)
        List<String> grados = Arrays.asList(
                "Primero", "Segundo", "Tercero", "Cuarto", "Quinto",
                "Sexto", "Séptimo", "Octavo", "Noveno", "Décimo", "Undécimo");
        model.addAttribute("grados", grados);

        // Jornadas
        List<String> jornadas = Arrays.asList("Mañana", "Tarde", "Sabatina", "Noche");
        model.addAttribute("jornadas", jornadas);

        // Motivos de la cita
        List<String> motivos = Arrays.asList("Proceso de matrícula", "Atención al usuario");
        model.addAttribute("motivos", motivos);

        return "tutor/agendar_cita";
    }

    /**
     * Gestiona el primer paso de la programación de citas.
     * Valida el límite de 20 citas por día (por institución) y, si es correcto,
     * construye un objeto Cita en memoria y redirige a la página de confirmación.
     */
    @PostMapping("/agendar-cita")
    public String processAgendarCita(
            @RequestParam("institucionId") Long institucionId,
            @RequestParam("grado") String grado,
            @RequestParam("cantidad") Integer cantidad,
            @RequestParam("motivo") String motivo,
            @RequestParam("jornada") String jornada,
            @RequestParam("fecha") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam("hora")  @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime hora,
            Authentication authentication,
            Model model) {

        // Usuario autenticado
        Usuario usuario = usuarioRepository.findByCorreo(authentication.getName())
                .orElseThrow();

        // Institución seleccionada
        Institucion institucion = institucionRepository.findById(institucionId)
                .orElseThrow();

        // ===== Validar límite de 20 citas por día PARA ESA INSTITUCIÓN =====
        long totalCitasDiaColegio =
                citaRepository.countByInstitucionIdAndFechaCita(institucionId, fecha);

        if (totalCitasDiaColegio >= 20) {
            // Reconstruir datos del formulario para mostrar mensaje y permitir elegir otra fecha
            List<String> localidades = institucionRepository.findAllLocalidades();
            model.addAttribute("localidades", localidades);

            String localidad = institucion.getLocalidad();
            List<Institucion> instituciones = institucionRepository.findByLocalidad(localidad);
            model.addAttribute("instituciones", instituciones);
            model.addAttribute("selectedLocalidad", localidad);

            List<String> grados = Arrays.asList(
                    "Primero", "Segundo", "Tercero", "Cuarto", "Quinto",
                    "Sexto", "Séptimo", "Octavo", "Noveno", "Décimo", "Undécimo");
            model.addAttribute("grados", grados);

            List<String> jornadas = Arrays.asList("Mañana", "Tarde", "Sabatina", "Noche");
            model.addAttribute("jornadas", jornadas);

            List<String> motivos = Arrays.asList("Proceso de matrícula", "Atención al usuario");
            model.addAttribute("motivos", motivos);

            model.addAttribute("errorFecha",
                    "Para la fecha seleccionada, la institución «" + institucion.getNombre()
                            + "» ya alcanzó el máximo de 20 citas. Por favor, elige otro día.");

            return "tutor/agendar_cita";
        }

        // ===== Construir la cita en memoria (sin guardar todavía) =====
        Cita cita = new Cita();
        cita.setInstitucion(institucion);
        cita.setFechaCita(fecha);
        cita.setHoraCita(hora);
        cita.setCantidadCitas(cantidad);
        cita.setNombreAgenda(usuario.getNombre() + " " + usuario.getApellido());
        cita.setCorreoAgenda(usuario.getCorreo());
        cita.setTelefonoAgenda(usuario.getTelefono());
        cita.setEstado("Pendiente asistir");
        cita.setIdSede(1L); // de momento fijo
        cita.setGrado(grado);
        cita.setMotivo(motivo);

        // Enviar la cita a la sesión para el paso de confirmación
        model.addAttribute("citaPendiente", cita);

        // Datos adicionales para la vista de confirmación
        model.addAttribute("grado", grado);
        model.addAttribute("jornada", jornada);
        model.addAttribute("institucion", institucion);
        model.addAttribute("usuario", usuario);

        return "tutor/confirmar_cita";
    }

    /**
     * Guarda la cita almacenada en la sesión. Tras guardarla,
     * el usuario es redirigido al panel principal del tutor.
     */
    @PostMapping("/agendar-cita/confirmar")
    public String confirmarCita(
            @ModelAttribute("citaPendiente") Cita cita,
            HttpSession session) {

        citaRepository.save(cita);
        session.removeAttribute("citaPendiente");

        // Más adelante aquí se puede invocar el envío de correo
        // emailService.enviarConfirmacion(cita);

        return "redirect:/tutor/dashboard";
    }

    /**
     * Muestra todas las citas del usuario autenticado.
     * El filtro se realiza por el correo con el que se agendó la cita.
     */
    @GetMapping("/citas")
    public String listarCitas(Authentication authentication, Model model) {
        String correo = authentication.getName();
        List<Cita> citas = citaRepository.findByCorreoAgenda(correo);
        model.addAttribute("citas", citas);
        return "tutor/citas";
    }

    /**
     * Muestra un formulario para reprogramar una cita específica.
     */
    @GetMapping("/citas/{id}/reprogramar")
    public String mostrarReprogramar(@PathVariable Long id, Model model) {
        Cita cita = citaRepository.findById(id).orElseThrow();
        model.addAttribute("cita", cita);
        return "tutor/reprogramar_cita";
    }

    /**
     * Gestiona la reprogramación de una cita.
     * Solo se modifican la fecha y la hora.
     */
    @PostMapping("/citas/{id}/reprogramar")
    public String reprogramarCita(
            @PathVariable Long id,
            @RequestParam("fecha") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam("hora")  @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime hora) {

        Cita cita = citaRepository.findById(id).orElseThrow();
        cita.setFechaCita(fecha);
        cita.setHoraCita(hora);
        cita.setEstado("Reprogramada");
        citaRepository.save(cita);
        return "redirect:/tutor/citas";
    }

    /**
     * Cancela una cita estableciendo su estado como "Cancelada".
     * La cita no se elimina de la base de datos.
     */
    @PostMapping("/citas/{id}/cancelar")
    public String cancelarCita(@PathVariable Long id) {
        Cita cita = citaRepository.findById(id).orElseThrow();
        cita.setEstado("Cancelada");
        citaRepository.save(cita);
        return "redirect:/tutor/citas";
    }
}
