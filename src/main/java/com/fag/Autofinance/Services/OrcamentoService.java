package com.fag.Autofinance.services;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.fag.Autofinance.dto.OrcamentoDTO;
import com.fag.Autofinance.entities.Cliente;
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

        public OrcamentoDTO atualizarOrcamento(Long id, OrcamentoDTO orcamentoDTO) {
                Orcamento orcamento = orcamentoRepository.findById(id)
                                .orElseThrow(() -> new NaoEncontradoException("Orçamento não encontrado"));

                // VERIFICAR SE MUDOU O STATUS PARA INATIVO
                boolean mudouInativo = orcamentoDTO.getStatus() == StatusCadastros.INATIVO
                                && orcamento.getStatus() != StatusCadastros.INATIVO;

                Cliente cliente = clienteRepository.findByCpfCnpj(orcamentoDTO.getCpfCnpj())
                                .orElseThrow(() -> new NaoEncontradoException("Cliente não encontrado"));
                orcamento.setCliente(cliente);

                Servico servico = servicoRepository.findByNomeContainingIgnoreCase(orcamentoDTO.getServicoNome())
                                .orElseThrow(() -> new NaoEncontradoException("Serviço não encontrado"));
                orcamento.setServico(servico);

                orcamento.setValorAjustado(orcamentoDTO.getValorAjustado());
                orcamento.setStatus(orcamentoDTO.getStatus());

                orcamento = orcamentoRepository.save(orcamento);

                // MANDAR MENSAGEM QUANDO ALTERAR O STATUS
                if (mudouInativo) {
                        String numeroCliente = orcamento.getCliente().getCelular();
                        whatsAppService.enviarMensagem(numeroCliente,
                                        "Seu orçamento foi concluído!");

                        if (orcamento.getServico() != null &&
                                        orcamento.getServico().getMesesRetornoPadrao() != null) {
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

        public OrcamentoDTO criarOrcamento(Orcamento orcamento, String usernameCriador) {

                usernameCriador = SecurityContextHolder.getContext().getAuthentication().getName();

                Usuarios mecanico = usuarioRepository.findByUsername(usernameCriador)
                                .orElseThrow(() -> new NaoEncontradoException("Usuário autenticado não encontrado"));

                orcamento.setMecanico(mecanico);

                Cliente cliente = clienteRepository.findByCpfCnpj(orcamento.getCliente().getCpfCnpj())
                                .orElseThrow(() -> new NaoEncontradoException("Cliente não encontrado"));
                orcamento.setCliente(cliente);

                Veiculo veiculo = veiculoRepository.findByPlaca(orcamento.getVeiculo().getPlaca())
                                .orElseThrow(() -> new NaoEncontradoException("Veículo não encontrado"));
                orcamento.setVeiculo(veiculo);

                Servico servico = servicoRepository.findByNomeContainingIgnoreCase(orcamento.getServico().getNome())
                                .orElseThrow(() -> new NaoEncontradoException("Serviço não encontrado"));
                orcamento.setServico(servico);

                Orcamento salvo = orcamentoRepository.save(orcamento);

                String celularCliente = salvo.getCliente().getCelular(); // formato: +5511999999999
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
                return orcamentoRepository.findAll()
                                .stream()
                                .map(OrcamentoDTO::new)
                                .toList();
        }

        public List<OrcamentoDTO> listarPorMecanico(String username) {
                return orcamentoRepository.findByMecanicoUsername(username)
                                .stream()
                                .map(OrcamentoDTO::new)
                                .toList();
        }

        public OrcamentoDTO listarPorId(Long id) {
                Orcamento orcamento = orcamentoRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Orçamento não encontrado"));
                return new OrcamentoDTO(orcamento);
        }

}
