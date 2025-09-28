package com.fag.Autofinance.services;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.TimerTask;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.fag.Autofinance.dto.OrdemServicoDTO;
import com.fag.Autofinance.entities.Orcamento;
import com.fag.Autofinance.entities.OrdemServico;
import com.fag.Autofinance.entities.Servico;
import com.fag.Autofinance.entities.Usuarios;
import com.fag.Autofinance.enums.StatusOrcamento;
import com.fag.Autofinance.enums.StatusOrdemServico;
import com.fag.Autofinance.exception.EnviarException;
import com.fag.Autofinance.exception.NaoEncontradoException;
import com.fag.Autofinance.repositories.OrcamentoRepository;
import com.fag.Autofinance.repositories.OrdemServicoRepository;
import com.fag.Autofinance.repositories.UsuarioRepository;

@Service

public class OrdemServicoService {

    private final TaskScheduler taskScheduler;

    private final OrdemServicoRepository ordemServicoRepository;
    private final OrcamentoRepository orcamentoRepository;
    private final UsuarioRepository usuarioRepository;
    private final WhatsAppService whatsAppService;

    public OrdemServicoService(OrdemServicoRepository ordemServicoRepository,
            OrcamentoRepository orcamentoRepository,
            UsuarioRepository usuarioRepository,
            WhatsAppService whatsAppService,
            TaskScheduler taskScheduler) {
        this.ordemServicoRepository = ordemServicoRepository;
        this.orcamentoRepository = orcamentoRepository;
        this.usuarioRepository = usuarioRepository;
        this.whatsAppService = whatsAppService;
        this.taskScheduler = taskScheduler;
    }

    private Usuarios getUsuarioLogado() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new NaoEncontradoException("Usuário autenticado não encontrado"));
    }

    public OrdemServicoDTO criarOrdemServico(Long numeroOrcamento) {
        Usuarios mecanico = getUsuarioLogado();
        Orcamento orcamento = orcamentoRepository
                .findByNumeroAndEmpresaId(numeroOrcamento, mecanico.getEmpresa().getId())
                .orElseThrow(() -> new NaoEncontradoException("Orçamento não encontrado"));

        OrdemServico ordem = new OrdemServico();
        ordem.setEmpresa(mecanico.getEmpresa());
        ordem.setCliente(orcamento.getCliente());
        ordem.setVeiculo(orcamento.getVeiculo());
        ordem.setServico(orcamento.getServico());
        ordem.setMecanico(mecanico);
        ordem.setOrcamento(orcamento);
        ordem.setValor(orcamento.getValorAjustado());
        ordem.setStatus(StatusOrdemServico.ATIVA);
        ordem.setDataCriacao(LocalDateTime.now());

        Integer ultimoNumero = ordemServicoRepository.findUltimoNumeroPorEmpresa(ordem.getEmpresa().getId())
                .orElse(0);
        ordem.setNumero(ultimoNumero + 1);

        OrdemServico salvo = ordemServicoRepository.save(ordem);

        orcamento.setStatus(StatusOrcamento.GERADO);
        orcamentoRepository.save(orcamento);

        /*
         * String mensagem = String.format(
         * "Orçamento aprovado! Ordem de Serviço criada:\n" +
         * "- Código: %d\n" +
         * "- Valor: R$ %.2f\n" +
         * "- Mecânico: %s\n" +
         * "- Veículo: %s\n" +
         * "- Serviço: %s",
         * salvo.getNumero(),
         * salvo.getValor(),
         * salvo.getMecanico().getUsername(),
         * salvo.getVeiculo().getModelo(),
         * salvo.getServico().getNome());
         * try {
         * whatsAppService.enviarMensagem(salvo.getCliente().getCelular(), mensagem);
         * } catch (EnviarException e) {
         * System.err.println("Erro ao enviar mensagem: " + e.getMessage());
         * 
         * throw new
         * RuntimeException("Não foi possível enviar a mensagem pelo WhatsApp", e);
         * }
         */
        return new OrdemServicoDTO(salvo);
    }

    public OrdemServicoDTO atualizarOrdemServico(Long numeroOrdem, OrdemServicoDTO dto) {
        UUID empresaId = getUsuarioLogado().getEmpresa().getId();
        OrdemServico ordem = ordemServicoRepository.findByNumeroAndEmpresaId(numeroOrdem, empresaId)
                .orElseThrow(() -> new NaoEncontradoException("Ordem de serviço não encontrada"));

        if (dto.getStatus() != null) {
            ordem.setStatus(dto.getStatus());
            if (dto.getStatus() == StatusOrdemServico.FINALIZADA) {
                ordem.setDataFinalizacao(LocalDateTime.now());
                scheduleMensagemRetorno(ordem);
            }
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

        OrdemServico salvo = ordemServicoRepository.save(ordem);
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

    public Page<OrdemServicoDTO> listarTodos(Pageable pageable) {
        UUID empresaId = getUsuarioLogado().getEmpresa().getId();
        return ordemServicoRepository.findByEmpresaId(empresaId, pageable)
                .map(OrdemServicoDTO::new);
    }

    public OrdemServicoDTO listarPorId(Long numeroOrdem) {
        UUID empresaId = getUsuarioLogado().getEmpresa().getId();
        OrdemServico ordem = ordemServicoRepository.findByNumeroAndEmpresaId(numeroOrdem, empresaId)
                .orElseThrow(() -> new NaoEncontradoException("Ordem de serviço não encontrada"));
        return new OrdemServicoDTO(ordem);
    }
}