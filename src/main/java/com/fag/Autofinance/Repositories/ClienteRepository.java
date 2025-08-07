package com.fag.Autofinance.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fag.Autofinance.entities.Cliente;
import com.fag.Autofinance.enums.StatusCadastros;

public interface ClienteRepository extends JpaRepository<Cliente, String> {
    Optional<Cliente> findByCpfCnpj(String cpfCnpj);

    boolean existsByCpfCnpj(String cpfCnpj);

    List<Cliente> findByStatus(StatusCadastros status);
}