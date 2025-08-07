package com.fag.Autofinance.dto;

import com.fag.Autofinance.entities.Veiculo;
import com.fag.Autofinance.enums.StatusCadastros;

public class VeiculoDTO {
    private String placa;
    private String modelo;
    private String marca;
    private Integer ano;
    private String cor;
    private String renavam;
    private String chassi;
    private String combustivel;
    private String quilometragem;
    private String observacoes;
    private StatusCadastros status;
    private ClienteDTO cliente;

    public VeiculoDTO(Veiculo veiculo) {
        this.placa = veiculo.getPlaca();
        this.modelo = veiculo.getModelo();
        this.marca = veiculo.getMarca();
        this.ano = veiculo.getAno();
        this.cor = veiculo.getCor();
        this.renavam = veiculo.getRenavam();
        this.chassi = veiculo.getChassi();
        this.combustivel = veiculo.getCombustivel();
        this.quilometragem = veiculo.getQuilometragem();
        this.observacoes = veiculo.getObservacoes();
        this.status = veiculo.getStatus();
        this.cliente = new ClienteDTO(veiculo.getCliente());
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public Integer getAno() {
        return ano;
    }

    public void setAno(Integer ano) {
        this.ano = ano;
    }

    public String getCor() {
        return cor;
    }

    public void setCor(String cor) {
        this.cor = cor;
    }

    public String getRenavam() {
        return renavam;
    }

    public void setRenavam(String renavam) {
        this.renavam = renavam;
    }

    public String getChassi() {
        return chassi;
    }

    public void setChassi(String chassi) {
        this.chassi = chassi;
    }

    public String getCombustivel() {
        return combustivel;
    }

    public void setCombustivel(String combustivel) {
        this.combustivel = combustivel;
    }

    public String getQuilometragem() {
        return quilometragem;
    }

    public void setQuilometragem(String quilometragem) {
        this.quilometragem = quilometragem;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public StatusCadastros getStatus() {
        return status;
    }

    public void setStatus(StatusCadastros status) {
        this.status = status;
    }

    public ClienteDTO getCliente() {
        return cliente;
    }

    public void setCliente(ClienteDTO cliente) {
        this.cliente = cliente;
    }

}