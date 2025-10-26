package com.fag.Autofinance.controllers;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fag.Autofinance.dto.OrcamentoDTO;
import com.fag.Autofinance.entities.Orcamento;
import com.fag.Autofinance.services.OrcamentoService;

@RestController
@RequestMapping("/orcamentos")
public class OrcamentoController {
    private final OrcamentoService orcamentoService;

    public OrcamentoController(OrcamentoService orcamentoService) {
        this.orcamentoService = orcamentoService;
    }

    @PostMapping
    public ResponseEntity<OrcamentoDTO> criarOrcamento(@RequestBody Orcamento orcamento) {
        OrcamentoDTO salvo = orcamentoService.criarOrcamento(orcamento);
        return ResponseEntity.status(HttpStatus.CREATED).body(salvo);
    }

    @PutMapping("/{numero}")
    public ResponseEntity<OrcamentoDTO> atualizarOrcamento(
            @PathVariable Long numero,
            @RequestBody OrcamentoDTO orcamentoDTO) {
        OrcamentoDTO atualizado = orcamentoService.atualizarOrcamento(numero, orcamentoDTO);
        return ResponseEntity.ok(atualizado);
    }

    @GetMapping
    public ResponseEntity<List<OrcamentoDTO>> listarTodos() {
        List<OrcamentoDTO> orcamentos = orcamentoService.listarTodos();
        return ResponseEntity.ok(orcamentos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrcamentoDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(orcamentoService.listarPorId(id));
    }

}
