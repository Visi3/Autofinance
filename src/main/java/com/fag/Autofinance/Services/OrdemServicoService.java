package com.fag.Autofinance.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.TimerTask;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.fag.Autofinance.dto.OrdemServicoDTO;
import com.fag.Autofinance.entities.Cliente;
import com.fag.Autofinance.entities.Empresa;
import com.fag.Autofinance.entities.Orcamento;
import com.fag.Autofinance.entities.OrdemServico;
import com.fag.Autofinance.entities.Servico;
import com.fag.Autofinance.entities.Usuarios;
import com.fag.Autofinance.entities.Veiculo;
import com.fag.Autofinance.enums.RoleUsuario;
import com.fag.Autofinance.enums.StatusOrcamento;
import com.fag.Autofinance.enums.StatusOrdemServico;
import com.fag.Autofinance.exception.EnviarException;
import com.fag.Autofinance.exception.NaoEncontradoException;
import com.fag.Autofinance.repositories.ClienteRepository;
import com.fag.Autofinance.repositories.OrcamentoRepository;
import com.fag.Autofinance.repositories.OrdemServicoRepository;
import com.fag.Autofinance.repositories.ServicoRepository;
import com.fag.Autofinance.repositories.UsuarioRepository;
import com.fag.Autofinance.repositories.VeiculoRepository;

import jakarta.transaction.Transactional;

@Service

public class OrdemServicoService {

    private final TaskScheduler taskScheduler;
    private final OrdemServicoRepository ordemServicoRepository;
    private final OrcamentoRepository orcamentoRepository;
    private final UsuarioRepository usuarioRepository;
    private final WhatsAppService whatsAppService;
    private final AgendamentoService agendamentoService;
    private final ClienteRepository clienteRepository;
    private final VeiculoRepository veiculoRepository;
    private final ServicoRepository servicoRepository;

    public OrdemServicoService(
            OrdemServicoRepository ordemServicoRepository,
            OrcamentoRepository orcamentoRepository,
            UsuarioRepository usuarioRepository,
            WhatsAppService whatsAppService,
            TaskScheduler taskScheduler,
            AgendamentoService agendamentoService,
            ClienteRepository clienteRepository,
            VeiculoRepository veiculoRepository,
            ServicoRepository servicoRepository) {
        this.ordemServicoRepository = ordemServicoRepository;
        this.orcamentoRepository = orcamentoRepository;
        this.usuarioRepository = usuarioRepository;
        this.whatsAppService = whatsAppService;
        this.taskScheduler = taskScheduler;
        this.agendamentoService = agendamentoService;
        this.clienteRepository = clienteRepository;
        this.veiculoRepository = veiculoRepository;
        this.servicoRepository = servicoRepository;
    }

    private Usuarios getUsuarioLogado() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new NaoEncontradoException("Usuário autenticado não encontrado"));
    }

    @Transactional
    public OrdemServicoDTO criarOrdemServico(
            Long numeroOrcamento,
            OrdemServico ordemInput) {

        Usuarios usuarioLogado = getUsuarioLogado();
        Empresa empresa = usuarioLogado.getEmpresa();
        System.out.println("Ta chegando aqui ");

        OrdemServico ordem = new OrdemServico();
        ordem.setEmpresa(empresa);
        ordem.setStatus(StatusOrdemServico.ATIVA);
        ordem.setDataCriacao(LocalDateTime.now());

        if (numeroOrcamento != null) {
            Orcamento orcamento = orcamentoRepository
                    .findByNumeroAndEmpresaId(numeroOrcamento, empresa.getId())
                    .orElseThrow(() -> new NaoEncontradoException("Orçamento não encontrado"));

            ordem.setOrcamento(orcamento);
            ordem.setCliente(orcamento.getCliente());
            ordem.setVeiculo(orcamento.getVeiculo());
            ordem.setServico(orcamento.getServico());
            ordem.setValor(orcamento.getValorAjustado());

            orcamento.setStatus(StatusOrcamento.GERADO);
            orcamentoRepository.save(orcamento);

        } else {

            if (ordemInput.getCliente() == null || ordemInput.getVeiculo() == null ||
                    ordemInput.getServico() == null || ordemInput.getValor() == null) {
                throw new IllegalArgumentException(
                        "Cliente, veículo, serviço e valor são obrigatórios para criar OS sem orçamento");
            }

            Cliente cliente = clienteRepository.findByCpfCnpjAndEmpresaId(
                    ordemInput.getCliente().getCpfCnpj(), empresa.getId())
                    .orElseThrow(() -> new NaoEncontradoException("Cliente não encontrado nesta empresa"));
            ordem.setCliente(cliente);

            Veiculo veiculo = veiculoRepository.findByPlacaAndEmpresaId(
                    ordemInput.getVeiculo().getPlaca(), empresa.getId())
                    .orElseThrow(() -> new NaoEncontradoException("Veículo não encontrado nesta empresa"));
            ordem.setVeiculo(veiculo);

            Servico servico = servicoRepository.findByNomeContainingIgnoreCaseAndEmpresaId(
                    ordemInput.getServico().getNome(), empresa.getId())
                    .orElseThrow(() -> new NaoEncontradoException("Serviço não encontrado nesta empresa"));
            ordem.setServico(servico);

            ordem.setValor(ordemInput.getValor());
        }
        System.out.println("Ta chegando aqui  2");
        if (ordemInput != null && ordemInput.getMecanico() != null) {
            Usuarios mecanicoSelecionado = usuarioRepository
                    .findByUsername(ordemInput.getMecanico().getUsername())
                    .orElseThrow(() -> new NaoEncontradoException("Mecânico não encontrado"));
            if (!mecanicoSelecionado.getEmpresa().getId().equals(empresa.getId())) {
                throw new IllegalArgumentException("Mecânico não pertence a esta empresa");
            }
            ordem.setMecanico(mecanicoSelecionado);
        } else {
            ordem.setMecanico(usuarioLogado);
        }

        Integer ultimoNumero = ordemServicoRepository.findUltimoNumeroPorEmpresa(empresa.getId()).orElse(0);
        ordem.setNumero(ultimoNumero + 1);

        System.out.println("Ta chegando aqui 3");

        OrdemServico salvo = ordemServicoRepository.save(ordem);

        try {
            String mensagem = String.format(
                    "Ordem de Serviço criada!\n" +
                            "- Código: %d\n" +
                            "- Valor: R$ %.2f\n" +
                            "- Mecânico: %s\n" +
                            "- Veículo: %s\n" +
                            "- Serviço: %s",
                    salvo.getNumero(),
                    salvo.getValor(),
                    salvo.getMecanico().getUsername(),
                    salvo.getVeiculo().getModelo(),
                    salvo.getServico().getNome());
            whatsAppService.enviarMensagem(salvo.getCliente().getCelular(), mensagem);
        } catch (EnviarException e) {
            System.err.println("Erro ao enviar mensagem: " + e.getMessage());
        }
        System.out.println("Ta chegando aqui  4");
        return new OrdemServicoDTO(salvo);
    }

    public OrdemServicoDTO atualizarOrdemServico(Long numeroOrdem, OrdemServicoDTO dto) {
        Usuarios usuarioLogado = getUsuarioLogado();
        UUID empresaId = usuarioLogado.getEmpresa().getId();
        OrdemServico ordem = ordemServicoRepository.findByNumeroAndEmpresaId(numeroOrdem, empresaId)
                .orElseThrow(() -> new NaoEncontradoException("Ordem de serviço não encontrada"));

        if (dto.getMecanicoUsername() != null) {
            Usuarios mecanicoSelecionado = usuarioRepository.findByUsername(dto.getMecanicoUsername())
                    .orElseThrow(() -> new NaoEncontradoException("Mecânico não encontrado"));
            if (!mecanicoSelecionado.getEmpresa().getId().equals(empresaId)) {
                throw new IllegalArgumentException("Mecânico não pertence a esta empresa");
            }
            ordem.setMecanico(mecanicoSelecionado);
        }

        if (dto.getObservacoes() != null)
            ordem.setObservacoes(dto.getObservacoes());

        if (dto.getServicoNome() != null) {
            Servico servico = ordem.getEmpresa().getServicos().stream()
                    .filter(s -> s.getNome().equalsIgnoreCase(dto.getServicoNome()))
                    .findFirst()
                    .orElseThrow(() -> new NaoEncontradoException("Serviço não encontrado na empresa"));
            ordem.setServico(servico);
        }

        if (dto.getValor() != null)
            ordem.setValor(dto.getValor());

        if (dto.getStatus() != null) {
            ordem.setStatus(dto.getStatus());

            if (dto.getStatus() == StatusOrdemServico.FINALIZADA) {
                ordem.setDataFinalizacao(LocalDateTime.now());
                agendamentoService.finalizarPorOrdemServico(ordem);
                scheduleMensagemRetorno(ordem);

                try {
                    String msgFinalizacao = String.format(
                            "Olá %s! Sua ordem de serviço nº %d foi finalizada. Agradecemos pela confiança!",
                            ordem.getCliente().getNome(),
                            ordem.getNumero());
                    whatsAppService.enviarMensagem(ordem.getCliente().getCelular(), msgFinalizacao);
                } catch (EnviarException e) {
                    System.err.println("Erro ao enviar mensagem de finalização: " + e.getMessage());
                }

            } else if (dto.getStatus() == StatusOrdemServico.INATIVA) {
                agendamentoService.excluirPorOrdemServico(ordem);
            }
        }

        OrdemServico salvo = ordemServicoRepository.save(ordem);

        if (salvo.getStatus() != StatusOrdemServico.FINALIZADA) {
            String mensagemAtualizacao = String.format(
                    "Olá %s! Sua ordem de serviço nº %d foi atualizada!\n" +
                            "- Status: %s\n" +
                            "- Valor: R$ %.2f\n" +
                            "- Serviço: %s\n" +
                            "- Mecânico: %s\n" +
                            (salvo.getObservacoes() != null ? "- Observações: " + salvo.getObservacoes() : ""),
                    salvo.getCliente().getNome(),
                    salvo.getNumero(),
                    salvo.getStatus(),
                    salvo.getValor(),
                    salvo.getServico().getNome(),
                    salvo.getMecanico().getUsername());
            try {
                whatsAppService.enviarMensagem(salvo.getCliente().getCelular(), mensagemAtualizacao);
            } catch (EnviarException e) {
                System.err.println("Erro ao enviar mensagem de atualização: " + e.getMessage());
            }
        }

        return new OrdemServicoDTO(salvo);
    }

    private void scheduleMensagemRetorno(OrdemServico ordem) {
        if (ordem.getServico().getMesesRetornoPadrao() != null && !ordem.isMensagemRetornoEnviada()) {

            int meses = ordem.getServico().getMesesRetornoPadrao();
            String msgRetorno = ordem.getServico().getMensagemRetornoPadrao();

            ordem.setMensagemRetornoEnviada(true);
            ordemServicoRepository.save(ordem);

            LocalDateTime dataEnvio = LocalDateTime.now().plusMinutes(3);

            taskScheduler.schedule(() -> {
                System.out.println("Enviando mensagem de retorno para " + ordem.getCliente().getCelular());
                whatsAppService.enviarMensagem(ordem.getCliente().getCelular(), msgRetorno);
            }, Date.from(dataEnvio.atZone(ZoneId.systemDefault()).toInstant()));
        }
    }

    public List<OrdemServicoDTO> listarTodos() {
        Usuarios usuario = getUsuarioLogado();
        UUID empresaId = usuario.getEmpresa().getId();

        List<OrdemServico> ordens;

        if (usuario.getRole() == RoleUsuario.ADMIN) {
            ordens = ordemServicoRepository.findByEmpresaIdOrderByStatusCustom(empresaId);
        } else {
            ordens = ordemServicoRepository.findByMecanicoUsernameAndEmpresaIdOrderByStatusCustom(
                    usuario.getUsername(),
                    empresaId);
        }

        return ordens.stream()
                .map(OrdemServicoDTO::new)
                .toList();
    }

    public OrdemServicoDTO listarPorId(Long numeroOrdem) {
        Usuarios usuario = getUsuarioLogado();
        UUID empresaId = usuario.getEmpresa().getId();

        OrdemServico ordem;
        if (usuario.getRole() == RoleUsuario.ADMIN) {
            ordem = ordemServicoRepository.findByNumeroAndEmpresaId(numeroOrdem, empresaId)
                    .orElseThrow(() -> new NaoEncontradoException("Ordem de serviço não encontrada"));
        } else {
            ordem = ordemServicoRepository.findByMecanicoUsernameAndEmpresaId(usuario.getUsername(), empresaId).stream()
                    .filter(o -> o.getNumero().equals(numeroOrdem))
                    .findFirst()
                    .orElseThrow(
                            () -> new NaoEncontradoException("Ordem de serviço não encontrada ou não pertence a você"));
        }

        return new OrdemServicoDTO(ordem);
    }
}