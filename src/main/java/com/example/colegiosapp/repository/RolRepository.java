package com.example.colegiosapp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.colegiosapp.entity.Rol;

@Repository
public interface RolRepository extends JpaRepository<Rol, Integer> {

    // Buscar rol por nombre: ROLE_ADMIN, ROLE_GESTOR, ROLE_USUARIO
    Optional<Rol> findByNombre(String nombre);

    // Verificar existencia del rol (útil para inicializar datos o validación)
    boolean existsByNombre(String nombre);
}
