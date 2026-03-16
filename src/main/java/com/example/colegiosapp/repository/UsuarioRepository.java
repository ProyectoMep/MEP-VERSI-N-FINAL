package com.example.colegiosapp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.colegiosapp.entity.Usuario;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Buscar usuario por correo (login)
    Optional<Usuario> findByCorreo(String correo);

    // Validar si ya existe un correo registrado
    boolean existsByCorreo(String correo);

    // Validar si ya existe un documento registrado
    boolean existsByNumeroDocumento(String numeroDocumento);
}
