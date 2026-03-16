package com.example.colegiosapp.config;

import com.example.colegiosapp.entity.Rol;
import com.example.colegiosapp.entity.Usuario;
import com.example.colegiosapp.repository.RolRepository;
import com.example.colegiosapp.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Crea usuarios de prueba para Administrador y Gestor al iniciar la aplicación.
 * Estas cuentas solo se crean si no existen previamente en la base de datos.
 */
@Component
public class AdminGestorInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminGestorInitializer(UsuarioRepository usuarioRepository,
                                RolRepository rolRepository,
                                PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Crea usuario administrador
        createUserIfNotExists(
            "admin@example.com",
            "Admin",
            "Test",
            "Administrador",
            "Cédula de Ciudadanía",
            "99999999",
            "3200000000",
            "AdminPass123"
        );

        // Crea usuario gestor
        createUserIfNotExists(
            "gestor@example.com",
            "Gestor",
            "Prueba",
            "Gestor",
            "Cédula de Ciudadanía",
            "88888888",
            "3200000001",
            "GestorPass123"
        );
    }

    private void createUserIfNotExists(String correo,
                                    String nombre,
                                    String apellido,
                                    String rolNombre,
                                    String tipoDocumento,
                                    String numeroDocumento,
                                    String telefono,
                                    String rawPassword) {
        // Si ya existe un usuario con ese correo, no hacemos nada
        if (usuarioRepository.findByCorreo(correo).isPresent()) {
            return;
        }

        Rol rol = rolRepository.findByNombre(rolNombre)
                .orElseThrow(() -> new IllegalStateException("Role not found: " + rolNombre));

        Usuario user = new Usuario();
        user.setCorreo(correo);
        user.setNombre(nombre);
        user.setApellido(apellido);
        user.setRol(rol);
        user.setTipoDocumento(tipoDocumento);
        user.setNumeroDocumento(numeroDocumento);
        user.setTelefono(telefono);
        user.setPassword(passwordEncoder.encode(rawPassword));

        usuarioRepository.save(user);
    }
}
