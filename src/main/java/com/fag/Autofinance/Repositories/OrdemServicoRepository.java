package com.fag.Autofinance.repositories;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.fag.Autofinance.entities.OrdemServico;
import com.fag.Autofinance.enums.StatusOrdemServico;

public interface OrdemServicoRepository extends JpaRepository<OrdemServico, Long> {

    @Query("SELECT MAX(o.numero) FROM OrdemServico o WHERE o.empresa.id = :empresaId")
    Optional<Integer> findUltimoNumeroPorEmpresa(@Param("empresaId") UUID empresaId);

    @Query("""
                SELECT o FROM OrdemServico o
                WHERE o.empresa.id = :empresaId
                ORDER BY
                    CASE o.status
                        WHEN 'ATIVA' THEN 1
                        WHEN 'GERADA' THEN 2
                        WHEN 'INATIVA' THEN 3
                        ELSE 4
                    END
            """)
    List<OrdemServico> findByEmpresaIdOrderByStatusCustom(UUID empresaId);

    @Query("""
                SELECT o FROM OrdemServico o
                WHERE o.mecanico.username = :username
                  AND o.empresa.id = :empresaId
                ORDER BY
                    CASE o.status
                        WHEN 'ATIVA' THEN 1
                        WHEN 'GERADA' THEN 2
                        WHEN 'INATIVA' THEN 3
                        ELSE 4
                    END
            """)
    List<OrdemServico> findByMecanicoUsernameAndEmpresaIdOrderByStatusCustom(
            String username,
            UUID empresaId);

    Optional<OrdemServico> findByNumeroAndEmpresaId(Long numero, UUID empresaId);

    List<OrdemServico> findByMecanicoUsernameAndEmpresaId(String username, UUID empresaId);
}