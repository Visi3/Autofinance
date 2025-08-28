package com.fag.Autofinance.repositories;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fag.Autofinance.entities.Veiculo;
import com.fag.Autofinance.enums.StatusCadastros;

public interface VeiculoRepository extends JpaRepository<Veiculo, String> {
    Page<Veiculo> findByStatusAndEmpresaId(StatusCadastros status, Long empresaId, Pageable pageable);

    Page<Veiculo> findByCliente_CpfCnpjAndEmpresaId(String cpfCnpj, Long empresaId, Pageable pageable);

    Page<Veiculo> findByCliente_NomeContainingIgnoreCaseAndEmpresaId(String nome, Long empresaId, Pageable pageable);

    Optional<Veiculo> findByPlacaAndEmpresaId(String placa, Long empresaId);

    boolean existsByPlacaAndEmpresaId(String placa, Long empresaId);

    Page<Veiculo> findAllByEmpresaId(Long empresaId, Pageable pageable);
}
