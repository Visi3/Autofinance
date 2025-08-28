package com.fag.Autofinance.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fag.Autofinance.dto.CriarEmpresaDTO;
import com.fag.Autofinance.dto.EmpresaDTO;
import com.fag.Autofinance.services.EmpresaService;

@RestController
@RequestMapping("/empresas")
public class EmpresaController {

    private final EmpresaService empresaService;

    public EmpresaController(EmpresaService empresaService) {
        this.empresaService = empresaService;
    }

    @PostMapping("/criar")
    public ResponseEntity<EmpresaDTO> criar(@RequestBody CriarEmpresaDTO dto) {
        EmpresaDTO empresa = empresaService.criarEmpresaComAdmin(dto);
        return ResponseEntity.ok(empresa);
    }
}
