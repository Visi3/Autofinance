package com.fag.Autofinance.services;

import org.springframework.stereotype.Service;

import com.fag.Autofinance.dto.DashboardDTO;
import com.fag.Autofinance.entities.Agendamento;
import com.fag.Autofinance.entities.Orcamento;
import com.fag.Autofinance.entities.OrdemServico;
import com.fag.Autofinance.entities.Usuarios;
import com.fag.Autofinance.enums.RoleUsuario;
import com.fag.Autofinance.enums.StatusAgendamento;
import com.fag.Autofinance.enums.StatusOrdemServico;
import com.fag.Autofinance.repositories.AgendamentoRepository;
import com.fag.Autofinance.repositories.ClienteRepository;
import com.fag.Autofinance.repositories.OrcamentoRepository;
import com.fag.Autofinance.repositories.OrdemServicoRepository;
import com.fag.Autofinance.repositories.UsuarioRepository;
import com.fag.Autofinance.repositories.VeiculoRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final ClienteRepository clienteRepository;
    private final VeiculoRepository veiculoRepository;
    private final OrdemServicoRepository osRepository;
    private final OrcamentoRepository orcamentoRepository;
    private final AgendamentoRepository agendamentoRepository;
    private final UsuarioRepository usuarioRepository;

    public DashboardService(
            ClienteRepository clienteRepository,
            VeiculoRepository veiculoRepository,
            OrdemServicoRepository osRepository,
            OrcamentoRepository orcamentoRepository,
            AgendamentoRepository agendamentoRepository,
            UsuarioRepository usuarioRepository) {
        this.clienteRepository = clienteRepository;
        this.veiculoRepository = veiculoRepository;
        this.osRepository = osRepository;
        this.orcamentoRepository = orcamentoRepository;
        this.agendamentoRepository = agendamentoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public DashboardDTO getDashboard(UUID empresaId, Usuarios username) {
        Usuarios usuario = username;

        boolean isAdmin = usuario.getRole() == RoleUsuario.ADMIN;

        DashboardDTO dto = new DashboardDTO();

        // Total de clientes
        dto.setTotalClientes(clienteRepository.findAllByEmpresaId(empresaId).size());

        // Total de veículos
        dto.setTotalVeiculos(veiculoRepository.findAllByEmpresaId(empresaId, Pageable.unpaged()).getTotalElements());

        // Ordens em andamento
        List<OrdemServico> ordensAndamento = isAdmin
                ? osRepository.findByEmpresaId(empresaId, Pageable.unpaged()).getContent()
                : osRepository.findByMecanicoUsernameAndEmpresaId(usuario.getUsername(), empresaId);

        long ordensEmAndamento = ordensAndamento.stream()
                .filter(os -> os.getStatus() == StatusOrdemServico.EM_ANDAMENTO
                        || os.getStatus() == StatusOrdemServico.ATIVA)
                .count();
        dto.setOrdensEmAndamento((int) ordensEmAndamento);

        // Todas as OS relevantes (para faturamento e últimos serviços)
        List<OrdemServico> todasOs = isAdmin
                ? osRepository.findByEmpresaId(empresaId, Pageable.unpaged()).getContent()
                : osRepository.findByMecanicoUsernameAndEmpresaId(usuario.getUsername(), empresaId);

        YearMonth mesAtual = YearMonth.now();
        LocalDate inicioMes = mesAtual.atDay(1);
        LocalDate fimMes = mesAtual.atEndOfMonth();

        // Faturamento mensal
        BigDecimal faturamento = todasOs.stream()
                .filter(os -> os.getStatus() == StatusOrdemServico.FINALIZADA)
                .filter(os -> os.getDataFinalizacao() != null)
                .filter(os -> {
                    LocalDate dataFinal = os.getDataFinalizacao().toLocalDate();
                    return !dataFinal.isBefore(inicioMes) && !dataFinal.isAfter(fimMes);
                })
                .map(os -> BigDecimal.valueOf(os.getValor() != null ? os.getValor() : 0))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        dto.setFaturamentoMensal(faturamento);

        // Últimos serviços (ordenados por dataFinalizacao DESC)
        List<DashboardDTO.ServicoResumoDTO> servicosRecentes = todasOs.stream()
                .filter(os -> os.getStatus() == StatusOrdemServico.FINALIZADA)
                .filter(os -> os.getDataFinalizacao() != null)
                .sorted(Comparator.comparing(OrdemServico::getDataFinalizacao).reversed())
                .limit(10)
                .map(os -> {
                    DashboardDTO.ServicoResumoDTO s = new DashboardDTO.ServicoResumoDTO();
                    s.setNomeCliente(os.getCliente().getNome());
                    s.setValorTotal(BigDecimal.valueOf(os.getValor() != null ? os.getValor() : 0));
                    return s;
                })
                .collect(Collectors.toList());
        dto.setServicosRecentes(servicosRecentes);

        // Orçamentos recentes (ordenados por dataCadastro DESC)
        List<Orcamento> orcamentos = isAdmin
                ? orcamentoRepository.findByEmpresaId(empresaId, Pageable.unpaged()).getContent()
                : orcamentoRepository
                        .findByMecanicoUsernameAndEmpresaId(usuario.getUsername(), empresaId, Pageable.unpaged())
                        .getContent();

        List<DashboardDTO.OrcamentoResumoDTO> orcamentosRecentes = orcamentos.stream()
                .filter(o -> o.getDataCadastro() != null)
                .sorted(Comparator.comparing(Orcamento::getDataCadastro).reversed())
                .limit(10)
                .map(o -> {
                    DashboardDTO.OrcamentoResumoDTO resumo = new DashboardDTO.OrcamentoResumoDTO();
                    resumo.setNomeCliente(o.getCliente().getNome());
                    resumo.setValor(BigDecimal.valueOf(
                            o.getValorAjustado() != null ? o.getValorAjustado() : o.getServico().getPreco()));
                    return resumo;
                })
                .collect(Collectors.toList());
        dto.setOrcamentosRecentes(orcamentosRecentes);

        // Calendário de agendamentos
        List<Agendamento> agendamentos = isAdmin
                ? agendamentoRepository.findByOrdemServicoEmpresaId(empresaId, Pageable.unpaged()).getContent()
                : agendamentoRepository.findByMecanicoUsernameAndOrdemServicoEmpresaId(usuario.getUsername(), empresaId,
                        Pageable.unpaged()).getContent();

        Map<LocalDate, Boolean> calendario = agendamentos.stream()
                .filter(a -> a.getStatus() != StatusAgendamento.CONCLUIDO)
                .collect(Collectors.groupingBy(
                        a -> a.getDataAgendada().toLocalDate(),
                        Collectors.collectingAndThen(Collectors.counting(), count -> count > 0)));

        dto.setCalendarioAgendamentos(calendario);

        return dto;
    }

    public Usuarios getUsuarioLogado() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }
}