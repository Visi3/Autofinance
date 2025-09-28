package com.fag.Autofinance.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fag.Autofinance.entities.Usuarios;
import com.fag.Autofinance.enums.StatusCadastros;

public class UsuariosDTO {

    private UUID id;
    private String username;
    private String email;
    private String telefone;
    private String role;
    private StatusCadastros status;
    private LocalDateTime dataCadastro;
    private String empresaNome;

    public UsuariosDTO(Usuarios usuario) {
        this.id = usuario.getId();
        this.username = usuario.getUsername();
        this.email = usuario.getEmail();
        this.telefone = usuario.getTelefone();
        this.role = usuario.getRole().name();
        this.status = usuario.getStatus();
        this.dataCadastro = usuario.getDataCadastro();
        this.empresaNome = usuario.getEmpresa().getNome();
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

    public String getEmpresaNome() {
        return empresaNome;
    }

    public void setEmpresaNome(String empresaNome) {
        this.empresaNome = empresaNome;
    }

}
