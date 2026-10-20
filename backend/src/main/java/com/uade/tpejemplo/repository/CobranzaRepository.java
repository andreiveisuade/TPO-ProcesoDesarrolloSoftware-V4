package com.uade.tpejemplo.repository;

import com.uade.tpejemplo.model.credito.Cobranza;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface CobranzaRepository extends JpaRepository<Cobranza, Long> {

    @Query("SELECT c FROM Cobranza c JOIN FETCH c.cuota WHERE c.cuota.credito.id = :idCredito")
    List<Cobranza> buscarPorCredito(@Param("idCredito") Long idCredito);

    @Query("SELECT COALESCE(SUM(c.importe), 0) FROM Cobranza c WHERE c.anulada = false")
    BigDecimal sumarImporteNoAnulado();
}
