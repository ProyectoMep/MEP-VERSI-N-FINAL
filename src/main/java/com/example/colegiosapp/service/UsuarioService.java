package com.example.colegiosapp.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.colegiosapp.entity.Rol;
import com.example.colegiosapp.entity.Usuario;
import com.example.colegiosapp.repository.UsuarioRepository;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registro desde register.html → SIEMPRE debe ser TUTOR (id_rol = 3)
     */
    @Transactional
    public Usuario registerTutor(Usuario usuario) {

        if (usuarioRepository.existsByCorreo(usuario.getCorreo())) {
            throw new IllegalStateException("El correo ya se encuentra registrado");
        }

        if (usuarioRepository.existsByNumeroDocumento(usuario.getNumeroDocumento())) {
            throw new IllegalStateException("El número de documento ya se encuentra registrado");
        }

        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));

        // ASIGNACIÓN DIRECTA DEL ID DEL ROL
        Rol rolTutor = new Rol();
        rolTutor.setId(3); // id del rol Tutor en BD

        usuario.setRol(rolTutor);
        usuario.setHabilitado(true);

        return usuarioRepository.save(usuario);
    }

    /**
     * Método para que el ADMIN cree usuarios con diferentes roles según ID
     * Roles:
     * 1 = Administrador
     * 2 = Gestor
     * 3 = Tutor
     */
    @Transactional
    public Usuario registerWithRoleId(Usuario usuario, int idRol) {

        if (usuarioRepository.existsByCorreo(usuario.getCorreo())) {
            throw new IllegalStateException("El correo ya se encuentra registrado");
        }

        if (usuarioRepository.existsByNumeroDocumento(usuario.getNumeroDocumento())) {
            throw new IllegalStateException("El número de documento ya se encuentra registrado");
        }

        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));

        Rol rol = new Rol();
        rol.setId(idRol); // asignación directa

        usuario.setRol(rol);
        usuario.setHabilitado(true);

        return usuarioRepository.save(usuario);
    }
}
