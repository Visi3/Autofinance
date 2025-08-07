package com.fag.Autofinance.entities;

import com.fag.Autofinance.enums.StatusCadastros;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.validation.constraints.NotBlank;
import jakarta.persistence.Enumerated;

@Entity
public class Servico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String nome;

    private String descricao;

    private Double preco;

    private String duracao;

    private Boolean possuiRetorno = false;

    private Integer mesesRetornoPadrao;

    private String mensagemRetornoPadrao;

    @Enumerated(EnumType.STRING)
    private StatusCadastros status = StatusCadastros.ATIVO;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Double getPreco() {
        return preco;
    }

    public void setPreco(Double preco) {
        this.preco = preco;
    }

    public String getDuracao() {
        return duracao;
    }

    public void setDuracao(String duracao) {
        this.duracao = duracao;
    }

    public Boolean getPossuiRetorno() {
        return possuiRetorno;
    }

    public void setPossuiRetorno(Boolean possuiRetorno) {
        this.possuiRetorno = possuiRetorno;
    }

    public Integer getMesesRetornoPadrao() {
        return mesesRetornoPadrao;
    }

    public void setMesesRetornoPadrao(Integer mesesRetornoPadrao) {
        this.mesesRetornoPadrao = mesesRetornoPadrao;
    }

    public String getMensagemRetornoPadrao() {
        return mensagemRetornoPadrao;
    }

    public void setMensagemRetornoPadrao(String mensagemRetornoPadrao) {
        this.mensagemRetornoPadrao = mensagemRetornoPadrao;
    }

    public StatusCadastros getStatus() {
        return status;
    }

    public void setStatus(StatusCadastros status) {
        this.status = status;
    }

}