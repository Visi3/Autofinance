package com.fag.Autofinance.controllers;

import java.util.List;
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
    public ResponseEntity<List<VeiculoDTO>> listarTodos() {
        List<VeiculoDTO> veiculos = veiculoService.listarTodos();
        return ResponseEntity.ok(veiculos);
    }

    @GetMapping("/ativos")
    public ResponseEntity<List<VeiculoDTO>> listarAtivos() {
        List<VeiculoDTO> veiculos = veiculoService.listarPorStatus(StatusCadastros.ATIVO);
        return ResponseEntity.ok(veiculos);
    }

    @GetMapping("/inativos")
    public ResponseEntity<List<VeiculoDTO>> listarInativos() {
        List<VeiculoDTO> veiculos = veiculoService.listarPorStatus(StatusCadastros.INATIVO);
        return ResponseEntity.ok(veiculos);
    }

    @GetMapping("/cpfcnpj/{cpfCnpj}")
    public ResponseEntity<List<VeiculoDTO>> listarPorCpfCnpj(@PathVariable String cpfCnpj) {
        List<VeiculoDTO> veiculos = veiculoService.listarPorCpfCnpj(cpfCnpj);
        return ResponseEntity.ok(veiculos);
    }

    @GetMapping("/nome/{nome}")
    public ResponseEntity<List<VeiculoDTO>> listarPorNome(@PathVariable String nome) {
        List<VeiculoDTO> veiculos = veiculoService.listarPorNome(nome);
        return ResponseEntity.ok(veiculos);
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
