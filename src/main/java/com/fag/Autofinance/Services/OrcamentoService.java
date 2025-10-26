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
import com.fag.Autofinance.enums.RoleUsuario;
import com.fag.Autofinance.enums.StatusCadastros;
import com.fag.Autofinance.enums.StatusOrcamento;
import com.fag.Autofinance.exception.EnviarException;
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
                Usuarios usuarioLogado = getUsuarioLogado();
                UUID empresaId = usuarioLogado.getEmpresa().getId();

                Orcamento orcamento = orcamentoRepository.findByNumeroAndEmpresaId(numero, empresaId)
                                .orElseThrow(() -> new NaoEncontradoException(
                                                "Orçamento não encontrado nesta empresa"));

                if (orcamentoDTO.getStatus() == StatusOrcamento.GERADO) {
                        throw new IllegalArgumentException(
                                        "O status GERADO só pode ser definido ao criar uma Ordem de Serviço.");
                }

                if (orcamentoDTO.getMecanicoUsername() != null) {
                        Usuarios mecanico = usuarioRepository.findByUsername(orcamentoDTO.getMecanicoUsername())
                                        .orElseThrow(() -> new NaoEncontradoException("Mecânico não encontrado"));

                        if (!mecanico.getEmpresa().getId().equals(empresaId)) {
                                throw new IllegalArgumentException("Mecânico não pertence a esta empresa");
                        }
                        if (mecanico.getStatus() != StatusCadastros.ATIVO) {
                                throw new IllegalArgumentException(
                                                "Mecânico inativo não pode ser vinculado ao orçamento");
                        }

                        orcamento.setMecanico(mecanico);
                }

                Servico servico = servicoRepository.findByNomeContainingIgnoreCaseAndEmpresaId(
                                orcamentoDTO.getServicoNome(), empresaId)
                                .orElseThrow(() -> new NaoEncontradoException("Serviço não encontrado nesta empresa"));

                if (servico.getStatus() != StatusCadastros.ATIVO) {
                        throw new IllegalArgumentException("Serviço inativo não pode ser utilizado no orçamento");
                }
                if (servico.getPreco() == null || servico.getPreco() <= 0) {
                        throw new IllegalArgumentException("O serviço selecionado não possui valor cadastrado");
                }

                orcamento.setServico(servico);

                double valorFinal = (orcamentoDTO.getValorAjustado() != null && orcamentoDTO.getValorAjustado() > 0)
                                ? orcamentoDTO.getValorAjustado()
                                : servico.getPreco();
                orcamento.setValorAjustado(valorFinal);

                orcamento.setStatus(orcamentoDTO.getStatus());

                Orcamento atualizado = orcamentoRepository.save(orcamento);
                return new OrcamentoDTO(atualizado);
        }

        public OrcamentoDTO criarOrcamento(Orcamento orcamento) {
                Usuarios usuarioLogado = getUsuarioLogado();
                Empresa empresa = usuarioLogado.getEmpresa();

                Usuarios mecanico = (orcamento.getMecanico() != null)
                                ? usuarioRepository.findByUsername(orcamento.getMecanico().getUsername())
                                                .orElseThrow(() -> new NaoEncontradoException(
                                                                "Mecânico não encontrado"))
                                : usuarioLogado;

                if (!mecanico.getEmpresa().getId().equals(empresa.getId())) {
                        throw new IllegalArgumentException("Mecânico não pertence a esta empresa");
                }
                if (mecanico.getStatus() != StatusCadastros.ATIVO) {
                        throw new IllegalArgumentException("Mecânico inativo não pode ser vinculado a um orçamento");
                }

                orcamento.setMecanico(mecanico);
                orcamento.setEmpresa(empresa);

                Cliente cliente = clienteRepository.findByCpfCnpjAndEmpresaId(
                                orcamento.getCliente().getCpfCnpj(), empresa.getId())
                                .orElseThrow(() -> new NaoEncontradoException("Cliente não encontrado nesta empresa"));

                if (cliente.getStatus() != StatusCadastros.ATIVO) {
                        throw new IllegalArgumentException("Cliente inativo não pode receber orçamentos");
                }
                orcamento.setCliente(cliente);

                Veiculo veiculo = veiculoRepository.findByPlacaAndEmpresaId(
                                orcamento.getVeiculo().getPlaca(), empresa.getId())
                                .orElseThrow(() -> new NaoEncontradoException("Veículo não encontrado nesta empresa"));

                if (veiculo.getStatus() != StatusCadastros.ATIVO) {
                        throw new IllegalArgumentException("Veículo inativo não pode ser vinculado a um orçamento");
                }
                orcamento.setVeiculo(veiculo);

                Servico servico = servicoRepository.findByNomeContainingIgnoreCaseAndEmpresaId(
                                orcamento.getServico().getNome(), empresa.getId())
                                .orElseThrow(() -> new NaoEncontradoException("Serviço não encontrado nesta empresa"));

                if (servico.getStatus() != StatusCadastros.ATIVO) {
                        throw new IllegalArgumentException("Serviço inativo não pode ser utilizado em um orçamento");
                }
                if (servico.getPreco() == null || servico.getPreco() <= 0) {
                        throw new IllegalArgumentException("O serviço selecionado não possui valor cadastrado");
                }
                orcamento.setServico(servico);

                double valorFinal = (orcamento.getValorAjustado() != null && orcamento.getValorAjustado() > 0)
                                ? orcamento.getValorAjustado()
                                : servico.getPreco();

                orcamento.setValorAjustado(valorFinal);

                Long proximoNumero = orcamentoRepository.findTopByEmpresaIdOrderByNumeroDesc(empresa.getId())
                                .map(o -> o.getNumero() + 1)
                                .orElse(1L);
                orcamento.setNumero(proximoNumero);

                Orcamento salvo = orcamentoRepository.save(orcamento);

                try {
                        String celularCliente = salvo.getCliente().getCelular();
                        if (celularCliente != null && !celularCliente.isBlank()) {
                                String mensagem = String.format(
                                                "Olá %s! Seu orçamento foi criado:\n" +
                                                                "- Código: %d\n" +
                                                                "- Valor: R$ %.2f\n" +
                                                                "- Mecânico: %s\n" +
                                                                "- Veículo: %s\n" +
                                                                "- Serviço: %s",
                                                salvo.getCliente().getNome(),
                                                salvo.getNumero(),
                                                salvo.getValorAjustado(),
                                                salvo.getMecanico().getUsername(),
                                                salvo.getVeiculo().getModelo(),
                                                salvo.getServico().getNome());
                                whatsAppService.enviarMensagem(celularCliente, mensagem);
                        }
                } catch (Exception e) {
                        System.err.println("Erro ao enviar mensagem WhatsApp: " + e.getMessage());
                }

                return new OrcamentoDTO(salvo);
        }

        public List<OrcamentoDTO> listarTodos() {
                Usuarios usuario = getUsuarioLogado();
                UUID empresaId = usuario.getEmpresa().getId();

                List<Orcamento> orcamentos;

                if (usuario.getRole() == RoleUsuario.ADMIN) {
                        orcamentos = orcamentoRepository.findByEmpresaIdOrderByStatusDesc(empresaId);
                } else {
                        orcamentos = orcamentoRepository
                                        .findByMecanicoUsernameAndEmpresaIdOrderByStatusDesc(usuario.getUsername(),
                                                        empresaId);
                }

                return orcamentos.stream()
                                .map(OrcamentoDTO::new)
                                .toList();
        }

        public List<OrcamentoDTO> listarPorMecanico() {
                Usuarios usuario = getUsuarioLogado();
                UUID empresaId = usuario.getEmpresa().getId();

                List<Orcamento> orcamentos;

                if (usuario.getRole() == RoleUsuario.ADMIN) {
                        orcamentos = orcamentoRepository.findByEmpresaIdOrderByStatusDesc(empresaId);
                } else {
                        orcamentos = orcamentoRepository.findByMecanicoUsernameAndEmpresaIdOrderByStatusDesc(
                                        usuario.getUsername(),
                                        empresaId);
                }

                return orcamentos.stream()
                                .map(OrcamentoDTO::new)
                                .toList();
        }

        public OrcamentoDTO listarPorId(Long numero) {
                Usuarios usuario = getUsuarioLogado();
                UUID empresaId = usuario.getEmpresa().getId();

                Orcamento orcamento = orcamentoRepository
                                .findByNumeroAndEmpresaId(numero, empresaId)
                                .orElseThrow(() -> new NaoEncontradoException("Orçamento não encontrado"));

                if (usuario.getRole() != RoleUsuario.ADMIN &&
                                !orcamento.getMecanico().getUsername().equals(usuario.getUsername())) {
                        throw new NaoEncontradoException("Orçamento não encontrado ou não pertence a este usuário");
                }

                return new OrcamentoDTO(orcamento);
        }

}
