package com.fag.Autofinance.services;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.fag.Autofinance.dto.OrcamentoDTO;
import com.fag.Autofinance.entities.Cliente;
import com.fag.Autofinance.entities.Empresa;
import com.fag.Autofinance.entities.Orcamento;
import com.fag.Autofinance.entities.Servico;
import com.fag.Autofinance.entities.Usuarios;
import com.fag.Autofinance.entities.Veiculo;
import com.fag.Autofinance.enums.StatusCadastros;
import com.fag.Autofinance.exception.NaoEncontradoException;
import com.fag.Autofinance.repositories.ClienteRepository;
import com.fag.Autofinance.repositories.OrcamentoRepository;
import com.fag.Autofinance.repositories.ServicoRepository;
import com.fag.Autofinance.repositories.UsuarioRepository;
import com.fag.Autofinance.repositories.VeiculoRepository;

@Service
public class OrcamentoService {

        private final OrcamentoRepository orcamentoRepository;
        private final ClienteRepository clienteRepository;
        private final VeiculoRepository veiculoRepository;
        private final ServicoRepository servicoRepository;
        private final UsuarioRepository usuarioRepository;
        private final WhatsAppService whatsAppService;

        public OrcamentoService(
                        OrcamentoRepository orcamentoRepository,
                        ClienteRepository clienteRepository,
                        VeiculoRepository veiculoRepository,
                        ServicoRepository servicoRepository,
                        UsuarioRepository usuarioRepository, WhatsAppService whatsAppService) {
                this.orcamentoRepository = orcamentoRepository;
                this.clienteRepository = clienteRepository;
                this.veiculoRepository = veiculoRepository;
                this.servicoRepository = servicoRepository;
                this.usuarioRepository = usuarioRepository;
                this.whatsAppService = whatsAppService;
        }

        private Usuarios getUsuarioLogado() {
                String username = SecurityContextHolder.getContext().getAuthentication().getName();
                return usuarioRepository.findByUsername(username)
                                .orElseThrow(() -> new NaoEncontradoException("Usuário autenticado não encontrado"));
        }

        public OrcamentoDTO atualizarOrcamento(Long id, OrcamentoDTO orcamentoDTO) {

                Usuarios mecanico = getUsuarioLogado();
                Long empresaId = mecanico.getEmpresa().getId();

                Orcamento orcamento = orcamentoRepository.findByIdAndEmpresaId(id, empresaId)
                                .orElseThrow(() -> new NaoEncontradoException(
                                                "Orçamento não encontrado nesta empresa"));

                boolean mudouInativo = orcamentoDTO.getStatus() == StatusCadastros.INATIVO
                                && orcamento.getStatus() != StatusCadastros.INATIVO;

                Servico servico = servicoRepository.findByNomeContainingIgnoreCaseAndEmpresaId(
                                orcamentoDTO.getServicoNome(),
                                empresaId)
                                .orElseThrow(() -> new NaoEncontradoException("Serviço não encontrado nesta empresa"));
                orcamento.setServico(servico);

                orcamento.setValorAjustado(orcamentoDTO.getValorAjustado());
                orcamento.setStatus(orcamentoDTO.getStatus());

                orcamento = orcamentoRepository.save(orcamento);

                if (mudouInativo) {
                        String numeroCliente = orcamento.getCliente().getCelular();
                        whatsAppService.enviarMensagem(numeroCliente, "Seu orçamento foi concluído!");

                        if (orcamento.getServico() != null && orcamento.getServico().getMesesRetornoPadrao() != null) {
                                int minutos = orcamento.getServico().getMesesRetornoPadrao();
                                String msgRetorno = orcamento.getServico().getMensagemRetornoPadrao();

                                Timer timer = new Timer();
                                timer.schedule(new TimerTask() {
                                        @Override
                                        public void run() {
                                                whatsAppService.enviarMensagem(numeroCliente, msgRetorno);
                                        }
                                }, minutos * 60 * 1000L);
                        }
                }

                return new OrcamentoDTO(orcamento);
        }

        public OrcamentoDTO criarOrcamento(Orcamento orcamento) {
                Usuarios mecanico = getUsuarioLogado();
                Empresa empresa = mecanico.getEmpresa();

                orcamento.setMecanico(mecanico);
                orcamento.setEmpresa(empresa);

                Cliente cliente = clienteRepository.findByCpfCnpjAndEmpresaId(
                                orcamento.getCliente().getCpfCnpj(),
                                empresa.getId())
                                .orElseThrow(() -> new NaoEncontradoException("Cliente não encontrado nesta empresa"));
                orcamento.setCliente(cliente);

                Veiculo veiculo = veiculoRepository.findByPlacaAndEmpresaId(
                                orcamento.getVeiculo().getPlaca(),
                                empresa.getId())
                                .orElseThrow(() -> new NaoEncontradoException("Veículo não encontrado nesta empresa"));
                orcamento.setVeiculo(veiculo);

                Servico servico = servicoRepository.findByNomeContainingIgnoreCaseAndEmpresaId(
                                orcamento.getServico().getNome(),
                                empresa.getId())
                                .orElseThrow(() -> new NaoEncontradoException("Serviço não encontrado nesta empresa"));
                orcamento.setServico(servico);

                Orcamento salvo = orcamentoRepository.save(orcamento);

                String celularCliente = salvo.getCliente().getCelular();
                String mensagem = String.format(
                                "Olá %s! Seu orçamento foi criado:\n" +
                                                "- Valor: R$ %.2f\n" +
                                                "- Mecânico: %s\n" +
                                                "- Veículo: %s\n" +
                                                "- Serviço: %s",
                                salvo.getCliente().getNome(),
                                salvo.getValorAjustado(),
                                salvo.getMecanico().getUsername(),
                                salvo.getVeiculo().getModelo(),
                                salvo.getServico().getNome());
                whatsAppService.enviarMensagem(celularCliente, mensagem);

                return new OrcamentoDTO(salvo);
        }

        public List<OrcamentoDTO> listarTodos() {
                Long empresaId = getUsuarioLogado().getEmpresa().getId();
                return orcamentoRepository.findAllByEmpresaId(empresaId)
                                .stream()
                                .map(OrcamentoDTO::new)
                                .toList();
        }

        public List<OrcamentoDTO> listarPorMecanico(String username) {
                Long empresaId = getUsuarioLogado().getEmpresa().getId();
                return orcamentoRepository.findByMecanicoUsernameAndEmpresaId(username, empresaId)
                                .stream()
                                .map(OrcamentoDTO::new)
                                .toList();
        }

        public OrcamentoDTO listarPorId(Long id) {
                Long empresaId = getUsuarioLogado().getEmpresa().getId();
                Orcamento orcamento = orcamentoRepository
                                .findByIdAndEmpresaId(id, empresaId)
                                .orElseThrow(() -> new NaoEncontradoException("Orçamento não encontrado"));
                return new OrcamentoDTO(orcamento);
        }

}
