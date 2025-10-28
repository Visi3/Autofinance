package com.fag.Autofinance.repositories;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.fag.Autofinance.entities.OrdemServico;
import com.fag.Autofinance.enums.StatusOrdemServico;

public interface OrdemServicoRepository extends JpaRepository<OrdemServico, UUID> {

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

        Optional<OrdemServico> findByNumeroAndMecanicoUsernameAndEmpresaId(Long numero, String username,
                        UUID empresaId);

        // --- PARA DASHBOARD ---

        long countByStatusInAndEmpresaId(List<StatusOrdemServico> statuses, UUID empresaId);

        long countByStatusInAndMecanicoUsernameAndEmpresaId(List<StatusOrdemServico> statuses, String username,
                        UUID empresaId);

        @Query("SELECT COALESCE(SUM(o.valor), 0.0) FROM OrdemServico o " +
                        "WHERE o.status = :status AND o.dataFinalizacao BETWEEN :inicio AND :fim " +
                        "AND o.empresa.id = :empresaId")
        BigDecimal sumValorByStatusAndDataFinalizacaoBetweenAndEmpresaId(
                        @Param("status") StatusOrdemServico status,
                        @Param("inicio") LocalDateTime inicio,
                        @Param("fim") LocalDateTime fim,
                        @Param("empresaId") UUID empresaId);

        @Query("SELECT COALESCE(SUM(o.valor), 0.0) FROM OrdemServico o " +
                        "WHERE o.status = :status AND o.dataFinalizacao BETWEEN :inicio AND :fim " +
                        "AND o.mecanico.username = :username AND o.empresa.id = :empresaId")
        BigDecimal sumValorByStatusAndDataFinalizacaoBetweenAndMecanicoUsernameAndEmpresaId(
                        @Param("status") StatusOrdemServico status,
                        @Param("inicio") LocalDateTime inicio,
                        @Param("fim") LocalDateTime fim,
                        @Param("username") String username,
                        @Param("empresaId") UUID empresaId);

        List<OrdemServico> findTop5ByStatusAndEmpresaIdOrderByDataFinalizacaoDesc(StatusOrdemServico status,
                        UUID empresaId);

        List<OrdemServico> findTop5ByStatusAndMecanicoUsernameAndEmpresaIdOrderByDataFinalizacaoDesc(
                        StatusOrdemServico status, String username, UUID empresaId);
}