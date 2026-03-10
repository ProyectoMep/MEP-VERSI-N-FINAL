package com.example.colegiosapp.service;

import java.util.Optional;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.colegiosapp.entity.Usuario;
import com.example.colegiosapp.repository.UsuarioRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public CustomUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Spring Security llamará este método cuando intente autenticar al usuario.
     * El "username" en nuestro caso es el correo.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // username == correo
        Optional<Usuario> optionalUsuario = usuarioRepository.findByCorreo(username);

        if (optionalUsuario.isEmpty()) {
            throw new UsernameNotFoundException("No se encontró un usuario con el correo: " + username);
        }

        Usuario usuario = optionalUsuario.get();

        // Como Usuario implementa UserDetails, lo devolvemos directamente
        return usuario;
    }
}
