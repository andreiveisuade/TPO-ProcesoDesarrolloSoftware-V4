package com.uade.tpejemplo.repository;

import com.uade.tpejemplo.model.credito.Cuota;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CuotaRepository extends JpaRepository<Cuota, Long> {

    // Bloquea la fila: dos cobros de la misma cuota se serializan. Sin JOIN FETCH, para que las
    // cobranzas se lean despues del bloqueo y vean la que acaba de commitear el otro request.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT c FROM Cuota c
        WHERE c.credito.id = :idCredito AND c.numero = :numero
        """)
    Optional<Cuota> buscarPorCreditoYNumero(@Param("idCredito") Long idCredito, @Param("numero") Integer numero);
}
