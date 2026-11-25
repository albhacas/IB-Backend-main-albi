package com.empresax.gestionflota.chofer.model;

import com.empresax.gestionflota.documentacion.model.DetalleDocumentacion;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Chofer")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Chofer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String dni;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String apellido;

    @Column(length = 25)
    private String cuil;

    @Column(length = 30)
    private String telefono;

    @Column(length = 100)
    private String email;

    private LocalDate fechaNacimiento;

    @Column(length = 255)
    private String domicilio;

    @Column(length = 100)
    private String localidad;

    @Column(length = 100)
    private String provincia;

    @Column(length = 500)
    private String fotoUrl;

    @Column(length = 50)
    private String ambitoHabilitacion;

    @Column(length = 100)
    private String turnoDisponibilidad;

    @Column(length = 100)
    private String estado;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    private String motivoBaja;
    private LocalDate fechaBaja;

    @OneToMany(mappedBy = "chofer", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    @Builder.Default
    private List<LicenciaChofer> licencias = new ArrayList<>();

    // Reemplazo: Certificado de Delitos Sexuales obligatorio
    @OneToOne(mappedBy = "chofer", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    private CertificadoDelitos certificadoDelitos;

    @OneToMany(mappedBy = "chofer", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference("chofer-documentacion")
    @Builder.Default
    private List<DetalleDocumentacion> documentaciones = new ArrayList<>();

    public void addLicencia(LicenciaChofer licencia) {
        licencias.add(licencia);
        licencia.setChofer(this);
    }

    public void setCertificadoDelitos(CertificadoDelitos certificadoDelitos) {
        this.certificadoDelitos = certificadoDelitos;
        if (certificadoDelitos != null) {
            certificadoDelitos.setChofer(this);
        }
    }

    public void addDocumentacion(DetalleDocumentacion documentacion) {
        if (documentacion == null) {
            throw new IllegalArgumentException("La documentación es obligatoria.");
        }
        documentaciones.add(documentacion);
        documentacion.asignarChofer(this);
    }
}