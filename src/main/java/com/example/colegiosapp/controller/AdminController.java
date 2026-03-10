package com.example.colegiosapp.controller;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.colegiosapp.entity.Rol;
import com.example.colegiosapp.entity.Usuario;
import com.example.colegiosapp.report.ReportService;
import com.example.colegiosapp.repository.CitaRepository;
import com.example.colegiosapp.repository.InstitucionRepository;
import com.example.colegiosapp.repository.RolRepository;
import com.example.colegiosapp.repository.UsuarioRepository;
import com.example.colegiosapp.service.ColegiosApiService;
import com.example.colegiosapp.util.ReportGenerator;

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

    /** GET: muestra formulario de creación */
    @GetMapping("/usuarios/nuevo")
    public String mostrarCrearUsuario(Model model) {

        model.addAttribute("instituciones", institucionRepository.findAll());
        model.addAttribute("roles", rolRepository.findAll());  // 🔥 importante

        return "admin/crear_usuario";
    }

    /** POST: procesa creación de usuario */
    @PostMapping("/usuarios/nuevo")
    public String crearUsuario(@RequestParam("nombre") String nombre,
                               @RequestParam("apellido") String apellido,
                               @RequestParam("correo") String correo,
                               @RequestParam("telefono") String telefono,
                               @RequestParam("password") String password,
                               @RequestParam("rolId") Integer rolId,
                               @RequestParam(value = "institucionId", required = false) Long institucionId,
                               Model model) {

        // Validar correo duplicado
        if (usuarioRepository.findByCorreo(correo).isPresent()) {
            model.addAttribute("error", "Ya existe un usuario con ese correo.");
            model.addAttribute("instituciones", institucionRepository.findAll());
            model.addAttribute("roles", rolRepository.findAll());
            return "admin/crear_usuario";
        }

        // Crear usuario
        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setApellido(apellido);
        usuario.setCorreo(correo);
        usuario.setTelefono(telefono);
        usuario.setPassword(passwordEncoder.encode(password));

        // Campos requeridos del entity
        usuario.setTipoDocumento("CC");                     // temporal
        usuario.setNumeroDocumento(correo);                 // temporal

        // Asignar rol
        Rol rol = rolRepository.findById(rolId)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));
        usuario.setRol(rol);

        // Asignar institución si es Gestor
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

    /* =================== OTROS MÉTODOS YA EXISTENTES =================== */
    // deja aquí el resto de tu código original (permisos, reportes, registro institución)
}
