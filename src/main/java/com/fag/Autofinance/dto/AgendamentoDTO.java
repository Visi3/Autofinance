package com.fag.Autofinance.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fag.Autofinance.entities.Agendamento;
import com.fag.Autofinance.enums.StatusAgendamento;

public class AgendamentoDTO {

    private UUID id;
    private Integer numero;
    private Integer numeroOrdem;
    private String mecanicoUsername;
    private LocalDateTime dataAgendada;
    private String observacoes;
    private StatusAgendamento status;

    public AgendamentoDTO(Agendamento agendamento) {
        this.id = agendamento.getId();
        this.numero = agendamento.getNumero();

        if (agendamento.getOrdemServico() != null) {
            this.numeroOrdem = agendamento.getOrdemServico().getNumero();
        }

        if (agendamento.getMecanico() != null) {
            this.mecanicoUsername = agendamento.getMecanico().getUsername();
        }
        this.dataAgendada = agendamento.getDataAgendada();
        this.observacoes = agendamento.getObservacoes();
        this.status = agendamento.getStatus();
    }

    public AgendamentoDTO() {
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

    public Integer getNumeroOrdem() {
        return numeroOrdem;
    }

    public void setNumeroOrdem(Integer numeroOrdem) {
        this.numeroOrdem = numeroOrdem;
    }

    public String getMecanicoUsername() {
        return mecanicoUsername;
    }

    public void setMecanicoUsername(String mecanicoUsername) {
        this.mecanicoUsername = mecanicoUsername;
    }

    public LocalDateTime getDataAgendada() {
        return dataAgendada;
    }

    public void setDataAgendada(LocalDateTime dataAgendada) {
        this.dataAgendada = dataAgendada;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public StatusAgendamento getStatus() {
        return status;
    }

    public void setStatus(StatusAgendamento status) {
        this.status = status;
    }

}