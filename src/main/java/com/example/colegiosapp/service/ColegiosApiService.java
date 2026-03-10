package com.example.colegiosapp.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.example.colegiosapp.entity.Institucion;
import com.example.colegiosapp.repository.InstitucionRepository;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@Service
public class ColegiosApiService {

    private static final Logger log = LoggerFactory.getLogger(ColegiosApiService.class);

    private static final String API_URL =
            "https://www.datos.gov.co/resource/up8f-n2jf.json?$limit=5000";

    /**
     * Lista oficial de localidades de Bogotá (para asignación aleatoria).
     */
    private static final List<String> LOCALIDADES = Arrays.asList(
            "Usaquén", "Chapinero", "Santa Fe", "San Cristóbal", "Usme",
            "Tunjuelito", "Bosa", "Kennedy", "Fontibón", "Engativá",
            "Suba", "Barrios Unidos", "Teusaquillo", "Los Mártires",
            "Antonio Nariño", "Puente Aranda", "La Candelaria",
            "Rafael Uribe Uribe", "Ciudad Bolívar", "Sumapaz"
    );

    private final RestTemplate restTemplate;
    private final InstitucionRepository institucionRepository;
    private final Random random = new Random();

    public ColegiosApiService(RestTemplate restTemplate,
                              InstitucionRepository institucionRepository) {
        this.restTemplate = restTemplate;
        this.institucionRepository = institucionRepository;
    }

    /**
     * Consume el dataset de colegios.
     */
    private List<ColegioDTO> obtenerColegiosDesdeAPI() {
        try {
            ColegioDTO[] respuesta = restTemplate.getForObject(API_URL, ColegioDTO[].class);
            if (respuesta == null) {
                log.warn("El API respondió sin datos.");
                return Collections.emptyList();
            }
            log.info("Se recibieron {} colegios desde el API.", respuesta.length);
            return Arrays.asList(respuesta);

        } catch (RestClientException e) {
            log.error("Error al consumir el API: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Sincronización principal.
     */
    public int sincronizarColegios() {

        List<ColegioDTO> colegios = obtenerColegiosDesdeAPI();
        int contador = 0;

        for (ColegioDTO dto : colegios) {

            String nombre = dto.getNombreEstablecimiento();
            if (nombre == null || nombre.isBlank()) continue;

            // 🎯 LOCALIDAD ASIGNADA COMPLETAMENTE AL AZAR
            String localidadAsignada = LOCALIDADES.get(random.nextInt(LOCALIDADES.size()));

            Institucion institucion = institucionRepository
                    .findByNombreAndLocalidad(nombre, localidadAsignada)
                    .orElseGet(Institucion::new);

            institucion.setNombre(nombre);
            institucion.setLocalidad(localidadAsignada);
            institucion.setDireccionPrincipal(dto.getDireccion());
            institucion.setTelefono(dto.getTelefono());
            institucion.setCorreo(dto.getCorreoElectronico());

            institucionRepository.save(institucion);
            contador++;
        }

        log.info("Sincronización completa: {} instituciones procesadas.", contador);
        return contador;
    }

    // ========== DTO INTERNO ==========

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ColegioDTO {

        @JsonProperty("nombreestablecimiento")
        private String nombreEstablecimiento;

        @JsonProperty("direccion")
        private String direccion;

        @JsonProperty("telefono")
        private String telefono;

        @JsonProperty("correo_electronico")
        private String correoElectronico;

        @JsonProperty("nombre_localidad")
        private String nombreLocalidad;

        public String getNombreEstablecimiento() {
            return nombreEstablecimiento;
        }

        public void setNombreEstablecimiento(String nombreEstablecimiento) {
            this.nombreEstablecimiento = nombreEstablecimiento;
        }

        public String getDireccion() {
            return direccion;
        }

        public void setDireccion(String direccion) {
            this.direccion = direccion;
        }

        public String getTelefono() {
            return telefono;
        }

        public void setTelefono(String telefono) {
            this.telefono = telefono;
        }

        public String getCorreoElectronico() {
            return correoElectronico;
        }

        public void setCorreoElectronico(String correoElectronico) {
            this.correoElectronico = correoElectronico;
        }

        public String getNombreLocalidad() {
            return nombreLocalidad;
        }

        public void setNombreLocalidad(String nombreLocalidad) {
            this.nombreLocalidad = nombreLocalidad;
        }
    }
}
