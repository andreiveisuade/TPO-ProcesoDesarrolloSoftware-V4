package com.uade.tpejemplo.model;

import com.uade.tpejemplo.exception.BusinessException;
import com.uade.tpejemplo.model.interfaces.CalculoDeCuota;
import com.uade.tpejemplo.model.interfaces.ICredito;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

// Credito otorgado a un cliente: genera sus cuotas, calcula saldo y estado
// y decide si se puede anular.
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
    @Column(name = "deuda_original", nullable = false, precision = 12, scale = 2)
    private BigDecimal deudaOriginal;

    @NotNull
    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @NotNull
    @Column(name = "tasa_interes", nullable = false, precision = 5, scale = 2)
    private BigDecimal tasaInteres;

    // Derivado de la deuda, la tasa y la cantidad de cuotas. Se guarda para
    // que el credito conserve el importe con el que se otorgo aunque despues
    // cambie la forma de calcularlo.
    @NotNull
    @Column(name = "importe_cuota", nullable = false, precision = 12, scale = 2)
    private BigDecimal importeCuota;

    @Min(1)
    @Column(name = "cantidad_cuotas", nullable = false)
    private Integer cantidadCuotas;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_plan", nullable = false, columnDefinition = "varchar(20) default 'INTERES_SIMPLE'")
    private TipoPlan tipoPlan;

    // EnumSet: al otorgar, los cargos se aplican siempre en el orden del enum
    @ElementCollection
    @CollectionTable(name = "credito_cargos", joinColumns = @JoinColumn(name = "id_credito"))
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_cargo", nullable = false, length = 30)
    private Set<TipoCargo> cargos = EnumSet.noneOf(TipoCargo.class);

    @Column(name = "anulado", nullable = false)
    private boolean anulado = false;

    @Getter(AccessLevel.NONE)
    @OneToMany(mappedBy = "credito", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @OrderBy("numero")
    private List<Cuota> cuotas = new ArrayList<>();

    private Credito(Cliente cliente, BigDecimal deudaOriginal, LocalDate fecha,
                    BigDecimal tasaInteres, Integer cantidadCuotas, TipoPlan tipoPlan, Set<TipoCargo> cargos) {
        this.cliente = cliente;
        this.deudaOriginal = deudaOriginal;
        this.fecha = fecha;
        this.tasaInteres = tasaInteres;
        this.cantidadCuotas = cantidadCuotas;
        this.tipoPlan = tipoPlan;
        this.cargos.addAll(cargos);
        this.importeCuota = calculo().importeCuota(deudaOriginal, tasaInteres, cantidadCuotas);
        // Creator: el credito nace con su plan de cuotas
        generarPlanDeCuotas();
    }

    // Unica forma de dar de alta un credito. El id lo asigna la base, las
    // cuotas las genera el propio credito y el importe de cuota lo calcula
    // el, asi que ninguno de los tres se recibe desde afuera.
    public static Credito nuevo(Cliente cliente, BigDecimal deudaOriginal, LocalDate fecha,
                                BigDecimal tasaInteres, Integer cantidadCuotas, TipoPlan tipoPlan,
                                Set<TipoCargo> cargos) {
        return new Credito(cliente, deudaOriginal, fecha, tasaInteres, cantidadCuotas, tipoPlan, cargos);
    }

    // Strategy: el contexto delega el cálculo de la cuota en la estrategia elegida al otorgar (no cambia después)
    // Decorator: cada cargo del credito envuelve a la estrategia y le suma lo suyo
    private CalculoDeCuota calculo() {
        CalculoDeCuota calculo = tipoPlan.calculo();
        for (TipoCargo cargo : cargos) {
            calculo = cargo.aplicarSobre(calculo);
        }
        return calculo;
    }

    public BigDecimal totalADevolver() {
        return importeCuota.multiply(BigDecimal.valueOf(cantidadCuotas));
    }

    // Es una regla del credito, no del caso de uso que lo da de alta:
    // por eso vive en la entidad y no en el servicio.
    private void generarPlanDeCuotas() {
        for (int numeroCuota = 1; numeroCuota <= cantidadCuotas; numeroCuota++) {
            cuotas.add(new Cuota(this, numeroCuota, importeCuota, fecha.plusMonths(numeroCuota)));
        }
    }

    public Set<TipoCargo> getCargos() {
        return Collections.unmodifiableSet(cargos);
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

    public boolean estaCancelado() {
        return !cuotas.isEmpty() && cuotas.stream().allMatch(Cuota::estaPagada);
    }

    public boolean tieneCobranzas() {
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
