package com.fag.Autofinance.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fag.Autofinance.dto.OrcamentoDTO;
import com.fag.Autofinance.entities.Orcamento;
import com.fag.Autofinance.services.OrcamentoService;

@RestController
@RequestMapping()
public class OrcamentoController {
    private final OrcamentoService orcamentoService;

    public OrcamentoController(OrcamentoService orcamentoService) {
        this.orcamentoService = orcamentoService;
    }

    @PostMapping
    public ResponseEntity<Orcamento> criarOrcamento(
            @RequestBody Orcamento orcamento,
            Authentication authentication) {
        return ResponseEntity.ok(orcamentoService.criarOrcamento(orcamento, authentication.getName()));
    }

    @GetMapping
    public ResponseEntity<List<OrcamentoDTO>> listarTodos() {
        return ResponseEntity.ok(orcamentoService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrcamentoDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(orcamentoService.listarPorId(id));
    }

}
