package com.fag.Autofinance.repositories;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.fag.Autofinance.entities.Agendamento;

@Repository
public interface AgendamentoRepository extends JpaRepository<Agendamento, UUID> {

        Page<Agendamento> findByOrdemServicoEmpresaId(UUID empresaId, Pageable pageable);

        Page<Agendamento> findByMecanicoUsernameAndOrdemServicoEmpresaId(
                        String username, UUID empresaId, Pageable pageable);

        List<Agendamento> findByOrdemServicoNumeroAndOrdemServicoEmpresaId(Integer numeroOrdem, UUID empresaId);

        Optional<Agendamento> findByIdAndOrdemServicoEmpresaId(UUID id, UUID empresaId);

        List<Agendamento> findByOrdemServicoClienteNomeContainingIgnoreCaseAndOrdemServicoEmpresaId(String nomeCliente,
                        UUID empresaId);

        List<Agendamento> findByDataAgendadaBetweenAndOrdemServicoEmpresaId(LocalDateTime inicio, LocalDateTime fim,
                        UUID empresaId);

        @Query("SELECT MAX(a.numero) FROM Agendamento a WHERE a.ordemServico.empresa.id = :empresaId")
        Optional<Integer> findUltimoNumeroPorEmpresa(@Param("empresaId") UUID empresaId);

        Optional<Agendamento> findByNumeroAndOrdemServicoEmpresaId(Integer numero, UUID empresaId);

}