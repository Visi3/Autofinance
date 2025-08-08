package com.fag.Autofinance.dto;

import java.time.LocalDateTime;

import com.fag.Autofinance.entities.Usuarios;
import com.fag.Autofinance.enums.StatusCadastros;

public class UsuariosDTO {
    private String username;
    private String email;
    private String telefone;
    private String role;
    private StatusCadastros status;
    private LocalDateTime dataCadastro;

    public UsuariosDTO(Usuarios usuario) {
        this.username = usuario.getUsername();
        this.email = usuario.getEmail();
        this.telefone = usuario.getTelefone();
        this.role = usuario.getRole().name();
        this.status = usuario.getStatus();
        this.dataCadastro = usuario.getDataCadastro();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public StatusCadastros getStatus() {
        return status;
    }

    public void setStatus(StatusCadastros status) {
        this.status = status;
    }

    public LocalDateTime getDataCadastro() {
        return dataCadastro;
    }

    public void setDataCadastro(LocalDateTime dataCadastro) {
        this.dataCadastro = dataCadastro;
    }

}
