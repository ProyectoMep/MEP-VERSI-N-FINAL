package com.example.colegiosapp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.colegiosapp.entity.Estudiante;

public interface EstudianteRepository extends JpaRepository<Estudiante, Long> {

    // Listar todos los estudiantes por estado (Pendiente, Matriculado, etc.)
    List<Estudiante> findByEstado(String estado);

    // Búsqueda por nombre (para el filtro en la interfaz)
    List<Estudiante> findByEstadoAndNombreContainingIgnoreCase(String estado, String nombre);

    // Búsqueda por número de documento (para el filtro en la interfaz)
    List<Estudiante> findByEstadoAndNumeroDocumentoContainingIgnoreCase(String estado, String numeroDocumento);
}
