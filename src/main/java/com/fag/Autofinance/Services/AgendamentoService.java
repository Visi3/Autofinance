package com.fag.Autofinance.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.fag.Autofinance.dto.AgendamentoDTO;
import com.fag.Autofinance.entities.Agendamento;
import com.fag.Autofinance.entities.OrdemServico;
import com.fag.Autofinance.entities.Usuarios;
import com.fag.Autofinance.enums.RoleUsuario;
import com.fag.Autofinance.enums.StatusAgendamento;
import com.fag.Autofinance.enums.StatusOrdemServico;
import com.fag.Autofinance.exception.EnviarException;
import com.fag.Autofinance.exception.NaoEncontradoException;
import com.fag.Autofinance.repositories.AgendamentoRepository;
import com.fag.Autofinance.repositories.OrdemServicoRepository;
import com.fag.Autofinance.repositories.UsuarioRepository;

@Service
public class AgendamentoService {

    private final AgendamentoRepository agendamentoRepository;
    private final OrdemServicoRepository ordemServicoRepository;
    private final UsuarioRepository usuarioRepository;
    private final WhatsAppService whatsAppService;

    public AgendamentoService(
            AgendamentoRepository agendamentoRepository,
            OrdemServicoRepository ordemServicoRepository,
            UsuarioRepository usuarioRepository,
            WhatsAppService whatsAppService) {
        this.agendamentoRepository = agendamentoRepository;
        this.ordemServicoRepository = ordemServicoRepository;
        this.usuarioRepository = usuarioRepository;
        this.whatsAppService = whatsAppService;
    }

    private Usuarios getUsuarioLogado() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new NaoEncontradoException("Usuário autenticado não encontrado"));
    }

    public AgendamentoDTO criarAgendamento(Long numeroOrdem, LocalDateTime dataAgendada, String observacoes) {
        Usuarios usuario = getUsuarioLogado();
        UUID empresaId = usuario.getEmpresa().getId();

        OrdemServico ordem = ordemServicoRepository.findByNumeroAndEmpresaId(numeroOrdem, empresaId)
                .orElseThrow(() -> new NaoEncontradoException("Ordem de serviço não encontrada"));

        Integer ultimoNumero = agendamentoRepository.findUltimoNumeroPorEmpresa(empresaId).orElse(0);

        Agendamento agendamento = new Agendamento();
        agendamento.setNumero(ultimoNumero + 1);
        agendamento.setOrdemServico(ordem);
        agendamento.setDataAgendada(dataAgendada);
        agendamento.setStatus(StatusAgendamento.AGENDADO);
        agendamento.setObservacoes(observacoes);
        agendamento.setMecanico(usuario);

        Agendamento salvo = agendamentoRepository.save(agendamento);

        try {
            String mensagem = String.format(
                    "Olá %s! Seu agendamento nº %d (ordem de serviço nº %d) foi marcado para %s.",
                    ordem.getCliente().getNome(),
                    salvo.getNumero(),
                    ordem.getNumero(),
                    dataAgendada.format(DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm")));
            whatsAppService.enviarMensagem(ordem.getCliente().getCelular(), mensagem);
        } catch (EnviarException e) {
            System.err.println("Erro ao enviar mensagem de agendamento: " + e.getMessage());
        }

        return new AgendamentoDTO(salvo);
    }

    public AgendamentoDTO atualizarAgendamento(Integer numero, AgendamentoDTO dto) {
        Usuarios usuario = getUsuarioLogado();
        UUID empresaId = usuario.getEmpresa().getId();

        Agendamento agendamento = agendamentoRepository
                .findByNumeroAndOrdemServicoEmpresaId(numero, empresaId)
                .orElseThrow(() -> new NaoEncontradoException("Agendamento não encontrado"));

        if (usuario.getRole() != RoleUsuario.ADMIN &&
                !agendamento.getMecanico().getUsername().equals(usuario.getUsername())) {
            throw new NaoEncontradoException("Agendamento não pertence a este usuário");
        }

        if (dto.getDataAgendada() != null) {
            agendamento.setDataAgendada(dto.getDataAgendada());
        }

        if (dto.getObservacoes() != null) {
            agendamento.setObservacoes(dto.getObservacoes());
        }

        if (dto.getStatus() != null) {
            agendamento.setStatus(dto.getStatus());
        }

        Agendamento salvo = agendamentoRepository.save(agendamento);
        try {
            String msg = String.format(
                    "Olá %s! O seu agendamento nº %d (ordem de serviço nº %d) foi atualizado:\n" +
                            "- Data: %s\n" +
                            "- Status: %s\n" +
                            "- Observações: %s",
                    agendamento.getOrdemServico().getCliente().getNome(),
                    agendamento.getNumero(),
                    agendamento.getOrdemServico().getNumero(),
                    agendamento.getDataAgendada().format(DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm")),
                    agendamento.getStatus(),
                    agendamento.getObservacoes() != null ? agendamento.getObservacoes() : "Nenhuma");
            whatsAppService.enviarMensagem(agendamento.getOrdemServico().getCliente().getCelular(), msg);
        } catch (EnviarException e) {
            System.err.println("Erro ao enviar mensagem de alteração de agendamento: " + e.getMessage());
        }

        return new AgendamentoDTO(salvo);
    }

    public void finalizarPorOrdemServico(OrdemServico ordem) {
        Usuarios usuario = getUsuarioLogado();
        UUID empresaId = usuario.getEmpresa().getId();

        List<Agendamento> agendamentos = agendamentoRepository
                .findByOrdemServicoNumeroAndOrdemServicoEmpresaId(ordem.getNumero(), empresaId);

        if (!agendamentos.isEmpty()) {
            for (Agendamento agendamento : agendamentos) {
                agendamento.setStatus(StatusAgendamento.CONCLUIDO);
                agendamentoRepository.save(agendamento);
            }

            try {
                String msg = String.format(
                        "Olá %s! Sua ordem de serviço nº %d foi finalizada. Agradecemos pela confiança!",
                        ordem.getCliente().getNome(),
                        ordem.getNumero());
                whatsAppService.enviarMensagem(ordem.getCliente().getCelular(), msg);
            } catch (EnviarException e) {
                System.err.println("Erro ao enviar mensagem de finalização: " + e.getMessage());
            }
        }
    }

    public void excluirPorOrdemServico(OrdemServico ordem) {
        Usuarios usuario = getUsuarioLogado();
        UUID empresaId = usuario.getEmpresa().getId();

        List<Agendamento> agendamentos = agendamentoRepository
                .findByOrdemServicoNumeroAndOrdemServicoEmpresaId(ordem.getNumero(), empresaId);

        if (!agendamentos.isEmpty()) {
            agendamentoRepository.deleteAll(agendamentos);

            try {
                String msg = String.format(
                        "Olá %s! Todos os agendamentos da ordem de serviço nº %d foram cancelados.",
                        ordem.getCliente().getNome(),
                        ordem.getNumero());
                whatsAppService.enviarMensagem(ordem.getCliente().getCelular(), msg);
            } catch (EnviarException e) {
                System.err.println("Erro ao enviar mensagem de cancelamento: " + e.getMessage());
            }
        }
    }

    public Page<AgendamentoDTO> listarTodos(Pageable pageable) {
        Usuarios usuario = getUsuarioLogado();
        UUID empresaId = usuario.getEmpresa().getId();

        if (usuario.getRole() == RoleUsuario.ADMIN) {
            return agendamentoRepository.findByOrdemServicoEmpresaId(empresaId, pageable)
                    .map(AgendamentoDTO::new);
        } else {
            return agendamentoRepository
                    .findByMecanicoUsernameAndOrdemServicoEmpresaId(usuario.getUsername(), empresaId, pageable)
                    .map(AgendamentoDTO::new);
        }
    }

    public List<AgendamentoDTO> listarPorCliente(String nomeCliente) {
        UUID empresaId = getUsuarioLogado().getEmpresa().getId();

        List<Agendamento> agendamentos = agendamentoRepository
                .findByOrdemServicoClienteNomeContainingIgnoreCaseAndOrdemServicoEmpresaId(nomeCliente, empresaId);

        return agendamentos.stream()
                .map(AgendamentoDTO::new)
                .toList();
    }

    public List<AgendamentoDTO> listarPorData(LocalDate data) {
        UUID empresaId = getUsuarioLogado().getEmpresa().getId();

        List<Agendamento> agendamentos = agendamentoRepository
                .findByDataAgendadaBetweenAndOrdemServicoEmpresaId(
                        data.atStartOfDay(),
                        data.plusDays(1).atStartOfDay(),
                        empresaId);

        return agendamentos.stream()
                .map(AgendamentoDTO::new)
                .toList();
    }
}