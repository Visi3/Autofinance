package com.fag.Autofinance.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fag.Autofinance.entities.Orcamento;

public interface OrcamentoRepository extends JpaRepository<Orcamento, Long> {
    List<Orcamento> findByMecanicoUsername(String username);
}
