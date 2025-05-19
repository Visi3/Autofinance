package com.fag.Autofinance.services;

import java.util.List;
import org.springframework.stereotype.Service;

import com.fag.Autofinance.entities.Veiculo;
import com.fag.Autofinance.repositories.VeiculoRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class VeiculoService {

    private final VeiculoRepository veiculoRepository;

    public VeiculoService(VeiculoRepository veiculoRepository) {
        this.veiculoRepository = veiculoRepository;
    }

    public Veiculo salvarVeiculo(Veiculo veiculo) {
        String placaLimpa = veiculo.getPlaca().replaceAll("[^A-Za-z0-9]", "");
        System.out.println(placaLimpa);
        veiculo.setPlaca(placaLimpa);
        return veiculoRepository.save(veiculo);
    }

    public List<Veiculo> listarVeiculos() {
        return veiculoRepository.findAll();
    }

    public Veiculo buscarPorPlaca(String placa) {
        return veiculoRepository.findById(placa)
                .orElseThrow(() -> new EntityNotFoundException("Veículo não encontrado"));
    }

    public Veiculo atualizarVeiculo(String placa, Veiculo veiculoAtualizado) {
        Veiculo veiculoExistente = veiculoRepository.findById(placa)
                .orElseThrow(() -> new EntityNotFoundException("Veículo com placa " + placa + " não encontrado."));

        veiculoExistente.setModelo(veiculoAtualizado.getModelo());
        veiculoExistente.setAno(veiculoAtualizado.getAno());

        return veiculoRepository.save(veiculoExistente);
    }

    public void deletarVeiculo(String placa) {
        veiculoRepository.deleteById(placa);
    }

}
