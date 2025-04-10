package com.fag.Autofinance.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fag.Autofinance.entities.Cliente;
import com.fag.Autofinance.services.ClienteService;

@RestController
@RequestMapping("/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @PostMapping
    public ResponseEntity<Cliente> criarOuAtualizarCliente(@RequestBody Cliente cliente) {
        Cliente clienteSalvo = clienteService.salvarCliente(cliente);
        return new ResponseEntity<>(clienteSalvo, HttpStatus.CREATED);
    }

    @GetMapping
    public List<Cliente> listarClientes() {
        return clienteService.listarClientes();
    }

    @DeleteMapping("/{cpfCnpj}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletarCliente(@PathVariable String cpfCnpj) {
        clienteService.deletarCliente(cpfCnpj);
    }
}
