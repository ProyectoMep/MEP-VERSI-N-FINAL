package com.example.colegiosapp.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.colegiosapp.entity.Cita;

/**
 * Repositorio de la entidad Cita.
 * Incluye métodos de consulta para listar y contar citas
 * según correo, institución, fecha y mes.
 */
@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {

    // ── Existentes ──────────────────────────────────────────────────

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

    // Cuenta citas por fecha (límite global por día)
    long countByFechaCita(LocalDate fechaCita);

    // Cuenta citas por institución y fecha (límite por colegio)
    long countByInstitucionIdAndFechaCita(Long institucionId, LocalDate fechaCita);

    // Citas por institución y fecha exacta (módulo Gestor)
    List<Cita> findByInstitucionIdAndFechaCita(Long institucionId, LocalDate fechaCita);

    // ── Nuevos para filtro por mes en Reportes ───────────────────────

    /**
     * Todas las citas de un año y mes específico (sin importar institución ni estado).
     */
    @Query("SELECT c FROM Cita c WHERE YEAR(c.fechaCita) = :anio AND MONTH(c.fechaCita) = :mes")
    List<Cita> findByAnioAndMes(
            @Param("anio") int anio,
            @Param("mes")  int mes);

    /**
     * Citas de una institución en un año y mes específico.
     */
    @Query("SELECT c FROM Cita c " +
           "WHERE c.institucion.id = :institucionId " +
           "AND YEAR(c.fechaCita) = :anio " +
           "AND MONTH(c.fechaCita) = :mes")
    List<Cita> findByInstitucionIdAndAnioAndMes(
            @Param("institucionId") Long institucionId,
            @Param("anio")          int anio,
            @Param("mes")           int mes);

    /**
     * Citas de un estado en un año y mes específico.
     */
    @Query("SELECT c FROM Cita c " +
           "WHERE c.estado = :estado " +
           "AND YEAR(c.fechaCita) = :anio " +
           "AND MONTH(c.fechaCita) = :mes")
    List<Cita> findByEstadoAndAnioAndMes(
            @Param("estado") String estado,
            @Param("anio")   int anio,
            @Param("mes")    int mes);

    /**
     * Citas de una institución, estado, año y mes específicos
     * (combinación de todos los filtros).
     */
    @Query("SELECT c FROM Cita c " +
           "WHERE c.institucion.id = :institucionId " +
           "AND c.estado = :estado " +
           "AND YEAR(c.fechaCita) = :anio " +
           "AND MONTH(c.fechaCita) = :mes")
    List<Cita> findByInstitucionIdAndEstadoAndAnioAndMes(
            @Param("institucionId") Long institucionId,
            @Param("estado")        String estado,
            @Param("anio")          int anio,
            @Param("mes")           int mes);
}