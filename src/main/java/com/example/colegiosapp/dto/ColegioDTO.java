package com.example.colegiosapp.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Representa un colegio devuelto por el Web Service externo.
 * Los nombres de @JsonProperty deben ajustarse a las columnas reales
 * del dataset que uses (datos abiertos de colegios).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ColegioDTO {

    // Ejemplo de campos típicos en datasets de colegios
    @JsonProperty("nombre_sede")       // ajusta al nombre real de la columna
    private String nombre;

    @JsonProperty("localidad")         // ajusta si el dataset usa otro nombre
    private String localidad;

    @JsonProperty("direccion")         // p.ej. "direccion" o "direccion_sede"
    private String direccion;

    @JsonProperty("telefono")          // p.ej. "telefono" o "telefono_sede"
    private String telefono;

    public ColegioDTO() {}

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getLocalidad() {
        return localidad;
    }

    public void setLocalidad(String localidad) {
        this.localidad = localidad;
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
}
