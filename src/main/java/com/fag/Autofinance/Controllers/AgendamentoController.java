package com.fag.Autofinance.controllers;

import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
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

import com.fag.Autofinance.dto.AgendamentoDTO;
import com.fag.Autofinance.services.AgendamentoService;

@RestController
@RequestMapping("/agendamentos")
public class AgendamentoController {
    private final AgendamentoService agendamentoService;

    public AgendamentoController(AgendamentoService agendamentoService) {
        this.agendamentoService = agendamentoService;
    }

    @PostMapping("/criar/{numeroOrdem}")
    public ResponseEntity<AgendamentoDTO> criarAgendamento(
            @PathVariable Long numeroOrdem,
            @RequestBody AgendamentoDTO request) {

        AgendamentoDTO dto = agendamentoService.criarAgendamento(
                numeroOrdem,
                request.getDataAgendada(),
                request.getObservacoes(),
                request.getMecanicoUsername());

        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @PutMapping("/atualizar/{numero}")
    public ResponseEntity<AgendamentoDTO> atualizarAgendamento(
            @PathVariable Integer numero,
            @RequestBody AgendamentoDTO dto) {
        AgendamentoDTO atualizado = agendamentoService.atualizarAgendamento(numero, dto);
        return ResponseEntity.ok(atualizado);
    }

    @GetMapping
    public ResponseEntity<List<AgendamentoDTO>> listarTodos() {
        return ResponseEntity.ok(agendamentoService.listarTodos());
    }

    @GetMapping("/cliente")
    public ResponseEntity<List<AgendamentoDTO>> listarPorCliente(@RequestParam("nome") String nomeCliente) {
        return ResponseEntity.ok(agendamentoService.listarPorCliente(nomeCliente));
    }

    @GetMapping("/data")
    public ResponseEntity<List<AgendamentoDTO>> listarPorData(
            @RequestParam("data") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return ResponseEntity.ok(agendamentoService.listarPorData(data));
    }
}
