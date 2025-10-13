package com.fag.Autofinance.controllers;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fag.Autofinance.dto.DashboardDTO;
import com.fag.Autofinance.entities.Usuarios;
import com.fag.Autofinance.services.DashboardService;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public DashboardDTO getDashboard() {
        // Aqui você precisa passar o ID da empresa do usuário logado
        // Pode ser obtido via SecurityContext ou diretamente do usuário
        Usuarios usuario = dashboardService.getUsuarioLogado();
        UUID empresaId = usuario.getEmpresa().getId();

        return dashboardService.getDashboard(empresaId, usuario);
    }
}