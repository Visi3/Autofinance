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
import org.springframework.web.bind.annotation.RestController;

import com.fag.Autofinance.dto.VeiculoDTO;
import com.fag.Autofinance.entities.Veiculo;
import com.fag.Autofinance.enums.StatusCadastros;
import com.fag.Autofinance.services.VeiculoService;

@RestController
@RequestMapping("/veiculos")
public class VeiculoController {

    private final VeiculoService veiculoService;

    public VeiculoController(VeiculoService veiculoService) {
        this.veiculoService = veiculoService;
    }

    @GetMapping
    public Page<VeiculoDTO> listarTodos(Pageable pageable) {
        return veiculoService.listarTodos(pageable);
    }

    @GetMapping("/ativos")
    public Page<VeiculoDTO> listarAtivos(@PageableDefault(size = 10) Pageable pageable) {
        return veiculoService.listarPorStatus(StatusCadastros.ATIVO, pageable);
    }

    @GetMapping("/inativos")
    public Page<VeiculoDTO> listarInativos(@PageableDefault(size = 10) Pageable pageable) {
        return veiculoService.listarPorStatus(StatusCadastros.INATIVO, pageable);
    }

    @GetMapping("/cpfcnpj/{cpfCnpj}")
    public Page<VeiculoDTO> listarPorCpfCnpj(@PageableDefault(size = 10) @PathVariable String cpfCnpj,
            Pageable pageable) {
        return veiculoService.listarPorCpfCnpj(cpfCnpj, pageable);
    }

    @GetMapping("/nome/{nome}")
    public Page<VeiculoDTO> listarPorNome(@PageableDefault(size = 10) @PathVariable String nome, Pageable pageable) {
        return veiculoService.listarPorNome(nome, pageable);
    }

    @GetMapping("/{placa}")
    public ResponseEntity<VeiculoDTO> listarPorPlaca(@PathVariable String placa) {
        return ResponseEntity.ok(veiculoService.listarPorPlaca(placa));
    }

    @PostMapping
    public ResponseEntity<Veiculo> criar(@RequestBody Veiculo veiculo) {
        veiculoService.criar(veiculo);
        return ResponseEntity.ok(null);
    }

    @PutMapping("/{placa}")
    public ResponseEntity<Veiculo> atualizar(@PathVariable String placa, @RequestBody Veiculo veiculo) {
        Veiculo atualizado = veiculoService.atualizar(placa, veiculo);
        return ResponseEntity.ok(atualizado);
    }

}
