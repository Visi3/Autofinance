package com.fag.Autofinance.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fag.Autofinance.entities.Veiculo;
import com.fag.Autofinance.services.VeiculoService;

@RestController
@RequestMapping("/veiculos")
public class VeiculoController {

    private final VeiculoService veiculoService;

    public VeiculoController(VeiculoService veiculoService) {
        this.veiculoService = veiculoService;
    }

    @PostMapping
    public ResponseEntity<Veiculo> criarOuAtualizarVeiculo(@RequestBody Veiculo veiculo) {
        System.out.println("Recebido no Controller: " + veiculo.getPlaca());
        Veiculo veiculoSalvo = veiculoService.salvarVeiculo(veiculo);
        return new ResponseEntity<>(veiculoSalvo, HttpStatus.CREATED);
    }

    @GetMapping("/{placa}")
    public ResponseEntity<Veiculo> buscarVeiculo(@PathVariable String placa) {
        Veiculo veiculo = veiculoService.buscarPorPlaca(placa);
        return ResponseEntity.ok(veiculo);
    }

    @GetMapping
    public List<Veiculo> listarVeiculos() {
        return veiculoService.listarVeiculos();
    }

    @PutMapping("/{placa}")
    public ResponseEntity<Veiculo> atualizarVeiculo(@PathVariable String placa, @RequestBody Veiculo veiculo) {
        Veiculo atualizado = veiculoService.atualizarVeiculo(placa, veiculo);
        return ResponseEntity.ok(atualizado);
    }

    @DeleteMapping("/{placa}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletarCliente(@PathVariable String placa) {
        veiculoService.deletarVeiculo(placa);
    }
}
