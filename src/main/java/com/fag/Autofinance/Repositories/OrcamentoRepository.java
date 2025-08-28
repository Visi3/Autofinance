package com.fag.Autofinance.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fag.Autofinance.entities.Orcamento;

public interface OrcamentoRepository extends JpaRepository<Orcamento, Long> {
    List<Orcamento> findByMecanicoUsernameAndEmpresaId(String username, Long empresaId);

    List<Orcamento> findAllByEmpresaId(Long empresaId);

    Optional<Orcamento> findByIdAndEmpresaId(Long id, Long empresaId);
}
