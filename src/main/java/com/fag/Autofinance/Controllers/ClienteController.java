package com.fag.Autofinance.controllers;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.fag.Autofinance.dto.ClienteDTO;
import com.fag.Autofinance.entities.Cliente;
import com.fag.Autofinance.enums.StatusCadastros;
import com.fag.Autofinance.services.ClienteService;

@RestController
@RequestMapping("/clientes")
public class ClienteController {

    @Autowired
    private ClienteService clienteService;

    @GetMapping
    public List<ClienteDTO> listarTodos() {
        return clienteService.listarTodos();
    }

    @GetMapping("/ativos")
    public List<ClienteDTO> listarAtivos() {
        return clienteService.listarPorStatus(StatusCadastros.ATIVO);
    }

    @GetMapping("/inativos")
    public List<ClienteDTO> listarInativos() {
        return clienteService.listarPorStatus(StatusCadastros.INATIVO);
    }

    @GetMapping("/{cpfCnpj}")
    public ResponseEntity<ClienteDTO> listarPorCpfCnpj(@PathVariable String cpfCnpj) {
        return ResponseEntity.ok(clienteService.listarPorCpfCnpj(cpfCnpj));
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

}
