package com.fag.Autofinance.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.fag.Autofinance.entities.Cliente;
import com.fag.Autofinance.repositories.ClienteRepository;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    public Cliente salvarCliente(Cliente cliente) {
        String cpfCnpjLimpo = cliente.getCpfCnpj().replaceAll("[^0-9]", "");
        cliente.setCpfCnpj(cpfCnpjLimpo);

        return clienteRepository.save(cliente);
    }

    public List<Cliente> listarClientes() {
        return clienteRepository.findAll();
    }

    public Cliente buscarClientePorCpfCnpj(String cpfCnpj) {
        return clienteRepository.findByCpfCnpj(cpfCnpj)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
    }

    public Cliente atualizarCliente(String cpfCnpj, Cliente clienteAtualizado) {
        Cliente clienteExistente = clienteRepository.findByCpfCnpj(cpfCnpj)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        clienteExistente.setNome(clienteAtualizado.getNome());
        clienteExistente.setCelular(clienteAtualizado.getCelular());

        return clienteRepository.save(clienteExistente);
    }

    public void deletarCliente(String cpfCnpj) {
        clienteRepository.deleteById(cpfCnpj);
    }

}
