package com.example.colegiosapp.entity;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "citas")
public class Cita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cita")
    private Long id;

    @Column(name = "fecha_cita", nullable = false)
    private LocalDate fechaCita;

    @Column(name = "hora_cita", nullable = false)
    private LocalTime horaCita;

    @Column(name = "nombre_agenda", nullable = false)
    private String nombreAgenda;

    @Column(name = "correo_agenda", nullable = false)
    private String correoAgenda;

    @Column(name = "telefono_agenda", nullable = false)
    private String telefonoAgenda;

    @Column(name = "cantidad_citas", nullable = false)
    private Integer cantidadCitas;

    /**
     * Relación con la institución (colegio).
     * En BD la columna se llama id_colegio (BIGINT).
     */
    @ManyToOne
    @JoinColumn(name = "id_colegio")
    private Institucion institucion;

    @Column(name = "id_sede")
    private Long idSede;

    @Column(name = "estado", nullable = false)
    private String estado;

    /**
     * Grado académico (Primero, Segundo, ... Undécimo)
     */
    @Column(name = "grado")
    private String grado;

    /**
     * Motivo de la cita (Proceso de matrícula / Atención al usuario)
     */
    @Column(name = "motivo")
    private String motivo;

    // ====== Getters y Setters ======

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getFechaCita() {
        return fechaCita;
    }

    public void setFechaCita(LocalDate fechaCita) {
        this.fechaCita = fechaCita;
    }

    public LocalTime getHoraCita() {
        return horaCita;
    }

    public void setHoraCita(LocalTime horaCita) {
        this.horaCita = horaCita;
    }

    public String getNombreAgenda() {
        return nombreAgenda;
    }

    public void setNombreAgenda(String nombreAgenda) {
        this.nombreAgenda = nombreAgenda;
    }

    public String getCorreoAgenda() {
        return correoAgenda;
    }

    public void setCorreoAgenda(String correoAgenda) {
        this.correoAgenda = correoAgenda;
    }

    public String getTelefonoAgenda() {
        return telefonoAgenda;
    }

    public void setTelefonoAgenda(String telefonoAgenda) {
        this.telefonoAgenda = telefonoAgenda;
    }

    public Integer getCantidadCitas() {
        return cantidadCitas;
    }

    public void setCantidadCitas(Integer cantidadCitas) {
        this.cantidadCitas = cantidadCitas;
    }

    public Institucion getInstitucion() {
        return institucion;
    }

    public void setInstitucion(Institucion institucion) {
        this.institucion = institucion;
    }

    public Long getIdSede() {
        return idSede;
    }

    public void setIdSede(Long idSede) {
        this.idSede = idSede;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getGrado() {
        return grado;
    }

    public void setGrado(String grado) {
        this.grado = grado;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}
