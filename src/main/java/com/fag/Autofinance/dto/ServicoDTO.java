package com.fag.Autofinance.dto;

import com.fag.Autofinance.entities.Servico;
import com.fag.Autofinance.enums.StatusCadastros;

public class ServicoDTO {
    private Long id;
    private String nome;
    private String descricao;
    private Double preco;
    private String duracao;
    private Boolean possuiRetorno;
    private Integer mesesRetornoPadrao;
    private String mensagemRetornoPadrao;
    private StatusCadastros status;

    public ServicoDTO(Servico servico) {
        this.id = servico.getId();
        this.nome = servico.getNome();
        this.descricao = servico.getDescricao();
        this.preco = servico.getPreco();
        this.duracao = servico.getDuracao();
        this.possuiRetorno = servico.getPossuiRetorno();
        this.mesesRetornoPadrao = servico.getMesesRetornoPadrao();
        this.mensagemRetornoPadrao = servico.getMensagemRetornoPadrao();
        this.status = servico.getStatus();
    }

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
