package com.fag.Autofinance.services;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.stereotype.Service;

import com.fag.Autofinance.dto.VeiculoDTO;
import com.fag.Autofinance.entities.Cliente;
import com.fag.Autofinance.entities.Veiculo;
import com.fag.Autofinance.enums.StatusCadastros;
import com.fag.Autofinance.exception.JaExisteException;
import com.fag.Autofinance.exception.NaoEncontradoException;
import com.fag.Autofinance.repositories.ClienteRepository;
import com.fag.Autofinance.repositories.VeiculoRepository;

@Service
public class VeiculoService {

    private final VeiculoRepository veiculoRepository;

    private final ClienteRepository clienteRepository;

    public VeiculoService(VeiculoRepository veiculoRepository, ClienteRepository clienteRepository) {
        this.veiculoRepository = veiculoRepository;
        this.clienteRepository = clienteRepository;
    }

    public Page<VeiculoDTO> listarTodos(Pageable pageable) {
        return veiculoRepository.findAll(pageable)
                .map(VeiculoDTO::new);
    }

    public Page<VeiculoDTO> listarPorStatus(StatusCadastros status, Pageable pageable) {
        return veiculoRepository.findByStatus(status, pageable)
                .map(VeiculoDTO::new);
    }

    public Page<VeiculoDTO> listarPorCpfCnpj(String cpfCnpj, Pageable pageable) {
        return veiculoRepository.findByCliente_CpfCnpj(cpfCnpj, pageable)
                .map(VeiculoDTO::new);
    }

    public Page<VeiculoDTO> listarPorNome(String nome, Pageable pageable) {
        return veiculoRepository.findByCliente_NomeContainingIgnoreCase(nome, pageable)
                .map(VeiculoDTO::new);
    }

    public VeiculoDTO listarPorPlaca(String placa) {
        Veiculo veiculo = veiculoRepository.findByPlaca(placa)
                .orElseThrow(() -> new NaoEncontradoException("Veículo não encontrado com essa placa."));
        return new VeiculoDTO(veiculo);
    }

    public Veiculo criar(Veiculo veiculo) {

        if (veiculoRepository.findByPlaca(veiculo.getPlaca()).isPresent()) {
            throw new JaExisteException("Veículo com essa placa já está cadastrado.");
        }

        if (veiculo.getCliente() == null || veiculo.getCliente().getCpfCnpj() == null) {
            throw new IllegalArgumentException("Cliente com CPF/CNPJ é obrigatório.");
        }

        Cliente cliente = clienteRepository.findById(veiculo.getCliente().getCpfCnpj())
                .orElseThrow(() -> new NaoEncontradoException("Cliente não encontrado com CPF/CNPJ informado."));

        veiculo.setCliente(cliente);

        return veiculoRepository.save(veiculo);
    }

    public Veiculo atualizar(String placa, Veiculo veiculoAtualizado) {
        Veiculo existente = veiculoRepository.findByPlaca(placa)
                .orElseThrow(() -> new NaoEncontradoException("Veículo com a placa " + placa + " não encontrado."));

        clienteRepository.findById(veiculoAtualizado.getCliente().getCpfCnpj())
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
        existente.setCliente(veiculoAtualizado.getCliente());

        return veiculoRepository.save(existente);
    }

}
