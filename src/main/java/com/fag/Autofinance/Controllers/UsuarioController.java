package com.fag.Autofinance.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fag.Autofinance.entities.Usuarios;
import com.fag.Autofinance.enums.StatusCadastros;
import com.fag.Autofinance.services.UsuarioService;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private AuthenticationManager authenticationManager;
    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MECANICO')")
    public List<Usuarios> listarTodos() {
        return usuarioService.listarTodos();
    }

    @GetMapping("/ativos")
    @PreAuthorize("hasAnyRole('ADMIN', 'MECANICO')")
    public List<Usuarios> listarAtivos() {
        return usuarioService.listarPorStatus(StatusCadastros.ATIVO);
    }

    @GetMapping("/inativos")
    @PreAuthorize("hasAnyRole('ADMIN', 'MECANICO')")
    public List<Usuarios> listarInativos() {
        return usuarioService.listarPorStatus(StatusCadastros.INATIVO);
    }

    @GetMapping("/{username}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MECANICO')")
    public ResponseEntity<Usuarios> listarPorUsername(@PathVariable String username) {
        return ResponseEntity.ok(usuarioService.listarPorUsername(username));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Usuarios> criar(@RequestBody Usuarios usuario) {
        Usuarios salvo = usuarioService.criar(usuario);
        return new ResponseEntity<>(salvo, HttpStatus.CREATED);
    }

    @PutMapping("/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Usuarios> atualizar(
            @PathVariable String username,
            @RequestBody Usuarios usuario) {
        Usuarios atualizado = usuarioService.atualizar(username, usuario);
        return ResponseEntity.ok(atualizado);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody String email, @RequestBody String senha) {
        try {
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(email, senha);

            authenticationManager.authenticate(authToken);
            return ResponseEntity.ok("Login realizado com sucesso!");
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Email ou senha inválidos!");
        }
    }

}
