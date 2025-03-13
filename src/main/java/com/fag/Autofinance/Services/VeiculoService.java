package com.fag.Autofinance.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fag.Autofinance.Entities.Veiculo;
import com.fag.Autofinance.Repositories.VeiculoRepository;

@Service
public class VeiculoService {

    @Autowired
    private VeiculoRepository veiculoRepository;

    public Veiculo salvarVeiculo(Veiculo veiculo) {
        String placaLimpa = veiculo.getPlaca().replaceAll("[^0-9]", "");
        veiculo.setPlaca(placaLimpa);
        return veiculoRepository.save(veiculo);
    }

}
