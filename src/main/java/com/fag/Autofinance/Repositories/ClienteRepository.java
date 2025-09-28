package com.fag.Autofinance.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fag.Autofinance.entities.Cliente;
import com.fag.Autofinance.enums.StatusCadastros;

public interface ClienteRepository extends JpaRepository<Cliente, String> {
    List<Cliente> findAllByEmpresaId(UUID empresaId);

    Optional<Cliente> findByCpfCnpjAndEmpresaId(String cpfCnpj, UUID empresaId);

    List<Cliente> findByStatusAndEmpresaId(StatusCadastros status, UUID empresaId);

    boolean existsByCpfCnpjAndEmpresaId(String cpfCnpj, UUID empresaId);
}