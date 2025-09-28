package com.fag.Autofinance.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fag.Autofinance.entities.OrdemServico;
import com.fag.Autofinance.enums.StatusOrdemServico;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class OrdemServicoDTO {
    private UUID id;
    private Integer numero;
    private String clienteCpfCnpj;
    private String veiculoPlaca;
    private String servicoNome;
    private String mecanicoUsername;
    private Double valor;
    private StatusOrdemServico status;
    private String observacoes;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataFinalizacao;
    private LocalDateTime dataRetorno;

    public OrdemServicoDTO(OrdemServico ordem) {
        this.id = ordem.getId();
        this.numero = ordem.getNumero();
        this.clienteCpfCnpj = ordem.getCliente().getCpfCnpj();
        this.veiculoPlaca = ordem.getVeiculo().getPlaca();
        this.servicoNome = ordem.getServico().getNome();
        this.mecanicoUsername = ordem.getMecanico().getUsername();
        this.valor = ordem.getValor();
        this.status = ordem.getStatus();
        this.observacoes = ordem.getObservacoes();
        this.dataCriacao = ordem.getDataCriacao();
        this.dataFinalizacao = ordem.getDataFinalizacao();
        this.dataRetorno = ordem.getDataRetorno();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Integer getNumero() {
        return numero;
    }

    public void setNumero(Integer numero) {
        this.numero = numero;
    }

    public String getClienteCpfCnpj() {
        return clienteCpfCnpj;
    }

    public void setClienteCpfCnpj(String clienteCpfCnpj) {
        this.clienteCpfCnpj = clienteCpfCnpj;
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

    public Double getValor() {
        return valor;
    }

    public void setValor(Double valor) {
        this.valor = valor;
    }

    public StatusOrdemServico getStatus() {
        return status;
    }

    public void setStatus(StatusOrdemServico status) {
        this.status = status;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public LocalDateTime getDataFinalizacao() {
        return dataFinalizacao;
    }

    public void setDataFinalizacao(LocalDateTime dataFinalizacao) {
        this.dataFinalizacao = dataFinalizacao;
    }

    public LocalDateTime getDataRetorno() {
        return dataRetorno;
    }

    public void setDataRetorno(LocalDateTime dataRetorno) {
        this.dataRetorno = dataRetorno;
    }

}