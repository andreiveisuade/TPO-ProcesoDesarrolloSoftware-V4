package com.uade.tpejemplo.model;

import com.uade.tpejemplo.exception.BusinessException;
import com.uade.tpejemplo.model.interfaces.ICredito;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

// Credito otorgado a un cliente: genera su plan de cuotas a partir de sus
// condiciones y lleva su ciclo de vida (estado, saldo y anulacion).
// Composite: suma la deuda de sus cuotas
@Entity
@Table(name = "creditos")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Credito implements ICredito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dni_cliente", nullable = false)
    private Cliente cliente;

    @NotNull
    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Valid
    @Embedded
    private CondicionesDeCredito condiciones;

    @Column(name = "anulado", nullable = false)
    private boolean anulado = false;

    @Getter(AccessLevel.NONE)
    @OneToMany(mappedBy = "credito", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @OrderBy("numero")
    private List<Cuota> cuotas = new ArrayList<>();

    private Credito(Cliente cliente, LocalDate fecha, CondicionesDeCredito condiciones) {
        this.cliente = cliente;
        this.fecha = fecha;
        this.condiciones = condiciones;
        // Creator: el credito nace con su plan de cuotas
        generarPlanDeCuotas();
    }

    // Unica forma de dar de alta un credito. El id lo asigna la base, las
    // cuotas las genera el propio credito y el importe de cada una sale de sus
    // condiciones, asi que ninguno de los tres se recibe desde afuera.
    public static Credito nuevo(Cliente cliente, LocalDate fecha, CondicionesDeCredito condiciones) {
        return new Credito(cliente, fecha, condiciones);
    }

    // Composite: el total, como el saldo, es la suma de sus cuotas
    public BigDecimal totalADevolver() {
        return sumar(Cuota::getImporte);
    }

    // Todas las cuotas valen lo mismo
    public BigDecimal importeCuota() {
        return cuotas.get(0).getImporte();
    }

    // Es una regla del credito, no del caso de uso que lo da de alta:
    // por eso vive en la entidad y no en el servicio.
    private void generarPlanDeCuotas() {
        // La cuota guarda lo que se le exige: es el hecho historico
        BigDecimal importeCuota = condiciones.importeCuota();
        for (int numeroCuota = 1; numeroCuota <= condiciones.getCantidadCuotas(); numeroCuota++) {
            cuotas.add(new Cuota(this, numeroCuota, importeCuota, fecha.plusMonths(numeroCuota)));
        }
    }

    public List<Cuota> getCuotas() {
        return Collections.unmodifiableList(cuotas);
    }

    // Information Expert: el credito calcula su estado, su saldo y su monto vencido
    public EstadoCredito estado() {
        if (anulado) {
            return EstadoCredito.ANULADO;
        }
        return estaCancelado() ? EstadoCredito.CANCELADO : EstadoCredito.VIGENTE;
    }

    public BigDecimal saldo() {
        return sumar(Cuota::saldo);
    }

    public BigDecimal montoVencido() {
        return sumar(Cuota::montoVencido);
    }

    private BigDecimal sumar(Function<Cuota, BigDecimal> monto) {
        return cuotas.stream()
            .map(monto)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private boolean estaCancelado() {
        return !cuotas.isEmpty() && cuotas.stream().allMatch(Cuota::estaPagada);
    }

    private boolean tieneCobranzas() {
        return cuotas.stream().anyMatch(Cuota::estaPagada);
    }

    // Information Expert: el credito decide si puede anularse
    public boolean puedeAnularse() {
        return !anulado && !tieneCobranzas();
    }

    public void anular() {
        if (anulado) {
            throw new BusinessException("El crédito " + id + " ya está anulado.");
        }
        if (tieneCobranzas()) {
            throw new BusinessException(
                "No se puede anular el crédito " + id + " porque tiene cobranzas registradas."
            );
        }
        this.anulado = true;
    }
}
