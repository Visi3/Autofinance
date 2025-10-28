package com.fag.Autofinance.services;

import java.util.List;
import java.util.UUID;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import com.fag.Autofinance.exception.NaoEncontradoException;
import com.fag.Autofinance.exception.ValidarException;
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
        public OrcamentoDTO atualizarOrcamento(Long numero, OrcamentoDTO orcamentoDTO) {
                Usuarios usuarioLogado = getUsuarioLogado();
                UUID empresaId = usuarioLogado.getEmpresa().getId();

                Orcamento orcamento = orcamentoRepository.findByNumeroAndEmpresaId(numero, empresaId)
                                .orElseThrow(() -> new NaoEncontradoException(
                                                "Orçamento não encontrado nesta empresa"));

                if (orcamentoDTO.getMecanicoUsername() != null) {
                        Usuarios mecanico = validarMecanico(orcamentoDTO.getMecanicoUsername(), empresaId);
                        orcamento.setMecanico(mecanico);
                }

                Servico servico = null;
                if (orcamentoDTO.getServicoNome() != null) {
                        servico = validarServico(orcamentoDTO.getServicoNome(), empresaId);
                        orcamento.setServico(servico);
                }

                if (orcamentoDTO.getValorAjustado() != null && orcamentoDTO.getValorAjustado() > 0) {
                        orcamento.setValorAjustado(orcamentoDTO.getValorAjustado());
                } else if (servico != null) {
                        orcamento.setValorAjustado(servico.getPreco());
                }

                if (orcamentoDTO.getStatus() != null) {
                        if (orcamentoDTO.getStatus() == StatusOrcamento.GERADO) {
                                throw new ValidarException(
                                                "O status GERADO só pode ser definido ao criar uma Ordem de Serviço.");
                        }
                        orcamento.setStatus(orcamentoDTO.getStatus());
                }

                Orcamento atualizado = orcamentoRepository.save(orcamento);

                enviarMensagemAtualizacao(atualizado);

                return new OrcamentoDTO(atualizado);
        }

        @Transactional
        public OrcamentoDTO criarOrcamento(Orcamento orcamento) {
                Usuarios usuarioLogado = getUsuarioLogado();
                Empresa empresa = usuarioLogado.getEmpresa();
                UUID empresaId = empresa.getId();

                String mecanicoUsername = (orcamento.getMecanico() != null)
                                ? orcamento.getMecanico().getUsername()
                                : usuarioLogado.getUsername();
                Usuarios mecanico = validarMecanico(mecanicoUsername, empresaId);
                orcamento.setMecanico(mecanico);

                orcamento.setEmpresa(empresa);

                String cpfCnpjFormatado = formatarCpfCnpj(orcamento.getCliente().getCpfCnpj());
                Cliente cliente = clienteRepository.findByCpfCnpjAndEmpresaId(cpfCnpjFormatado, empresaId)
                                .orElseThrow(() -> new NaoEncontradoException("Cliente não encontrado nesta empresa"));
                if (cliente.getStatus() != StatusCadastros.ATIVO) {
                        throw new ValidarException("Cliente inativo não pode receber orçamentos");
                }
                orcamento.setCliente(cliente);

                String placaPadronizada = padronizarPlaca(orcamento.getVeiculo().getPlaca());
                Veiculo veiculo = veiculoRepository.findByPlacaAndEmpresaId(placaPadronizada, empresaId)
                                .orElseThrow(() -> new NaoEncontradoException("Veículo não encontrado nesta empresa"));
                if (veiculo.getStatus() != StatusCadastros.ATIVO) {
                        throw new ValidarException("Veículo inativo não pode ser vinculado a um orçamento");
                }
                orcamento.setVeiculo(veiculo);

                Servico servico = validarServico(orcamento.getServico().getNome(), empresaId);
                orcamento.setServico(servico);

                double valorFinal = (orcamento.getValorAjustado() != null && orcamento.getValorAjustado() > 0)
                                ? orcamento.getValorAjustado()
                                : servico.getPreco();
                orcamento.setValorAjustado(valorFinal);

                Long proximoNumero = orcamentoRepository.findTopByEmpresaIdOrderByNumeroDesc(empresaId)
                                .map(o -> o.getNumero() + 1)
                                .orElse(1L);
                orcamento.setNumero(proximoNumero);

                Orcamento salvo = orcamentoRepository.save(orcamento);

                enviarMensagemCriacao(salvo);

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

        // --- MÉTODOS HELPER ---

        private Usuarios validarMecanico(String username, UUID empresaId) {
                Usuarios mecanico = usuarioRepository.findByUsername(username)
                                .orElseThrow(() -> new NaoEncontradoException("Mecânico não encontrado"));

                if (!mecanico.getEmpresa().getId().equals(empresaId)) {
                        throw new ValidarException("Mecânico não pertence a esta empresa");
                }
                if (mecanico.getStatus() != StatusCadastros.ATIVO) {
                        throw new ValidarException("Mecânico inativo não pode ser vinculado");
                }
                return mecanico;
        }

        private Servico validarServico(String nome, UUID empresaId) {

                Servico servico = servicoRepository.findByNomeContainingIgnoreCaseAndEmpresaId(nome, empresaId)
                                .orElseThrow(() -> new NaoEncontradoException("Serviço não encontrado nesta empresa"));

                if (servico.getStatus() != StatusCadastros.ATIVO) {
                        throw new ValidarException("Serviço inativo não pode ser utilizado");
                }
                if (servico.getPreco() == null || servico.getPreco() <= 0) {
                        throw new ValidarException("O serviço selecionado não possui valor cadastrado");
                }
                return servico;
        }

        private void enviarMensagemCriacao(Orcamento salvo) {
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
        }

        private void enviarMensagemAtualizacao(Orcamento salvo) {

                if (salvo.getStatus() == StatusOrcamento.GERADO) {
                        return;
                }

                try {
                        String celularCliente = salvo.getCliente().getCelular();
                        if (celularCliente != null && !celularCliente.isBlank()) {

                                String mensagem = String.format(
                                                "Olá %s! Seu orçamento nº %d foi *atualizado*:\n" +
                                                                "- Status: %s\n" +
                                                                "- Valor: R$ %.2f\n" +
                                                                "- Serviço: %s\n" +
                                                                "- Mecânico: %s",
                                                salvo.getCliente().getNome(),
                                                salvo.getNumero(),
                                                salvo.getStatus(),
                                                salvo.getValorAjustado(),
                                                salvo.getServico().getNome(),
                                                salvo.getMecanico().getUsername());

                                whatsAppService.enviarMensagem(celularCliente, mensagem);
                        }
                } catch (Exception e) {
                        System.err.println("Erro ao enviar mensagem WhatsApp de atualização: " + e.getMessage());
                }
        }
}
