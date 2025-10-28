package com.fag.Autofinance.controllers;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fag.Autofinance.dto.OrdemServicoDTO;
import com.fag.Autofinance.entities.OrdemServico;
import com.fag.Autofinance.services.OrdemServicoService;

@RestController
@RequestMapping("/ordens-servico")
public class OrdemServicoController {

    private final OrdemServicoService ordemServicoService;

    public OrdemServicoController(OrdemServicoService ordemServicoService) {
        this.ordemServicoService = ordemServicoService;
    }

    @PostMapping("/criar/orcamento/{numeroOrcamento}")
    public ResponseEntity<OrdemServicoDTO> criarComOrcamento(
            @PathVariable Long numeroOrcamento,
            @RequestBody(required = false) OrdemServico ordemInput) {

        OrdemServicoDTO dto = ordemServicoService.criarOrdemServico(numeroOrcamento, ordemInput);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @PostMapping("/criar")
    public ResponseEntity<OrdemServicoDTO> criarManual(@RequestBody OrdemServico ordemInput) {
        OrdemServicoDTO dto = ordemServicoService.criarOrdemServico(null, ordemInput);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @PutMapping("/{numeroOrdem}")
    public ResponseEntity<OrdemServicoDTO> atualizarOrdem(
            @PathVariable Long numeroOrdem,
            @RequestBody OrdemServicoDTO dto) {
        OrdemServicoDTO atualizado = ordemServicoService.atualizarOrdemServico(numeroOrdem, dto);
        return ResponseEntity.ok(atualizado);
    }

    @GetMapping
    public ResponseEntity<List<OrdemServicoDTO>> listarTodos() {
        List<OrdemServicoDTO> ordens = ordemServicoService.listarTodos();
        return ResponseEntity.ok(ordens);
    }

    @GetMapping("/{numeroOrdem}")
    public ResponseEntity<OrdemServicoDTO> listarPorId(@PathVariable Long numeroOrdem) {
        OrdemServicoDTO dto = ordemServicoService.listarPorId(numeroOrdem);
        return ResponseEntity.ok(dto);
    }
}