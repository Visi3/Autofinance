package com.fag.Autofinance.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fag.Autofinance.entities.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, String> {
    Optional<Cliente> findByCpfCnpj(String cpfCnpj);
}