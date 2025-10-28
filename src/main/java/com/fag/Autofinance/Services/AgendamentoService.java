package com.fag.Autofinance.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fag.Autofinance.dto.AgendamentoDTO;
import com.fag.Autofinance.entities.Agendamento;
import com.fag.Autofinance.entities.OrdemServico;
import com.fag.Autofinance.entities.Usuarios;
import com.fag.Autofinance.enums.RoleUsuario;
import com.fag.Autofinance.enums.StatusAgendamento;
import com.fag.Autofinance.enums.StatusCadastros;
import com.fag.Autofinance.enums.StatusOrdemServico;
import com.fag.Autofinance.exception.NaoEncontradoException;
import com.fag.Autofinance.exception.ValidarException;
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

    @Transactional
    public AgendamentoDTO criarAgendamento(Long numeroOrdem, LocalDateTime dataAgendada, String observacoes,
            String mecanicoUsername) {

        Usuarios usuarioLogado = getUsuarioLogado();
        UUID empresaId = usuarioLogado.getEmpresa().getId();

        if (dataAgendada == null) {
            throw new ValidarException("A data do agendamento é obrigatória.");
        }
        if (dataAgendada.isBefore(LocalDateTime.now())) {
            throw new ValidarException("Não é possível criar agendamentos para uma data no passado.");
        }

        OrdemServico ordem = ordemServicoRepository.findByNumeroAndEmpresaId(numeroOrdem, empresaId)
                .orElseThrow(() -> new NaoEncontradoException("Ordem de serviço não encontrada"));

        if (ordem.getStatus() == StatusOrdemServico.FINALIZADA || ordem.getStatus() == StatusOrdemServico.INATIVA) {
            throw new ValidarException(
                    "Não é possível criar agendamentos para uma Ordem de Serviço que está FINALIZADA ou INATIVA.");
        }

        Usuarios mecanicoAtribuido;
        if (mecanicoUsername != null && !mecanicoUsername.isBlank()) {
            if (usuarioLogado.getRole() != RoleUsuario.ADMIN && !mecanicoUsername.equals(usuarioLogado.getUsername())) {
                throw new ValidarException("Mecânicos só podem criar agendamentos para si mesmos.");
            }
            mecanicoAtribuido = usuarioRepository.findByUsername(mecanicoUsername)
                    .orElseThrow(
                            () -> new NaoEncontradoException("Mecânico '" + mecanicoUsername + "' não encontrado"));
            if (!mecanicoAtribuido.getEmpresa().getId().equals(empresaId)) {
                throw new ValidarException("Mecânico não pertence a esta empresa");
            }
            if (mecanicoAtribuido.getStatus() != StatusCadastros.ATIVO) {
                throw new ValidarException("Mecânico inativo não pode ser vinculado a um agendamento");
            }
        } else {
            mecanicoAtribuido = usuarioLogado;
        }

        Integer ultimoNumero = agendamentoRepository.findUltimoNumeroPorEmpresa(empresaId).orElse(0);

        Agendamento agendamento = new Agendamento();
        agendamento.setNumero(ultimoNumero + 1);
        agendamento.setOrdemServico(ordem);
        agendamento.setDataAgendada(dataAgendada);
        agendamento.setStatus(StatusAgendamento.AGENDADO);
        agendamento.setObservacoes(observacoes);
        agendamento.setMecanico(mecanicoAtribuido);

        Agendamento salvo = agendamentoRepository.save(agendamento);

        enviarMensagemCriacao(salvo);

        return new AgendamentoDTO(salvo);
    }

    @Transactional
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

            if (dto.getDataAgendada().isBefore(LocalDateTime.now())) {
                throw new ValidarException("Não é possível atualizar agendamentos para uma data no passado.");
            }

            agendamento.setDataAgendada(dto.getDataAgendada());
        }
        if (dto.getObservacoes() != null) {
            agendamento.setObservacoes(dto.getObservacoes());
        }
        if (dto.getStatus() != null) {
            agendamento.setStatus(dto.getStatus());
        }

        Agendamento salvo = agendamentoRepository.save(agendamento);

        enviarMensagemAtualizacao(salvo);

        return new AgendamentoDTO(salvo);
    }

    @Transactional
    public void finalizarPorOrdemServico(OrdemServico ordem) {
        List<Agendamento> agendamentos = agendamentoRepository
                .findByOrdemServicoNumeroAndOrdemServicoEmpresaId(ordem.getNumero(), ordem.getEmpresa().getId());

        if (!agendamentos.isEmpty()) {
            for (Agendamento agendamento : agendamentos) {
                agendamento.setStatus(StatusAgendamento.CONCLUIDO);
            }

            agendamentoRepository.saveAll(agendamentos);

        }
    }

    @Transactional
    public void excluirPorOrdemServico(OrdemServico ordem) {
        List<Agendamento> agendamentos = agendamentoRepository
                .findByOrdemServicoNumeroAndOrdemServicoEmpresaId(ordem.getNumero(), ordem.getEmpresa().getId());

        if (!agendamentos.isEmpty()) {
            agendamentoRepository.deleteAll(agendamentos);
            enviarMensagemCancelamento(ordem);
        }
    }

    public List<AgendamentoDTO> listarTodos() {
        Usuarios usuario = getUsuarioLogado();
        UUID empresaId = usuario.getEmpresa().getId();

        List<Agendamento> agendamentos;

        if (usuario.getRole() == RoleUsuario.ADMIN) {
            agendamentos = agendamentoRepository.findByOrdemServicoEmpresaId(empresaId);
        } else {
            agendamentos = agendamentoRepository
                    .findByMecanicoUsernameAndOrdemServicoEmpresaId(usuario.getUsername(), empresaId);
        }

        return agendamentos.stream()
                .map(AgendamentoDTO::new)
                .toList();
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

    // --- MÉTODOS HELPER ---

    private void enviarMensagemCriacao(Agendamento salvo) {
        try {
            String mensagem = String.format(
                    "Olá %s! Seu agendamento nº %d (ordem de serviço nº %d) foi marcado para %s com o mecânico %s.",
                    salvo.getOrdemServico().getCliente().getNome(),
                    salvo.getNumero(),
                    salvo.getOrdemServico().getNumero(),
                    salvo.getDataAgendada().format(DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm")),
                    salvo.getMecanico().getUsername());
            whatsAppService.enviarMensagem(salvo.getOrdemServico().getCliente().getCelular(), mensagem);
        } catch (Exception e) {
            System.err.println("Erro ao enviar mensagem de agendamento: " + e.getMessage());
        }
    }

    private void enviarMensagemAtualizacao(Agendamento salvo) {
        try {
            String msg = String.format(
                    "Olá %s! O seu agendamento nº %d (ordem de serviço nº %d) foi atualizado:\n" +
                            "- Data: %s\n" +
                            "- Status: %s\s" +
                            "- Mecânico: %s\n" +
                            "- Observações: %s",
                    salvo.getOrdemServico().getCliente().getNome(),
                    salvo.getNumero(),
                    salvo.getOrdemServico().getNumero(),
                    salvo.getDataAgendada().format(DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm")),
                    salvo.getStatus(),
                    salvo.getMecanico().getUsername(),
                    salvo.getObservacoes() != null ? salvo.getObservacoes() : "Nenhuma");
            whatsAppService.enviarMensagem(salvo.getOrdemServico().getCliente().getCelular(), msg);
        } catch (Exception e) {
            System.err.println("Erro ao enviar mensagem de alteração de agendamento: " + e.getMessage());
        }
    }

    private void enviarMensagemCancelamento(OrdemServico ordem) {
        try {
            String msg = String.format(
                    "Olá %s! Todos os agendamentos da ordem de serviço nº %d foram cancelados (OS Inativada).",
                    ordem.getCliente().getNome(),
                    ordem.getNumero());
            whatsAppService.enviarMensagem(ordem.getCliente().getCelular(), msg);
        } catch (Exception e) {
            System.err.println("Erro ao enviar mensagem de cancelamento: " + e.getMessage());
        }
    }
}