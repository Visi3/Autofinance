package com.fag.Autofinance.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    /*
     * public Cliente salvarCliente(Cliente cliente) {
     * String cpfCnpjLimpo = cliente.getCpfCnpj().replaceAll("[^0-9]", "");
     * cliente.setCpfCnpj(cpfCnpjLimpo);
     * 
     * return clienteRepository.save(cliente);
     * }
     * 
     * public List<Cliente> listarClientes() {
     * return clienteRepository.findAll();
     * }
     * 
     * public Cliente buscarClientePorCpfCnpj(String cpfCnpj) {
     * return clienteRepository.findByCpfCnpj(cpfCnpj)
     * .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
     * }
     * 
     * public Cliente atualizarCliente(String cpfCnpj, Cliente clienteAtualizado) {
     * Cliente clienteExistente = clienteRepository.findByCpfCnpj(cpfCnpj)
     * .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
     * 
     * clienteExistente.setNome(clienteAtualizado.getNome());
     * clienteExistente.setCelular(clienteAtualizado.getCelular());
     * 
     * return clienteRepository.save(clienteExistente);
     * }
     * 
     * public void deletarCliente(String cpfCnpj) {
     * clienteRepository.deleteById(cpfCnpj);
     * }
     */

}
