package com.fag.Autofinance.services;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.UUID;
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
import com.fag.Autofinance.enums.StatusCadastros;
import com.fag.Autofinance.enums.StatusOrcamento;
import com.fag.Autofinance.enums.StatusOrdemServico;
import com.fag.Autofinance.exception.NaoEncontradoException;
import com.fag.Autofinance.exception.ValidarException;
import com.fag.Autofinance.repositories.ClienteRepository;
import com.fag.Autofinance.repositories.OrcamentoRepository;
import com.fag.Autofinance.repositories.OrdemServicoRepository;
import com.fag.Autofinance.repositories.ServicoRepository;
import com.fag.Autofinance.repositories.UsuarioRepository;
import com.fag.Autofinance.repositories.VeiculoRepository;
import org.springframework.transaction.annotation.Transactional;

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

    private String formatarCpfCnpj(String cpfCnpj) {
        if (cpfCnpj == null)
            return null;
        return cpfCnpj.replaceAll("\\D", "");
    }

    private String padronizarPlaca(String placa) {
        if (placa == null)
            return null;
        return placa.toUpperCase().replaceAll("[^A-Z0-9]", "");
    }

    @Transactional
    public OrdemServicoDTO criarOrdemServico(
            Long numeroOrcamento,
            OrdemServico ordemInput) {

        Usuarios usuarioLogado = getUsuarioLogado();
        Empresa empresa = usuarioLogado.getEmpresa();

        OrdemServico ordem = new OrdemServico();
        ordem.setEmpresa(empresa);
        ordem.setStatus(StatusOrdemServico.ATIVA);
        ordem.setDataCriacao(LocalDateTime.now());

        if (numeroOrcamento != null) {
            preencherDadosComOrcamento(ordem, numeroOrcamento, empresa.getId());
        } else {
            preencherDadosSemOrcamento(ordem, ordemInput, empresa.getId());
        }

        Usuarios mecanico = buscarEValidarMecanico(
                (ordemInput != null ? ordemInput.getMecanico() : null),
                usuarioLogado,
                empresa.getId());
        ordem.setMecanico(mecanico);

        Integer ultimoNumero = ordemServicoRepository.findUltimoNumeroPorEmpresa(empresa.getId()).orElse(0);
        ordem.setNumero(ultimoNumero + 1);

        OrdemServico salvo = ordemServicoRepository.save(ordem);

        enviarMensagemCriacao(salvo);

        return new OrdemServicoDTO(salvo);
    }

    @Transactional
    public OrdemServicoDTO atualizarOrdemServico(Long numeroOrdem, OrdemServicoDTO dto) {
        Usuarios usuarioLogado = getUsuarioLogado();
        UUID empresaId = usuarioLogado.getEmpresa().getId();
        OrdemServico ordem = ordemServicoRepository.findByNumeroAndEmpresaId(numeroOrdem, empresaId)
                .orElseThrow(() -> new NaoEncontradoException("Ordem de serviço não encontrada"));

        if (dto.getMecanicoUsername() != null) {
            Usuarios mecanicoInput = new Usuarios();
            mecanicoInput.setUsername(dto.getMecanicoUsername());

            Usuarios mecanico = buscarEValidarMecanico(mecanicoInput, usuarioLogado, empresaId);
            ordem.setMecanico(mecanico);
        }

        if (dto.getObservacoes() != null)
            ordem.setObservacoes(dto.getObservacoes());

        if (dto.getServicoNome() != null) {

            Servico servico = servicoRepository
                    .findByNomeContainingIgnoreCaseAndEmpresaId(dto.getServicoNome(), empresaId)
                    .orElseThrow(() -> new NaoEncontradoException("Serviço não encontrado na empresa"));

            if (servico.getStatus() != StatusCadastros.ATIVO) {
                throw new ValidarException("Serviço inativo não pode ser vinculado a uma OS.");
            }
            ordem.setServico(servico);
        }

        if (dto.getValor() != null)
            ordem.setValor(dto.getValor());

        if (dto.getStatus() != null) {

            processarMudancaDeStatus(ordem, dto.getStatus());
        }

        OrdemServico salvo = ordemServicoRepository.save(ordem);

        enviarMensagemAtualizacao(salvo);

        return new OrdemServicoDTO(salvo);
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
            ordem = ordemServicoRepository
                    .findByNumeroAndMecanicoUsernameAndEmpresaId(numeroOrdem, usuario.getUsername(), empresaId)
                    .orElseThrow(
                            () -> new NaoEncontradoException("Ordem de serviço não encontrada ou não pertence a você"));
        }

        return new OrdemServicoDTO(ordem);
    }

    // --- MÉTODOS HELPER ---

    private void preencherDadosComOrcamento(OrdemServico ordem, Long numeroOrcamento, UUID empresaId) {
        Orcamento orcamento = orcamentoRepository
                .findByNumeroAndEmpresaId(numeroOrcamento, empresaId)
                .orElseThrow(() -> new NaoEncontradoException("Orçamento não encontrado"));

        if (orcamento.getStatus() != StatusOrcamento.ATIVO) {
            throw new ValidarException("Este orçamento não está ATIVO e não pode ser usado.");
        }

        ordem.setOrcamento(orcamento);
        ordem.setCliente(orcamento.getCliente());
        ordem.setVeiculo(orcamento.getVeiculo());
        ordem.setServico(orcamento.getServico());
        ordem.setValor(orcamento.getValorAjustado());

        orcamento.setStatus(StatusOrcamento.GERADO);
        orcamentoRepository.save(orcamento);
    }

    private void preencherDadosSemOrcamento(OrdemServico ordem, OrdemServico ordemInput, UUID empresaId) {
        if (ordemInput.getCliente() == null || ordemInput.getCliente().getCpfCnpj() == null ||
                ordemInput.getVeiculo() == null || ordemInput.getVeiculo().getPlaca() == null ||
                ordemInput.getServico() == null || ordemInput.getServico().getNome() == null ||
                ordemInput.getValor() == null) {
            throw new ValidarException(
                    "Cliente, veículo, serviço e valor são obrigatórios para criar OS sem orçamento");
        }

        String cpfCnpj = formatarCpfCnpj(ordemInput.getCliente().getCpfCnpj());
        Cliente cliente = clienteRepository.findByCpfCnpjAndEmpresaId(cpfCnpj, empresaId)
                .orElseThrow(() -> new NaoEncontradoException("Cliente não encontrado nesta empresa"));
        if (cliente.getStatus() != StatusCadastros.ATIVO) {
            throw new ValidarException("Cliente inativo não pode ter OS.");
        }
        ordem.setCliente(cliente);

        String placa = padronizarPlaca(ordemInput.getVeiculo().getPlaca());
        Veiculo veiculo = veiculoRepository.findByPlacaAndEmpresaId(placa, empresaId)
                .orElseThrow(() -> new NaoEncontradoException("Veículo não encontrado nesta empresa"));
        if (veiculo.getStatus() != StatusCadastros.ATIVO) {
            throw new ValidarException("Veículo inativo não pode ter OS.");
        }
        ordem.setVeiculo(veiculo);

        Servico servico = servicoRepository.findByNomeContainingIgnoreCaseAndEmpresaId(
                ordemInput.getServico().getNome(), empresaId)
                .orElseThrow(() -> new NaoEncontradoException("Serviço não encontrado nesta empresa"));
        if (servico.getStatus() != StatusCadastros.ATIVO) {
            throw new ValidarException("Serviço inativo não pode ter OS.");
        }
        ordem.setServico(servico);

        ordem.setValor(ordemInput.getValor());
    }

    private Usuarios buscarEValidarMecanico(Usuarios mecanicoInput, Usuarios usuarioLogado, UUID empresaId) {
        Usuarios mecanico;

        if (mecanicoInput != null && mecanicoInput.getUsername() != null) {
            mecanico = usuarioRepository
                    .findByUsername(mecanicoInput.getUsername())
                    .orElseThrow(() -> new NaoEncontradoException("Mecânico não encontrado"));

            if (!mecanico.getEmpresa().getId().equals(empresaId)) {
                throw new ValidarException("Mecânico não pertence a esta empresa");
            }
        } else {

            mecanico = usuarioLogado;
        }

        if (mecanico.getStatus() != StatusCadastros.ATIVO) {
            throw new ValidarException("Mecânico inativo não pode ser vinculado a uma OS.");
        }
        return mecanico;
    }

    private void processarMudancaDeStatus(OrdemServico ordem, StatusOrdemServico novoStatus) {
        ordem.setStatus(novoStatus);

        if (novoStatus == StatusOrdemServico.FINALIZADA) {
            ordem.setDataFinalizacao(LocalDateTime.now());
            agendamentoService.finalizarPorOrdemServico(ordem);
            scheduleMensagemRetorno(ordem);
            enviarMensagemFinalizacao(ordem);

        } else if (novoStatus == StatusOrdemServico.INATIVA) {
            agendamentoService.excluirPorOrdemServico(ordem);
        }
    }

    // --- MÉTODOS HELPER ---

    private void scheduleMensagemRetorno(OrdemServico ordem) {

        if (ordem.getServico().getMesesRetornoPadrao() != null && !ordem.isMensagemRetornoEnviada()) {

            int meses = ordem.getServico().getMesesRetornoPadrao();
            String msgRetorno = ordem.getServico().getMensagemRetornoPadrao();

            if (meses <= 0 || msgRetorno == null || msgRetorno.isBlank()) {
                System.err.println("Não foi possível agendar retorno para OS " + ordem.getNumero()
                        + ": dados de retorno incompletos no serviço.");
                return;
            }

            LocalDateTime dataEnvio = LocalDateTime.now().plusMinutes(3);

            taskScheduler.schedule(() -> {

                try {
                    System.out.println("Enviando mensagem de retorno agendada para " + ordem.getCliente().getCelular());
                    whatsAppService.enviarMensagem(ordem.getCliente().getCelular(), msgRetorno);

                    OrdemServico osParaAtualizar = ordemServicoRepository.findById(ordem.getId()).orElse(null);
                    if (osParaAtualizar != null) {
                        osParaAtualizar.setMensagemRetornoEnviada(true);
                        ordemServicoRepository.save(osParaAtualizar);
                    }
                } catch (Exception e) {
                    System.err.println("Falha ao enviar mensagem de retorno agendada para OS " + ordem.getNumero()
                            + ": " + e.getMessage());

                }
            }, Date.from(dataEnvio.atZone(ZoneId.systemDefault()).toInstant()));

        }
    }

    private void enviarMensagemCriacao(OrdemServico salvo) {
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
        } catch (Exception e) { // <--- Use Exception genérica
            System.err.println("Erro ao enviar mensagem de criação de OS: " + e.getMessage());
        }
    }

    private void enviarMensagemFinalizacao(OrdemServico ordem) {
        try {
            String msgFinalizacao = String.format(
                    "Olá %s! Sua ordem de serviço nº %d foi finalizada. Agradecemos pela confiança!",
                    ordem.getCliente().getNome(),
                    ordem.getNumero());
            whatsAppService.enviarMensagem(ordem.getCliente().getCelular(), msgFinalizacao);
        } catch (Exception e) {
            System.err.println("Erro ao enviar mensagem de finalização: " + e.getMessage());
        }
    }

    private void enviarMensagemAtualizacao(OrdemServico salvo) {

        if (salvo.getStatus() != StatusOrdemServico.FINALIZADA) {
            try {
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

                whatsAppService.enviarMensagem(salvo.getCliente().getCelular(), mensagemAtualizacao);
            } catch (Exception e) {
                System.err.println("Erro ao enviar mensagem de atualização: " + e.getMessage());
            }
        }
    }
}