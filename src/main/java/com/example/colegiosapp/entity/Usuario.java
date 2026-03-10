package com.example.colegiosapp.entity;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(
        name = "usuario",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "correo"),
                @UniqueConstraint(columnNames = "numero_documento")
        }
)
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long id;

    @NotBlank
    @Column(name = "tipo_documento", nullable = false)
    private String tipoDocumento;

    @NotBlank
    @Column(name = "numero_documento", nullable = false)
    private String numeroDocumento;

    @NotBlank
    @Column(name = "nombre", nullable = false)
    private String nombre;

    @NotBlank
    @Column(name = "apellido", nullable = false)
    private String apellido;

    @NotBlank
    @Column(name = "telefono", nullable = false)
    private String telefono;

    @Email
    @NotBlank
    @Column(name = "correo", nullable = false, unique = true)
    private String correo;

    /**
     * Contraseña cifrada en BCrypt
     */
    @NotBlank
    @Size(min = 8)
    @Column(name = "contrasena", nullable = false)
    private String password;

    @ManyToOne
    @JoinColumn(name = "id_rol")
    private Rol rol;

    /**
     * Relación con institución (por ahora solo guardamos el id).
     * Columna existente en la BD: id_institucion (BIGINT).
     */
    @Column(name = "id_institucion")
    private Long idInstitucion;

    /**
     * Para activar o desactivar cuentas sin eliminar usuario.
     * Columna creada en BD: habilitado (TINYINT(1) NOT NULL DEFAULT 1).
     */
    @Column(name = "habilitado", nullable = false)
    private Boolean habilitado = true;

    public Usuario() {}

    // =====================================
    // Getters y Setters
    // =====================================

    public Long getId() {
        return id;
    }

    public void setId(Long id) { this.id = id; }

    public String getTipoDocumento() { return tipoDocumento; }

    public void setTipoDocumento(String tipoDocumento) { this.tipoDocumento = tipoDocumento; }

    public String getNumeroDocumento() { return numeroDocumento; }

    public void setNumeroDocumento(String numeroDocumento) { this.numeroDocumento = numeroDocumento; }

    public String getNombre() { return nombre; }

    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }

    public void setApellido(String apellido) { this.apellido = apellido; }

    public String getTelefono() { return telefono; }

    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getCorreo() { return correo; }

    public void setCorreo(String correo) { this.correo = correo; }

    @Override
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) { this.password = password; }

    public Rol getRol() { return rol; }

    public void setRol(Rol rol) { this.rol = rol; }

    public Long getIdInstitucion() {
        return idInstitucion;
    }

    public void setIdInstitucion(Long idInstitucion) {
        this.idInstitucion = idInstitucion;
    }

    public Boolean getHabilitado() { return habilitado; }

    public void setHabilitado(Boolean habilitado) { this.habilitado = habilitado; }

    // =====================================
    // Implementación de UserDetails
    // =====================================

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Nombre del rol tal como está en la BD (ej: "Tutor", "Administrador")
        return Collections.singleton(
                new SimpleGrantedAuthority(rol.getNombre())
        );
    }

    @Override
    public String getUsername() {
        // El correo será el "username" para login
        return this.correo;
    }

    /**
     * Si habilitado es null (algún registro viejo), lo tratamos como true
     * para no bloquear usuarios antiguos.
     */
    private boolean isEnabledInternal() {
        return habilitado == null || Boolean.TRUE.equals(habilitado);
    }

    @Override
    public boolean isAccountNonExpired() {
        return isEnabledInternal();
    }

    @Override
    public boolean isAccountNonLocked() {
        return isEnabledInternal();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return isEnabledInternal();
    }

    @Override
    public boolean isEnabled() {
        return isEnabledInternal();
    }
}
