package com.fag.Autofinance.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.fag.Autofinance.entities.Cliente;
import com.fag.Autofinance.enums.StatusCadastros;
import com.fag.Autofinance.repositories.ClienteRepository;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    public List<Cliente> listarTodos() {
        return clienteRepository.findAll();
    }

    public List<Cliente> listarPorStatus(StatusCadastros status) {
        return clienteRepository.findByStatus(status);
    }

    public Cliente listarPorCpfCnpj(String cpfCnpj) {
        return clienteRepository.findByCpfCnpj(cpfCnpj)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
    }

    public Cliente criar(Cliente cliente) {

        /*
         * if (clienteRepository.existsById(cliente.getCpfCnpj())) {
         * throw new ResponseStatusException(
         * HttpStatus.CONFLICT,
         * "Já existe um cliente com esse CPF/CNPJ.");
         * }
         */

        cliente.setStatus(StatusCadastros.ATIVO);
        return clienteRepository.save(cliente);
    }

    public Cliente atualizar(String cpfCnpj, Cliente clienteAtualizado) {
        Cliente clienteExistente = clienteRepository.findByCpfCnpj(cpfCnpj)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
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
