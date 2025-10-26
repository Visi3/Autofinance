package com.fag.Autofinance.services;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.fag.Autofinance.dto.VeiculoDTO;
import com.fag.Autofinance.entities.Cliente;
import com.fag.Autofinance.entities.Usuarios;
import com.fag.Autofinance.entities.Veiculo;
import com.fag.Autofinance.enums.StatusCadastros;
import com.fag.Autofinance.exception.JaExisteException;
import com.fag.Autofinance.exception.NaoEncontradoException;
import com.fag.Autofinance.repositories.ClienteRepository;
import com.fag.Autofinance.repositories.UsuarioRepository;
import com.fag.Autofinance.repositories.VeiculoRepository;

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
        return veiculoRepository.findByStatusAndEmpresaIdOrderByStatusCustom(status, empresaId)
                .stream()
                .map(VeiculoDTO::new)
                .toList();
    }

    public VeiculoDTO listarPorPlaca(String placa) {
        UUID empresaId = getUsuarioLogado().getEmpresa().getId();
        Veiculo veiculo = veiculoRepository.findByPlacaAndEmpresaId(placa, empresaId)
                .orElseThrow(() -> new NaoEncontradoException("Veículo não encontrado com essa placa."));
        return new VeiculoDTO(veiculo);
    }

    public Veiculo criar(Veiculo veiculo) {
        Usuarios usuarioLogado = getUsuarioLogado();

        if (veiculoRepository.existsByPlacaAndEmpresaId(veiculo.getPlaca(), usuarioLogado.getEmpresa().getId())) {
            throw new JaExisteException("Veículo com essa placa já está cadastrado.");
        }

        if (veiculo.getCliente() == null || veiculo.getCliente().getCpfCnpj() == null) {
            throw new IllegalArgumentException("Cliente com CPF/CNPJ é obrigatório.");
        }

        Cliente cliente = clienteRepository.findByCpfCnpjAndEmpresaId(
                veiculo.getCliente().getCpfCnpj(),
                usuarioLogado.getEmpresa().getId())
                .orElseThrow(() -> new NaoEncontradoException("Cliente não encontrado com CPF/CNPJ informado."));

        veiculo.setCliente(cliente);
        veiculo.setEmpresa(usuarioLogado.getEmpresa());
        veiculo.setStatus(StatusCadastros.ATIVO);

        return veiculoRepository.save(veiculo);
    }

    public Veiculo atualizar(String placa, Veiculo veiculoAtualizado) {
        UUID empresaId = getUsuarioLogado().getEmpresa().getId();

        Veiculo existente = veiculoRepository.findByPlacaAndEmpresaId(placa, empresaId)
                .orElseThrow(() -> new NaoEncontradoException("Veículo não encontrado com essa placa."));

        Cliente cliente = clienteRepository.findByCpfCnpjAndEmpresaId(
                veiculoAtualizado.getCliente().getCpfCnpj(),
                empresaId)
                .orElseThrow(() -> new NaoEncontradoException("Cliente não encontrado com CPF/CNPJ informado."));

        existente.setModelo(veiculoAtualizado.getModelo());
        existente.setMarca(veiculoAtualizado.getMarca());
        existente.setAno(veiculoAtualizado.getAno());
        existente.setCor(veiculoAtualizado.getCor());
        existente.setRenavam(veiculoAtualizado.getRenavam());
        existente.setChassi(veiculoAtualizado.getChassi());
        existente.setCombustivel(veiculoAtualizado.getCombustivel());
        existente.setQuilometragem(veiculoAtualizado.getQuilometragem());
        existente.setObservacoes(veiculoAtualizado.getObservacoes());
        existente.setStatus(veiculoAtualizado.getStatus());
        existente.setCliente(cliente);

        return veiculoRepository.save(existente);
    }

}
