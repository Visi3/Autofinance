package com.fag.Autofinance.services;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.fag.Autofinance.dto.OrcamentoDTO;
import com.fag.Autofinance.entities.Cliente;
import com.fag.Autofinance.entities.Empresa;
import com.fag.Autofinance.entities.Orcamento;
import com.fag.Autofinance.entities.Servico;
import com.fag.Autofinance.entities.Usuarios;
import com.fag.Autofinance.entities.Veiculo;

import com.fag.Autofinance.enums.StatusOrcamento;

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

        public OrcamentoDTO atualizarOrcamento(Long numero, OrcamentoDTO orcamentoDTO) {

                Usuarios mecanico = getUsuarioLogado();
                UUID empresaId = mecanico.getEmpresa().getId();

                Orcamento orcamento = orcamentoRepository.findByNumeroAndEmpresaId(numero, empresaId)
                                .orElseThrow(() -> new NaoEncontradoException(
                                                "Orçamento não encontrado nesta empresa"));

                if (orcamentoDTO.getStatus() == StatusOrcamento.GERADO) {
                        throw new IllegalArgumentException(
                                        "O status GERADO só pode ser definido ao criar uma Ordem de Serviço.");
                }

                Servico servico = servicoRepository.findByNomeContainingIgnoreCaseAndEmpresaId(
                                orcamentoDTO.getServicoNome(),
                                empresaId)
                                .orElseThrow(() -> new NaoEncontradoException("Serviço não encontrado nesta empresa"));
                orcamento.setServico(servico);

                orcamento.setValorAjustado(orcamentoDTO.getValorAjustado());
                orcamento.setStatus(orcamentoDTO.getStatus());

                return new OrcamentoDTO(orcamentoRepository.save(orcamento));
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

                Long proximoNumero = orcamentoRepository.findTopByEmpresaIdOrderByNumeroDesc(empresa.getId())
                                .map(o -> o.getNumero() + 1)
                                .orElse(1L);
                orcamento.setNumero(proximoNumero);

                Orcamento salvo = orcamentoRepository.save(orcamento);

                String celularCliente = salvo.getCliente().getCelular();

                /*
                 * try {
                 * String mensagem = String.format(
                 * "Olá %s! Seu orçamento foi criado:\n" +
                 * "- Código: %d\n" +
                 * "- Valor: R$ %.2f\n" +
                 * "- Mecânico: %s\n" +
                 * "- Veículo: %s\n" +
                 * "- Serviço: %s",
                 * salvo.getCliente().getNome(),
                 * salvo.getNumero(),
                 * salvo.getValorAjustado(),
                 * salvo.getMecanico().getUsername(),
                 * salvo.getVeiculo().getModelo(),
                 * salvo.getServico().getNome());
                 * whatsAppService.enviarMensagem(celularCliente, mensagem);
                 * } catch (EnviarException e) {
                 * 
                 * System.err.println("Erro ao enviar mensagem: " + e.getMessage());
                 * 
                 * throw new
                 * RuntimeException("Não foi possível enviar a mensagem pelo WhatsApp", e);
                 * }
                 */

                return new OrcamentoDTO(salvo);
        }

        public Page<OrcamentoDTO> listarTodos(Pageable pageable) {
                Usuarios mecanico = getUsuarioLogado();
                UUID empresaId = mecanico.getEmpresa().getId();

                return orcamentoRepository.findByEmpresaId(empresaId, pageable)
                                .map(OrcamentoDTO::new);
        }

        public List<OrcamentoDTO> listarPorMecanico(String username) {
                UUID empresaId = getUsuarioLogado().getEmpresa().getId();
                return orcamentoRepository.findByMecanicoUsernameAndEmpresaId(username, empresaId)
                                .stream()
                                .map(OrcamentoDTO::new)
                                .toList();
        }

        public OrcamentoDTO listarPorId(Long numero) {
                UUID empresaId = getUsuarioLogado().getEmpresa().getId();
                Orcamento orcamento = orcamentoRepository
                                .findByNumeroAndEmpresaId(numero, empresaId)
                                .orElseThrow(() -> new NaoEncontradoException("Orçamento não encontrado"));
                return new OrcamentoDTO(orcamento);
        }

}
