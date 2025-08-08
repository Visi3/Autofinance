package com.fag.Autofinance.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fag.Autofinance.entities.Orcamento;
import com.fag.Autofinance.enums.StatusCadastros;

public class OrcamentoDTO {

    private Long id;
    private String cpfCnpj;
    private String veiculoPlaca;
    private String servicoNome;
    private String mecanicoUsername;
    private Double valorAjustado;
    private StatusCadastros status;
    private LocalDateTime dataCriacao;

    public OrcamentoDTO(Orcamento orcamento) {
        this.id = orcamento.getId();
        this.cpfCnpj = orcamento.getCliente().getCpfCnpj();
        this.veiculoPlaca = orcamento.getVeiculo().getPlaca();
        this.servicoNome = orcamento.getServico().getNome();
        this.mecanicoUsername = orcamento.getMecanico().getUsername();
        this.valorAjustado = orcamento.getValorAjustado();
        this.status = orcamento.getStatus();
        this.dataCriacao = orcamento.getDataCadastro();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public StatusCadastros getStatus() {
        return status;
    }

    public void setStatus(StatusCadastros status) {
        this.status = status;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

}
