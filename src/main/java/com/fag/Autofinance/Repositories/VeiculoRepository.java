package com.fag.Autofinance.repositories;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fag.Autofinance.entities.Veiculo;
import com.fag.Autofinance.enums.StatusCadastros;

public interface VeiculoRepository extends JpaRepository<Veiculo, String> {
    Page<Veiculo> findByStatus(StatusCadastros status, Pageable pageable);

    Page<Veiculo> findByCliente_CpfCnpj(String cpfCnpj, Pageable pageable);

    Page<Veiculo> findByCliente_NomeContainingIgnoreCase(String nome, Pageable pageable);

    Optional<Veiculo> findByPlaca(String placa);
}
