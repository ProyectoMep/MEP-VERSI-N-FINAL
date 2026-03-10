package com.example.colegiosapp.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.colegiosapp.entity.Usuario;
import com.example.colegiosapp.service.UsuarioService;

import jakarta.validation.Valid;

/**
 * Controlador que maneja la página de inicio, el login, el registro y,
 * opcionalmente, la redirección posterior al inicio de sesión.
 */
@Controller
public class HomeController {

    private final UsuarioService usuarioService;

    public HomeController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    /** Muestra la página principal con enlaces a login y registro. */
    @GetMapping("/")
    public String index() {
        return "index";
    }

    /** Devuelve la plantilla de login personalizada. */
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    /** Muestra el formulario de registro de usuarios. */
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        if (!model.containsAttribute("usuario")) {
            model.addAttribute("usuario", new Usuario());
        }
        return "register";
    }

    /**
     * Procesa el registro desde el formulario público.
     * Por REGLA DE NEGOCIO, estos usuarios serán TUTORES.
     */
    @PostMapping("/register")
    @SuppressWarnings("CallToPrintStackTrace")
    public String register(
            @Valid @ModelAttribute("usuario") Usuario usuario,
            BindingResult result,
            Model model) {

        if (result.hasErrors()) {
            return "register";
        }

        try {
            usuario.setHabilitado(true);
            usuarioService.registerTutor(usuario);
        } catch (IllegalStateException e) {
            // Errores de negocio: correo/doc ya registrados o rol Tutor inexistente
            model.addAttribute("errorRegistro", e.getMessage());
            return "register";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute(
                    "errorRegistro",
                    "Ocurrió un error inesperado al registrar el usuario. " +
                    "Por favor, verifica los datos o inténtalo más tarde."
            );
            return "register";
        }

        model.addAttribute("registrationSuccess", true);
        return "login";
    }

    /**
     * Redirige al usuario autenticado a su panel según el rol asignado.
     */
    @RequestMapping("/postLogin")
    @SuppressWarnings("ConvertToStringSwitch")
    public String postLogin(Authentication authentication) {
        if (authentication == null) {
            return "redirect:/";
        }
        String authority = authentication.getAuthorities().iterator().next().getAuthority();
        if ("Tutor".equals(authority)) {
            return "redirect:/tutor/dashboard";
        } else if ("Administrador".equals(authority)) {
            return "redirect:/admin/dashboard";
        } else if ("Gestor".equals(authority)) {
            return "redirect:/gestor/dashboard";
        } else {
            return "redirect:/";
        }
    }
}
