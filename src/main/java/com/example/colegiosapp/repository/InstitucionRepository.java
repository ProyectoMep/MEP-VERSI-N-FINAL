package com.example.colegiosapp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.colegiosapp.entity.Institucion;

/**
 * Buscar colegios por localidad y otras operaciones sobre la entidad Institucion.
 */
@Repository
public interface InstitucionRepository extends JpaRepository<Institucion, Long> {

    /**
     * Lista todas las instituciones de una localidad específica.
     */
    List<Institucion> findByLocalidad(String localidad);

    /**
     * Devuelve el listado de localidades distintas registradas.
     */
    @Query("SELECT DISTINCT i.localidad FROM Institucion i ORDER BY i.localidad")
    List<String> findAllLocalidades();

    /**
     * Usado por el servicio que consume el Web Service externo para
     * evitar duplicar instituciones cuando sincronizamos los colegios.
     */
    Optional<Institucion> findByNombreAndLocalidad(String nombre, String localidad);
}
