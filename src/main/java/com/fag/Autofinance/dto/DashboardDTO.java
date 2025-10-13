package com.fag.Autofinance.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class DashboardDTO {

    private long totalClientes;
    private long totalVeiculos;
    private long ordensEmAndamento;
    private BigDecimal faturamentoMensal;

    private Map<LocalDate, Boolean> calendarioAgendamentos;

    private List<OrcamentoResumoDTO> orcamentosRecentes;
    private List<ServicoResumoDTO> servicosRecentes;

    public long getTotalClientes() {
        return totalClientes;
    }

    public void setTotalClientes(long totalClientes) {
        this.totalClientes = totalClientes;
    }

    public long getTotalVeiculos() {
        return totalVeiculos;
    }

    public void setTotalVeiculos(long totalVeiculos) {
        this.totalVeiculos = totalVeiculos;
    }

    public long getOrdensEmAndamento() {
        return ordensEmAndamento;
    }

    public void setOrdensEmAndamento(long ordensEmAndamento) {
        this.ordensEmAndamento = ordensEmAndamento;
    }

    public BigDecimal getFaturamentoMensal() {
        return faturamentoMensal;
    }

    public void setFaturamentoMensal(BigDecimal faturamentoMensal) {
        this.faturamentoMensal = faturamentoMensal;
    }

    public Map<LocalDate, Boolean> getCalendarioAgendamentos() {
        return calendarioAgendamentos;
    }

    public void setCalendarioAgendamentos(Map<LocalDate, Boolean> calendarioAgendamentos) {
        this.calendarioAgendamentos = calendarioAgendamentos;
    }

    public List<OrcamentoResumoDTO> getOrcamentosRecentes() {
        return orcamentosRecentes;
    }

    public void setOrcamentosRecentes(List<OrcamentoResumoDTO> orcamentosRecentes) {
        this.orcamentosRecentes = orcamentosRecentes;
    }

    public List<ServicoResumoDTO> getServicosRecentes() {
        return servicosRecentes;
    }

    public void setServicosRecentes(List<ServicoResumoDTO> servicosRecentes) {
        this.servicosRecentes = servicosRecentes;
    }

    public static class OrcamentoResumoDTO {
        private String nomeCliente;
        private BigDecimal valor;

        public String getNomeCliente() {
            return nomeCliente;
        }

        public void setNomeCliente(String nomeCliente) {
            this.nomeCliente = nomeCliente;
        }

        public BigDecimal getValor() {
            return valor;
        }

        public void setValor(BigDecimal valor) {
            this.valor = valor;
        }

    }

    public static class ServicoResumoDTO {
        private String nomeCliente;
        private BigDecimal valorTotal;

        public String getNomeCliente() {
            return nomeCliente;
        }

        public void setNomeCliente(String nomeCliente) {
            this.nomeCliente = nomeCliente;
        }

        public BigDecimal getValorTotal() {
            return valorTotal;
        }

        public void setValorTotal(BigDecimal valorTotal) {
            this.valorTotal = valorTotal;
        }

    }
}