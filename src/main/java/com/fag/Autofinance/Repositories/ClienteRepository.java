package com.fag.Autofinance.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fag.Autofinance.entities.Cliente;
import com.fag.Autofinance.enums.StatusCadastros;

public interface ClienteRepository extends JpaRepository<Cliente, String> {
    List<Cliente> findAllByEmpresaId(Long empresaId);

    Optional<Cliente> findByCpfCnpjAndEmpresaId(String cpfCnpj, Long empresaId);

    List<Cliente> findByStatusAndEmpresaId(StatusCadastros status, Long empresaId);

    boolean existsByCpfCnpjAndEmpresaId(String cpfCnpj, Long empresaId);
}