package com.fag.Autofinance.Services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fag.Autofinance.Entities.Cliente;
import com.fag.Autofinance.Repositories.ClienteRepository;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    public Cliente salvarCliente(Cliente cliente) {
        String cpfCnpjLimpo = cliente.getCpfCnpj().replaceAll("[^0-9]", "");
        cliente.setCpfCnpj(cpfCnpjLimpo);

        return clienteRepository.save(cliente);
    }

    public List<Cliente> listarClientes() {
        return clienteRepository.findAll();
    }

    public void deletarCliente(String cpfCnpj) {
        clienteRepository.deleteById(cpfCnpj);
    }

}
