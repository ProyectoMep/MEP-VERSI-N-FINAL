package com.example.colegiosapp.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.colegiosapp.entity.Cita;

/**
 * Repositorio de la entidad Cita.
 * Incluye métodos de consulta para listar y contar citas
 * según correo, institución y fecha.
 */
@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {

    // Citas de un usuario (tutor) por correo
    List<Cita> findByCorreoAgenda(String correoAgenda);

    // Citas por institución
    List<Cita> findByInstitucionId(Long institucionId);

    // Citas por institución y estado
    List<Cita> findByInstitucionIdAndEstado(Long institucionId, String estado);

    // Citas por institución en un rango de fechas
    List<Cita> findByInstitucionIdAndFechaCitaBetween(Long institucionId,
                                                       LocalDate start,
                                                       LocalDate end);

    // ===== métodos para el límite de 20 citas por día =====

    /**
     * Cuenta cuántas citas hay en una fecha específica
     * (puede servir si el límite de 20 es global por día).
     */
    long countByFechaCita(LocalDate fechaCita);

    /**
     * Cuenta cuántas citas hay en una fecha específica
     * para una institución en particular.
     * Útil si el límite de 20 se aplica por colegio.
     */
    long countByInstitucionIdAndFechaCita(Long institucionId, LocalDate fechaCita);

    /**
     * Citas por institución y fecha exacta.
     * Se usa en el módulo Gestor para el filtro por día.
     */
    List<Cita> findByInstitucionIdAndFechaCita(Long institucionId, LocalDate fechaCita);
}
