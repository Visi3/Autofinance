package com.fag.Autofinance.repositories;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.fag.Autofinance.entities.Servico;
import com.fag.Autofinance.enums.StatusCadastros;
import java.util.List; // <--- Mudança: import List

public interface ServicoRepository extends JpaRepository<Servico, Long> {

    // <--- MUDANÇA: Sai Page<Servico> e Pageable, entra List<Servico>
    List<Servico> findByStatusAndEmpresaId(StatusCadastros status, UUID empresaId);

    // <--- MUDANÇA: Sai Page<Servico> e Pageable, entra List<Servico>
    List<Servico> findAllByEmpresaId(UUID empresaId);

    // <--- NOVO MÉTODO: Para validar 'criar' e 'atualizar' com nome exato
    // (ignorando maiúsculas)
    // Isso corrige a lógica de validação no seu service
    Optional<Servico> findByNomeIgnoreCaseAndEmpresaId(String nome, UUID empresaId);

    // (Mantido) Usado pela OrdemDeServicoService
    Optional<Servico> findByNomeContainingIgnoreCaseAndEmpresaId(String nome, UUID empresaId);

    // (Sem mudança)
    Optional<Servico> findByIdAndEmpresaId(Long id, UUID empresaId);
}
