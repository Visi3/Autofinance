package com.fag.Autofinance.controllers;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fag.Autofinance.dto.OrdemServicoDTO;
import com.fag.Autofinance.services.OrdemServicoService;

@RestController
@RequestMapping("/ordens-servico")
public class OrdemServicoController {

    private final OrdemServicoService ordemServicoService;

    public OrdemServicoController(OrdemServicoService ordemServicoService) {
        this.ordemServicoService = ordemServicoService;
    }

    // Criar ordem de serviço a partir de um orçamento
    @PostMapping("/criar/{numeroOrcamento}")
    public ResponseEntity<OrdemServicoDTO> criarOrdem(@PathVariable Long numeroOrcamento) {
        OrdemServicoDTO dto = ordemServicoService.criarOrdemServico(numeroOrcamento);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    // Atualizar ordem de serviço
    @PutMapping("/{numeroOrdem}")
    public ResponseEntity<OrdemServicoDTO> atualizarOrdem(
            @PathVariable Long numeroOrdem,
            @RequestBody OrdemServicoDTO dto) {
        OrdemServicoDTO atualizado = ordemServicoService.atualizarOrdemServico(numeroOrdem, dto);
        return ResponseEntity.ok(atualizado);
    }

    // Listar todas as ordens de serviço (paginação)
    @GetMapping
    public ResponseEntity<Page<OrdemServicoDTO>> listarTodos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("numero").descending());
        Page<OrdemServicoDTO> ordens = ordemServicoService.listarTodos(pageable);
        return ResponseEntity.ok(ordens);
    }

    // Buscar ordem de serviço por número
    @GetMapping("/{numeroOrdem}")
    public ResponseEntity<OrdemServicoDTO> listarPorId(@PathVariable Long numeroOrdem) {
        OrdemServicoDTO dto = ordemServicoService.listarPorId(numeroOrdem);
        return ResponseEntity.ok(dto);
    }
}