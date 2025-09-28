package com.fag.Autofinance.dto;

import java.time.LocalDateTime;

import com.fag.Autofinance.entities.Orcamento;

import com.fag.Autofinance.enums.StatusOrcamento;

public class OrcamentoDTO {

    private Long numeroOrcamento;
    private String cpfCnpj;
    private String veiculoPlaca;
    private String servicoNome;
    private String mecanicoUsername;
    private Double valorAjustado;
    private StatusOrcamento status;
    private LocalDateTime dataCriacao;

    public OrcamentoDTO(Orcamento salvo) {
        this.numeroOrcamento = salvo.getNumero();
        this.cpfCnpj = salvo.getCliente().getCpfCnpj();
        this.veiculoPlaca = salvo.getVeiculo().getPlaca();
        this.servicoNome = salvo.getServico().getNome();
        this.mecanicoUsername = salvo.getMecanico().getUsername();
        this.valorAjustado = salvo.getValorAjustado();
        this.status = salvo.getStatus();
        this.dataCriacao = salvo.getDataCadastro();
    }

    public OrcamentoDTO() {
    }

    public Long getNumeroOrcamento() {
        return numeroOrcamento;
    }

    public void setNumeroOrcamento(Long numeroOrcamento) {
        this.numeroOrcamento = numeroOrcamento;
    }

    public String getCpfCnpj() {
        return cpfCnpj;
    }

    public void setCpfCnpj(String cpfCnpj) {
        this.cpfCnpj = cpfCnpj;
    }

    public String getVeiculoPlaca() {
        return veiculoPlaca;
    }

    public void setVeiculoPlaca(String veiculoPlaca) {
        this.veiculoPlaca = veiculoPlaca;
    }

    public String getServicoNome() {
        return servicoNome;
    }

    public void setServicoNome(String servicoNome) {
        this.servicoNome = servicoNome;
    }

    public String getMecanicoUsername() {
        return mecanicoUsername;
    }

    public void setMecanicoUsername(String mecanicoUsername) {
        this.mecanicoUsername = mecanicoUsername;
    }

    public Double getValorAjustado() {
        return valorAjustado;
    }

    public void setValorAjustado(Double valorAjustado) {
        this.valorAjustado = valorAjustado;
    }

    public StatusOrcamento getStatus() {
        return status;
    }

    public void setStatus(StatusOrcamento status) {
        this.status = status;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

}
