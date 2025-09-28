package com.fag.Autofinance.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fag.Autofinance.entities.Empresa;

public class EmpresaDTO {
    private UUID id;
    private String nome;
    private String cnpj;
    private LocalDateTime dataCadastro;
    private String endereco;
    private String cidade;
    private String telefone;
    private String cep;

    public EmpresaDTO(Empresa empresa) {
        this.id = empresa.getId();
        this.nome = empresa.getNome();
        this.cnpj = empresa.getCnpj();
        this.dataCadastro = empresa.getDataCadastro();
        this.endereco = empresa.getEndereco();
        this.cidade = empresa.getCidade();
        this.telefone = empresa.getTelefone();
        this.cep = empresa.getCep();
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getCep() {
        return cep;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public LocalDateTime getDataCadastro() {
        return dataCadastro;
    }

    public void setDataCadastro(LocalDateTime dataCadastro) {
        this.dataCadastro = dataCadastro;
    }

}
