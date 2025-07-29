package com.fag.Autofinance.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fag.Autofinance.entities.Cliente;
import com.fag.Autofinance.enums.StatusCadastros;
import com.fag.Autofinance.services.ClienteService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/clientes")
public class ClienteController {

    @Autowired
    private ClienteService clienteService;

    @GetMapping
    public List<Cliente> listarTodos() {
        return clienteService.listarTodos();
    }

    @GetMapping("/ativos")
    public List<Cliente> listarAtivos() {
        return clienteService.listarPorStatus(StatusCadastros.ATIVO);
    }

    @GetMapping("/inativos")
    public List<Cliente> listarInativos() {
        return clienteService.listarPorStatus(StatusCadastros.INATIVO);
    }

    @GetMapping("/{cpfCnpj}")
    public ResponseEntity<Cliente> listarPorCpfCnpj(@PathVariable String cpfCnpj) {
        Cliente cliente = clienteService.listarPorCpfCnpj(cpfCnpj);
        return ResponseEntity.ok(cliente);
    }

    @PostMapping
    public ResponseEntity<Cliente> criar(@RequestBody Cliente cliente) {
        Cliente salvo = clienteService.criar(cliente);
        return new ResponseEntity<>(salvo, HttpStatus.CREATED);
    }

    @PutMapping("/{cpfCnpj}")
    public ResponseEntity<Cliente> criar(@PathVariable String cpfCnpj, @RequestBody Cliente cliente) {
        Cliente atualizado = clienteService.atualizar(cpfCnpj, cliente);
        return ResponseEntity.ok(atualizado);
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
