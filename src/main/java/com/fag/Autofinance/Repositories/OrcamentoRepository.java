package com.fag.Autofinance.repositories;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.fag.Autofinance.entities.Orcamento;

public interface OrcamentoRepository extends JpaRepository<Orcamento, Long> {

    Page<Orcamento> findByMecanicoUsernameAndEmpresaId(String username, UUID empresaId, Pageable pageable);

    Page<Orcamento> findByEmpresaId(UUID empresaId, Pageable pageable);

    Optional<Orcamento> findByNumeroAndEmpresaId(Long numeroOrcamento, UUID empresaId);

    Optional<Orcamento> findTopByEmpresaIdOrderByNumeroDesc(UUID empresaId);
}