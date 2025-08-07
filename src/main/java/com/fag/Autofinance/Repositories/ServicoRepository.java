package com.fag.Autofinance.repositories;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.fag.Autofinance.entities.Servico;
import com.fag.Autofinance.enums.StatusCadastros;

public interface ServicoRepository extends JpaRepository<Servico, Long> {

    Page<Servico> findByStatus(StatusCadastros status, Pageable pageable);

    Optional<Servico> findByNomeContainingIgnoreCase(String nome);

}
