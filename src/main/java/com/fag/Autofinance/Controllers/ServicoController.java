package com.fag.Autofinance.controllers;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fag.Autofinance.dto.ServicoDTO;
import com.fag.Autofinance.entities.Servico;
import com.fag.Autofinance.entities.Veiculo;
import com.fag.Autofinance.enums.StatusCadastros;
import com.fag.Autofinance.services.ServicoService;

@RestController
@RequestMapping("/servicos")
public class ServicoController {

    private final ServicoService servicoService;

    public ServicoController(ServicoService servicoService) {
        this.servicoService = servicoService;
    }

    @GetMapping
    public Page<ServicoDTO> listarTodos(Pageable pageable) {
        return servicoService.listarTodos(pageable);
    }

    @GetMapping("/ativos")
    public Page<ServicoDTO> listarAtivos(Pageable pageable) {
        return servicoService.listarPorStatus(StatusCadastros.ATIVO, pageable);
    }

    @GetMapping("/inativos")
    public Page<ServicoDTO> listarInativos(Pageable pageable) {
        return servicoService.listarPorStatus(StatusCadastros.INATIVO, pageable);
    }

    @PostMapping
    public ResponseEntity<Servico> criar(@RequestBody Servico servico) {
        Servico salvo = servicoService.criar(servico);
        return ResponseEntity.ok(salvo);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Servico> atualizar(@PathVariable Long id, @RequestBody Servico servico) {
        Servico atualizado = servicoService.atualizar(id, servico);
        return ResponseEntity.ok(atualizado);
    }

}
