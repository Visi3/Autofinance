package com.fag.Autofinance.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.fag.Autofinance.entities.Orcamento;

public interface OrcamentoRepository extends JpaRepository<Orcamento, Long> {

    @Query("""
                SELECT o FROM Orcamento o
                WHERE o.empresa.id = :empresaId
                ORDER BY
                    CASE o.status
                        WHEN 'ATIVO' THEN 1
                        WHEN 'GERADO' THEN 2
                        WHEN 'INATIVO' THEN 3
                        ELSE 4
                    END
            """)
    List<Orcamento> findByEmpresaIdOrderByStatusDesc(UUID empresaId);

    @Query("""
                SELECT o FROM Orcamento o
                WHERE o.mecanico.username = :username
                  AND o.empresa.id = :empresaId
                ORDER BY
                    CASE o.status
                        WHEN 'ATIVO' THEN 1
                        WHEN 'GERADO' THEN 2
                        WHEN 'INATIVO' THEN 3
                        ELSE 4
                    END
            """)
    List<Orcamento> findByMecanicoUsernameAndEmpresaIdOrderByStatusDesc(
            String username,
            UUID empresaId);

    Optional<Orcamento> findByNumeroAndEmpresaId(Long numeroOrcamento, UUID empresaId);

    Optional<Orcamento> findTopByEmpresaIdOrderByNumeroDesc(UUID empresaId);
}
