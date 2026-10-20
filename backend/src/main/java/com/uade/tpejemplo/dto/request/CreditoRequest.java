package com.uade.tpejemplo.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import com.uade.tpejemplo.model.TipoCargo;
import com.uade.tpejemplo.model.TipoPlan;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@Data
public class CreditoRequest {

    @NotBlank(message = "El DNI del cliente es obligatorio")
    @Pattern(regexp = "\\d{7,8}", message = "El DNI debe tener 7 u 8 dígitos, sin puntos ni letras")
    private String dniCliente;

    @NotNull(message = "La deuda original es obligatoria")
    @Positive(message = "La deuda original debe ser mayor a cero")
    @Digits(integer = 8, fraction = 2, message = "La deuda admite hasta 8 enteros y 2 decimales")
    private BigDecimal deudaOriginal;

    @NotNull(message = "La fecha es obligatoria")
    private LocalDate fecha;

    // Interes simple: % total sobre el capital; sistema frances: % mensual.
    @NotNull(message = "La tasa de interes es obligatoria")
    @DecimalMin(value = "0", message = "La tasa de interes no puede ser negativa")
    @DecimalMax(value = "999.99", message = "La tasa de interes es demasiado alta")
    @Digits(integer = 3, fraction = 2, message = "La tasa admite hasta 2 decimales")
    private BigDecimal tasaInteres;

    @NotNull(message = "La cantidad de cuotas es obligatoria")
    @Min(value = 1, message = "Debe tener al menos 1 cuota")
    private Integer cantidadCuotas;

    @NotNull(message = "El tipo de plan es obligatorio")
    private TipoPlan tipoPlan;

    // Opcional: sin cargos si no viene o viene vacio.
    private Set<@NotNull TipoCargo> cargos;
}
