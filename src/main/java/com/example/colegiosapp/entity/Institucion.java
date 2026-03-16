package com.example.colegiosapp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "instituciones")
public class Institucion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id_Colegio")
    private Long id;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "localidad")
    private String localidad;

    @Column(name = "direccion_principal")
    private String direccionPrincipal;

    @Column(name = "correo")
    private String correo;

    @Column(name = "telefono")
    private String telefono;

    @Column(name = "cupos_por_grado")
    private String cuposPorGrado;

    @Column(name = "grados_academicos")
    private String gradosAcademicos;

    @Column(name = "jornadas")
    private String jornadas;

    @Column(name = "mision")
    private String mision;

    @Column(name = "vision")
    private String vision;

    @Column(name = "cantidad_sedes")
    private Integer cantidadSedes;

    // Campo nuevo para apoyar el dataset (PÚBLICO / PRIVADO)
    @Column(name = "sector")
    private String sector;

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getLocalidad() { return localidad; }
    public void setLocalidad(String localidad) { this.localidad = localidad; }

    public String getDireccionPrincipal() { return direccionPrincipal; }
    public void setDireccionPrincipal(String direccionPrincipal) { this.direccionPrincipal = direccionPrincipal; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getCuposPorGrado() { return cuposPorGrado; }
    public void setCuposPorGrado(String cuposPorGrado) { this.cuposPorGrado = cuposPorGrado; }

    public String getGradosAcademicos() { return gradosAcademicos; }
    public void setGradosAcademicos(String gradosAcademicos) { this.gradosAcademicos = gradosAcademicos; }

    public String getJornadas() { return jornadas; }
    public void setJornadas(String jornadas) { this.jornadas = jornadas; }

    public String getMision() { return mision; }
    public void setMision(String mision) { this.mision = mision; }

    public String getVision() { return vision; }
    public void setVision(String vision) { this.vision = vision; }

    public Integer getCantidadSedes() { return cantidadSedes; }
    public void setCantidadSedes(Integer cantidadSedes) { this.cantidadSedes = cantidadSedes; }

    public String getSector() { return sector; }
    public void setSector(String sector) { this.sector = sector; }
}
