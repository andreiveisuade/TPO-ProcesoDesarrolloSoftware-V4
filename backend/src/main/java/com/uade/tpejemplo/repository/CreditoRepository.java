package com.uade.tpejemplo.repository;

import com.uade.tpejemplo.model.credito.Credito;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface CreditoRepository extends JpaRepository<Credito, Long> {

    @Query("SELECT c FROM Credito c WHERE c.cliente.dni = :dni")
    List<Credito> buscarPorCliente(@Param("dni") String dni);

    // Bloquea el credito: cobrar y anular se serializan (siempre credito primero, despues cuota).
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Credito c WHERE c.id = :id")
    Optional<Credito> bloquearPorId(@Param("id") Long id);

    @Query("SELECT COALESCE(SUM(c.condiciones.deudaOriginal), 0) FROM Credito c WHERE c.anulado = false")
    BigDecimal sumarDeudaOriginalNoAnulada();
}
