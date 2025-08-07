package com.fag.Autofinance.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import com.fag.Autofinance.dto.ClienteDTO;
import com.fag.Autofinance.entities.Cliente;
import com.fag.Autofinance.enums.StatusCadastros;
import com.fag.Autofinance.exception.JaExisteException;
import com.fag.Autofinance.exception.NaoEncontradoException;
import com.fag.Autofinance.repositories.ClienteRepository;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    public List<ClienteDTO> listarTodos() {
        return clienteRepository.findAll().stream().map(ClienteDTO::new).collect(Collectors.toList());
    }

    public List<ClienteDTO> listarPorStatus(StatusCadastros status) {
        return clienteRepository.findByStatus(status).stream().map(ClienteDTO::new).collect(Collectors.toList());
    }

    public ClienteDTO listarPorCpfCnpj(String cpfCnpj) {
        Cliente cliente = clienteRepository.findByCpfCnpj(cpfCnpj)
                .orElseThrow(() -> new NaoEncontradoException("Cliente não encontrado"));
        return new ClienteDTO(cliente);
    }

    public Cliente criar(Cliente cliente) {

        if (clienteRepository.existsByCpfCnpj(cliente.getCpfCnpj())) {
            throw new JaExisteException("Já existe um cliente com este CPF/CNPJ");
        }

        cliente.setStatus(StatusCadastros.ATIVO);
        return clienteRepository.save(cliente);
    }

    public Cliente atualizar(String cpfCnpj, Cliente clienteAtualizado) {
        Cliente clienteExistente = clienteRepository.findByCpfCnpj(cpfCnpj)
                .orElseThrow(() -> new NaoEncontradoException("Cliente não encontrado"));
        clienteExistente.setNome(clienteAtualizado.getNome());
        clienteExistente.setEmail(clienteAtualizado.getEmail());
        clienteExistente.setCelular(clienteAtualizado.getCelular());
        clienteExistente.setObservacoes(clienteAtualizado.getObservacoes());
        clienteExistente.setEndereco(clienteAtualizado.getEndereco());
        clienteExistente.setCep(clienteAtualizado.getCep());
        clienteExistente.setStatus(clienteAtualizado.getStatus());

        return clienteRepository.save(clienteExistente);
    }

}
