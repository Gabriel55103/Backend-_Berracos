
package com.backend.berracos.models;

import com.backend.berracos.models.enums.EstadoReserva;
import com.backend.berracos.models.enums.TipoProducto;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "reservaciones")
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idReserva;

    @Column(nullable = false)
    private String servicio;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_producto")
    private TipoProducto tipoProducto = TipoProducto.SERVICIO;

    @Column(nullable = false)
    private String barbero;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false)
    private LocalTime hora;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoReserva estado = EstadoReserva.PENDIENTE;

    @Column(name = "asistio")
    private Boolean asistio;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_barbero", nullable = false)
    private Usuario barberoUsuario;
}
