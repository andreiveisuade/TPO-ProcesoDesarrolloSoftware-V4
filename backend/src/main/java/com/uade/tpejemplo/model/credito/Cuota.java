package com.uade.tpejemplo.model.credito;

import com.uade.tpejemplo.exception.BusinessException;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// Cuota de un credito: sabe si esta pagada o vencida y registra su cobranza.
// Composite: hoja, contesta su propia deuda
@Entity
@Table(name = "cuotas", uniqueConstraints = @UniqueConstraint(columnNames = {"id_credito", "numero"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Cuota implements ICuota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_credito", nullable = false)
    private Credito credito;

    @Min(1)
    @Column(name = "numero", nullable = false)
    private Integer numero;

    @NotNull
    @Column(name = "importe", nullable = false, precision = 12, scale = 2)
    private BigDecimal importe;

    @NotNull
    @Column(name = "fecha_vencimiento", nullable = false)
    private LocalDate fechaVencimiento;

    // Sin getter: la cuota contesta estaPagada(), no entrega la lista.
    @Getter(AccessLevel.NONE)
    @OneToMany(mappedBy = "cuota", fetch = FetchType.LAZY)
    private List<Cobranza> cobranzas = new ArrayList<>();

    // Visible solo dentro del paquete model: una cuota no se crea suelta,
    // la crea el credito al generar su plan.
    Cuota(Credito credito, Integer numero, BigDecimal importe, LocalDate fechaVencimiento) {
        this.credito = credito;
        this.numero = numero;
        this.importe = importe;
        this.fechaVencimiento = fechaVencimiento;
    }

    // Information Expert: la cuota sabe si esta pagada (las cobranzas anuladas no cuentan)
    public boolean estaPagada() {
        return cobranzas.stream().anyMatch(cobranza -> !cobranza.isAnulada());
    }

    public boolean estaVencida() {
        return estaVencida(LocalDate.now());
    }

    // La cuota de un credito anulado ya no se debe: no entra en mora.
    public boolean estaVencida(LocalDate hoy) {
        return !credito.isAnulado() && !estaPagada() && fechaVencimiento.isBefore(hoy);
    }

    // La cuota de un credito anulado ya no se debe.
    public BigDecimal saldo() {
        return estaPagada() || credito.isAnulado() ? BigDecimal.ZERO : importe;
    }

    public BigDecimal montoVencido() {
        return estaVencida() ? importe : BigDecimal.ZERO;
    }

    // Creator: la cuota crea su cobranza (sabe si ya esta pagada y cuanto vale)
    public Cobranza registrarCobranza(BigDecimal importe) {
        if (credito.isAnulado()) {
            throw new BusinessException(
                "El crédito " + credito.getId() + " está anulado, no admite cobranzas"
            );
        }
        if (estaPagada()) {
            throw new BusinessException(
                "La cuota " + numero + " del crédito " + credito.getId() + " ya fue pagada"
            );
        }
        if (importe.compareTo(this.importe) != 0) {
            throw new BusinessException(
                "El importe " + importe + " no coincide con el de la cuota, " + this.importe
            );
        }
        Cobranza cobranza = Cobranza.registrar(this, importe);
        cobranzas.add(cobranza);
        return cobranza;
    }
}
