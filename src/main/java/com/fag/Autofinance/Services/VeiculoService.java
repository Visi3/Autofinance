package com.fag.Autofinance.Services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fag.Autofinance.Entities.Veiculo;
import com.fag.Autofinance.Repositories.VeiculoRepository;

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

    public void deletarVeiculo(String placa) {
        veiculoRepository.deleteById(placa);
    }

}
