package com.fag.Autofinance.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fag.Autofinance.entities.Veiculo;
import com.fag.Autofinance.enums.StatusCadastros;

public interface VeiculoRepository extends JpaRepository<Veiculo, String> {
    Page<Veiculo> findByStatusAndEmpresaId(StatusCadastros status, UUID empresaId, Pageable pageable);

    Page<Veiculo> findByCliente_CpfCnpjAndEmpresaId(String cpfCnpj, UUID empresaId, Pageable pageable);

    Page<Veiculo> findByCliente_NomeContainingIgnoreCaseAndEmpresaId(String nome, UUID empresaId, Pageable pageable);

    Optional<Veiculo> findByPlacaAndEmpresaId(String placa, UUID empresaId);

    boolean existsByPlacaAndEmpresaId(String placa, UUID empresaId);

    Page<Veiculo> findAllByEmpresaId(UUID empresaId, Pageable pageable);
}
