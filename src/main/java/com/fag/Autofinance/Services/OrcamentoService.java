package com.fag.Autofinance.services;

import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.fag.Autofinance.dto.OrcamentoDTO;
import com.fag.Autofinance.entities.Cliente;
import com.fag.Autofinance.entities.Orcamento;
import com.fag.Autofinance.entities.Servico;
import com.fag.Autofinance.entities.Usuarios;
import com.fag.Autofinance.entities.Veiculo;
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

    public OrcamentoService(
            OrcamentoRepository orcamentoRepository,
            ClienteRepository clienteRepository,
            VeiculoRepository veiculoRepository,
            ServicoRepository servicoRepository,
            UsuarioRepository usuarioRepository) {
        this.orcamentoRepository = orcamentoRepository;
        this.clienteRepository = clienteRepository;
        this.veiculoRepository = veiculoRepository;
        this.servicoRepository = servicoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public Orcamento criarOrcamento(Orcamento orcamento, String usernameCriador) {

        usernameCriador = SecurityContextHolder.getContext().getAuthentication().getName();

        Usuarios mecanico = usuarioRepository.findByUsername(usernameCriador)
                .orElseThrow(() -> new RuntimeException("Usuário autenticado não encontrado"));

        orcamento.setMecanico(mecanico);

        Cliente cliente = clienteRepository.findByCpfCnpj(orcamento.getCliente().getCpfCnpj())
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
        orcamento.setCliente(cliente);

        Veiculo veiculo = veiculoRepository.findByPlaca(orcamento.getVeiculo().getPlaca())
                .orElseThrow(() -> new RuntimeException("Veículo não encontrado"));
        orcamento.setVeiculo(veiculo);

        Servico servico = servicoRepository.findByNomeContainingIgnoreCase(orcamento.getServico().getNome())
                .orElseThrow(() -> new RuntimeException("Serviço não encontrado"));
        orcamento.setServico(servico);

        return orcamentoRepository.save(orcamento);
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
