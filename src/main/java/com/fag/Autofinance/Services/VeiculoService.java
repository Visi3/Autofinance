package com.fag.Autofinance.services;

import java.util.List;
import java.util.UUID;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.fag.Autofinance.dto.VeiculoDTO;
import com.fag.Autofinance.entities.Cliente;
import com.fag.Autofinance.entities.Usuarios;
import com.fag.Autofinance.entities.Veiculo;
import com.fag.Autofinance.enums.StatusCadastros;
import com.fag.Autofinance.exception.JaExisteException;
import com.fag.Autofinance.exception.NaoEncontradoException;
import com.fag.Autofinance.exception.ValidarException;
import com.fag.Autofinance.repositories.ClienteRepository;
import com.fag.Autofinance.repositories.UsuarioRepository;
import com.fag.Autofinance.repositories.VeiculoRepository;

import jakarta.transaction.Transactional;

@Service
public class VeiculoService {

    private final VeiculoRepository veiculoRepository;
    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;

    public VeiculoService(VeiculoRepository veiculoRepository, ClienteRepository clienteRepository,
            UsuarioRepository usuarioRepository) {
        this.veiculoRepository = veiculoRepository;
        this.clienteRepository = clienteRepository;
        this.usuarioRepository = usuarioRepository;
    }

    private Usuarios getUsuarioLogado() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new NaoEncontradoException("Usuário autenticado não encontrado"));
    }

    private String padronizarPlaca(String placa) {
        if (placa == null) {
            return null;
        }
        return placa.toUpperCase().replaceAll("[^A-Z0-9]", "");
    }

    public List<VeiculoDTO> listarPorCpfCnpj(String cpfCnpj) {
        UUID empresaId = getUsuarioLogado().getEmpresa().getId();
        return veiculoRepository.findByCliente_CpfCnpjAndEmpresaIdOrderByStatusCustom(cpfCnpj, empresaId)
                .stream()
                .map(VeiculoDTO::new)
                .toList();
    }

    public List<VeiculoDTO> listarPorNome(String nome) {
        UUID empresaId = getUsuarioLogado().getEmpresa().getId();
        return veiculoRepository.findByCliente_NomeContainingIgnoreCaseAndEmpresaIdOrderByStatusCustom(nome, empresaId)
                .stream()
                .map(VeiculoDTO::new)
                .toList();
    }

    public List<VeiculoDTO> listarTodos() {
        UUID empresaId = getUsuarioLogado().getEmpresa().getId();
        return veiculoRepository.findAllByEmpresaIdOrderByStatusCustom(empresaId)
                .stream()
                .map(VeiculoDTO::new)
                .toList();
    }

    public List<VeiculoDTO> listarPorStatus(StatusCadastros status) {
        UUID empresaId = getUsuarioLogado().getEmpresa().getId();
        return veiculoRepository.findByStatusAndEmpresaId(status, empresaId)
                .stream()
                .map(VeiculoDTO::new)
                .toList();
    }

    public VeiculoDTO listarPorPlaca(String placa) {
        UUID empresaId = getUsuarioLogado().getEmpresa().getId();
        String placaPadronizada = padronizarPlaca(placa);

        Veiculo veiculo = veiculoRepository.findByPlacaAndEmpresaId(placaPadronizada, empresaId)
                .orElseThrow(() -> new NaoEncontradoException("Veículo não encontrado com essa placa."));

        return new VeiculoDTO(veiculo);
    }

    @Transactional
    public Veiculo criar(Veiculo veiculo) {
        Usuarios usuarioLogado = getUsuarioLogado();
        UUID empresaId = usuarioLogado.getEmpresa().getId();

        String placaPadronizada = padronizarPlaca(veiculo.getPlaca());
        if (placaPadronizada == null || placaPadronizada.isBlank()) {
            throw new ValidarException("Placa é obrigatória.");
        }

        if (veiculoRepository.existsByPlacaAndEmpresaId(placaPadronizada, empresaId)) {
            throw new JaExisteException("Veículo com essa placa já está cadastrado.");
        }
        veiculo.setPlaca(placaPadronizada);

        if (veiculo.getCliente() == null || veiculo.getCliente().getCpfCnpj() == null) {
            throw new ValidarException("Cliente com CPF/CNPJ é obrigatório.");
        }
        Cliente cliente = clienteRepository.findByCpfCnpjAndEmpresaId(
                veiculo.getCliente().getCpfCnpj(),
                empresaId)
                .orElseThrow(() -> new NaoEncontradoException("Cliente não encontrado com CPF/CNPJ informado."));

        veiculo.setCliente(cliente);
        veiculo.setEmpresa(usuarioLogado.getEmpresa());
        veiculo.setStatus(StatusCadastros.ATIVO);

        return veiculoRepository.save(veiculo);
    }

    @Transactional
    public Veiculo atualizar(String placa, Veiculo veiculoAtualizado) {
        UUID empresaId = getUsuarioLogado().getEmpresa().getId();
        String placaPadronizada = padronizarPlaca(placa);

        Veiculo existente = veiculoRepository.findByPlacaAndEmpresaId(placaPadronizada, empresaId)
                .orElseThrow(() -> new NaoEncontradoException("Veículo não encontrado com essa placa."));

        if (veiculoAtualizado.getModelo() != null) {
            existente.setModelo(veiculoAtualizado.getModelo());
        }
        if (veiculoAtualizado.getMarca() != null) {
            existente.setMarca(veiculoAtualizado.getMarca());
        }

        if (veiculoAtualizado.getStatus() != null) {
            existente.setStatus(veiculoAtualizado.getStatus());
        }

        if (veiculoAtualizado.getCliente() != null && veiculoAtualizado.getCliente().getCpfCnpj() != null) {
            Cliente cliente = clienteRepository.findByCpfCnpjAndEmpresaId(
                    veiculoAtualizado.getCliente().getCpfCnpj(),
                    empresaId)
                    .orElseThrow(() -> new NaoEncontradoException("Cliente não encontrado com CPF/CNPJ informado."));
            existente.setCliente(cliente);
        }

        return veiculoRepository.save(existente);
    }
}
