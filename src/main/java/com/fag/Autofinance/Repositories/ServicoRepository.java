package com.fag.Autofinance.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.fag.Autofinance.entities.Servico;
import com.fag.Autofinance.enums.StatusCadastros;

public interface ServicoRepository extends JpaRepository<Servico, Long> {

    Page<Servico> findByStatusAndEmpresaId(StatusCadastros status, UUID empresaId, Pageable pageable);

    Optional<Servico> findByNomeContainingIgnoreCaseAndEmpresaId(String nome, UUID empresaId);

    Page<Servico> findAllByEmpresaId(UUID empresaId, Pageable pageable);

    Optional<Servico> findByIdAndEmpresaId(Long id, UUID empresaId);

}
