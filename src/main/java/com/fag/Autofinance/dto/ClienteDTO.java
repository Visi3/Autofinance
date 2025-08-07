package com.fag.Autofinance.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fag.Autofinance.entities.Cliente;
import com.fag.Autofinance.enums.StatusCadastros;

public class ClienteDTO {

    private String cpfCnpj;
    private String nome;
    private String email;
    private String celular;
    private String endereco;
    private String cep;
    private String observacoes;
    private LocalDate dataNascimento;
    private LocalDateTime dataCadastro;
    private StatusCadastros status;

    public ClienteDTO(Cliente cliente) {
        this.cpfCnpj = cliente.getCpfCnpj();
        this.nome = cliente.getNome();
        this.email = cliente.getEmail();
        this.celular = cliente.getCelular();
        this.endereco = cliente.getEndereco();
        this.cep = cliente.getCep();
        this.observacoes = cliente.getObservacoes();
        this.dataNascimento = cliente.getDataNascimento();
        this.dataCadastro = cliente.getDataCadastro();
        this.status = cliente.getStatus();
    }

    public String getCpfCnpj() {
        return cpfCnpj;
    }

    public void setCpfCnpj(String cpfCnpj) {
        this.cpfCnpj = cpfCnpj;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCelular() {
        return celular;
    }

    public void setCelular(String celular) {
        this.celular = celular;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public String getCep() {
        return cep;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public LocalDateTime getDataCadastro() {
        return dataCadastro;
    }

    public void setDataCadastro(LocalDateTime dataCadastro) {
        this.dataCadastro = dataCadastro;
    }

    public StatusCadastros getStatus() {
        return status;
    }

    public void setStatus(StatusCadastros status) {
        this.status = status;
    }

}