package com.fag.Autofinance.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fag.Autofinance.entities.Cliente;
import com.fag.Autofinance.services.ClienteService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    /*
     * @GetMapping
     * public List<Cliente> listaTodos() {
     * return clienteService.listarTodos();
     * }
     * 
     * 
     * @PostMapping
     * public ResponseEntity<Cliente> criarOuAtualizarCliente(@RequestBody Cliente
     * cliente) {
     * Cliente clienteSalvo = clienteService.salvarCliente(cliente);
     * return new ResponseEntity<>(clienteSalvo, HttpStatus.CREATED);
     * }
     * 
     * @GetMapping("/{cpfCnpj}")
     * public ResponseEntity<Cliente> buscarClientePorCpfCnpj(@PathVariable String
     * cpfCnpj) {
     * Cliente cliente = clienteService.buscarClientePorCpfCnpj(cpfCnpj);
     * return ResponseEntity.ok(cliente);
     * }
     * 
     * @GetMapping
     * public List<Cliente> listarClientes() {
     * return clienteService.listarClientes();
     * }
     * 
     * @PutMapping("/{cpfCnpj}")
     * public ResponseEntity<Cliente> atualizarCliente(@PathVariable String
     * cpfCnpj, @RequestBody @Valid Cliente cliente) {
     * Cliente atualizado = clienteService.atualizarCliente(cpfCnpj, cliente);
     * return ResponseEntity.ok(atualizado);
     * }
     * 
     * @DeleteMapping("/{cpfCnpj}")
     * 
     * @ResponseStatus(HttpStatus.NO_CONTENT)
     * public void deletarCliente(@PathVariable String cpfCnpj) {
     * clienteService.deletarCliente(cpfCnpj);
     * }
     */
}
