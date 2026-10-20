package com.uade.tpejemplo.repository;

import com.uade.tpejemplo.model.cliente.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// El DNI es la @Id de Cliente: findById y existsById buscan por DNI.
@Repository
public interface ClienteRepository extends JpaRepository<Cliente, String> {
}
