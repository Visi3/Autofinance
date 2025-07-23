package com.fag.Autofinance.entities;

import java.time.LocalDate;

import com.fag.Autofinance.enums.StatusCadastros;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;

@Entity
public class ServicoRealizado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Servico servico;

    @ManyToOne(optional = false)
    private Veiculo veiculo;

    /*@ManyToOne(optional = false)
    private OrdemServico ordemServico;*/

    private LocalDate dataExecucao;

    private LocalDate dataPrevistaRetorno;

    @Enumerated(EnumType.STRING)
    private StatusCadastros status = StatusCadastros.ATIVO;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Servico getServico() {
        return servico;
    }

    public void setServico(Servico servico) {
        this.servico = servico;
    }

    public Veiculo getVeiculo() {
        return veiculo;
    }

    public void setVeiculo(Veiculo veiculo) {
        this.veiculo = veiculo;
    }

    public LocalDate getDataExecucao() {
        return dataExecucao;
    }

    public void setDataExecucao(LocalDate dataExecucao) {
        this.dataExecucao = dataExecucao;
    }

    public LocalDate getDataPrevistaRetorno() {
        return dataPrevistaRetorno;
    }

    public void setDataPrevistaRetorno(LocalDate dataPrevistaRetorno) {
        this.dataPrevistaRetorno = dataPrevistaRetorno;
    }

    public StatusCadastros getStatus() {
        return status;
    }

    public void setStatus(StatusCadastros status) {
        this.status = status;
    }

    
}
