package com.fag.Autofinance.services;

import org.springframework.stereotype.Service;
import com.fag.Autofinance.dto.DashboardDTO;
import com.fag.Autofinance.entities.Agendamento;
import com.fag.Autofinance.entities.Orcamento;
import com.fag.Autofinance.entities.OrdemServico;
import com.fag.Autofinance.entities.Usuarios;
import com.fag.Autofinance.enums.RoleUsuario;
import com.fag.Autofinance.enums.StatusAgendamento;
import com.fag.Autofinance.enums.StatusCadastros;
import com.fag.Autofinance.enums.StatusOrdemServico;
import com.fag.Autofinance.exception.NaoEncontradoException;
import com.fag.Autofinance.repositories.AgendamentoRepository;
import com.fag.Autofinance.repositories.ClienteRepository;
import com.fag.Autofinance.repositories.OrcamentoRepository;
import com.fag.Autofinance.repositories.OrdemServicoRepository;
import com.fag.Autofinance.repositories.UsuarioRepository;
import com.fag.Autofinance.repositories.VeiculoRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.stream.Collectors;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

        private Usuarios getUsuarioLogado() {
                String username = SecurityContextHolder.getContext().getAuthentication().getName();
                return usuarioRepository.findByUsername(username)
                                .orElseThrow(() -> new NaoEncontradoException("Usuário autenticado não encontrado"));
        }

        public DashboardDTO getDashboard() {
                Usuarios usuario = getUsuarioLogado();
                UUID empresaId = usuario.getEmpresa().getId();
                boolean isAdmin = usuario.getRole() == RoleUsuario.ADMIN;

                DashboardDTO dto = new DashboardDTO();

                dto.setTotalClientes(clienteRepository.countByEmpresaId(empresaId));

                long totalVeiculosAtivos = veiculoRepository.countByStatusAndEmpresaId(StatusCadastros.ATIVO,
                                empresaId);
                dto.setTotalVeiculos((int) totalVeiculosAtivos);

                List<StatusOrdemServico> statusAndamento = List.of(StatusOrdemServico.ATIVA,
                                StatusOrdemServico.EM_ANDAMENTO);
                long ordensEmAndamento = isAdmin
                                ? osRepository.countByStatusInAndEmpresaId(statusAndamento, empresaId)
                                : osRepository.countByStatusInAndMecanicoUsernameAndEmpresaId(statusAndamento,
                                                usuario.getUsername(), empresaId);
                dto.setOrdensEmAndamento((int) ordensEmAndamento);

                YearMonth mesAtual = YearMonth.now();
                LocalDateTime inicioMes = mesAtual.atDay(1).atStartOfDay();
                LocalDateTime fimMes = mesAtual.atEndOfMonth().atTime(LocalTime.MAX);

                BigDecimal faturamento = isAdmin
                                ? osRepository.sumValorByStatusAndDataFinalizacaoBetweenAndEmpresaId(
                                                StatusOrdemServico.FINALIZADA, inicioMes, fimMes, empresaId)
                                : osRepository.sumValorByStatusAndDataFinalizacaoBetweenAndMecanicoUsernameAndEmpresaId(
                                                StatusOrdemServico.FINALIZADA, inicioMes, fimMes, usuario.getUsername(),
                                                empresaId);
                dto.setFaturamentoMensal(faturamento);

                List<OrdemServico> osRecentes = isAdmin
                                ? osRepository.findTop5ByStatusAndEmpresaIdOrderByDataFinalizacaoDesc(
                                                StatusOrdemServico.FINALIZADA, empresaId)
                                : osRepository.findTop5ByStatusAndMecanicoUsernameAndEmpresaIdOrderByDataFinalizacaoDesc(
                                                StatusOrdemServico.FINALIZADA, usuario.getUsername(), empresaId);

                List<DashboardDTO.ServicoResumoDTO> servicosRecentes = osRecentes.stream()
                                .map(os -> {
                                        DashboardDTO.ServicoResumoDTO s = new DashboardDTO.ServicoResumoDTO();
                                        s.setNomeCliente(os.getCliente().getNome());
                                        s.setValorTotal(BigDecimal.valueOf(os.getValor() != null ? os.getValor() : 0));
                                        return s;
                                })
                                .collect(Collectors.toList());
                dto.setServicosRecentes(servicosRecentes);

                List<Orcamento> orcamentosRecentes = isAdmin
                                ? orcamentoRepository.findTop5ByEmpresaIdOrderByDataCadastroDesc(empresaId)
                                : orcamentoRepository.findTop5ByMecanicoUsernameAndEmpresaIdOrderByDataCadastroDesc(
                                                usuario.getUsername(), empresaId);

                List<DashboardDTO.OrcamentoResumoDTO> orcamentosResumo = orcamentosRecentes.stream()
                                .map(o -> {
                                        DashboardDTO.OrcamentoResumoDTO resumo = new DashboardDTO.OrcamentoResumoDTO();
                                        resumo.setNomeCliente(o.getCliente().getNome());

                                        double valor = (o.getValorAjustado() != null && o.getValorAjustado() > 0)
                                                        ? o.getValorAjustado()
                                                        : (o.getServico() != null ? o.getServico().getPreco() : 0.0);
                                        resumo.setValor(BigDecimal.valueOf(valor));
                                        return resumo;
                                })
                                .collect(Collectors.toList());
                dto.setOrcamentosRecentes(orcamentosResumo);

                List<Agendamento> agendamentos = isAdmin
                                ? agendamentoRepository.findByOrdemServicoEmpresaId(empresaId)
                                : agendamentoRepository.findByMecanicoUsernameAndOrdemServicoEmpresaId(
                                                usuario.getUsername(), empresaId);

                Map<LocalDate, Boolean> calendario = agendamentos.stream()
                                .filter(a -> a.getStatus() != StatusAgendamento.CONCLUIDO
                                                && a.getStatus() != StatusAgendamento.INATIVO)

                                .collect(Collectors.groupingBy(
                                                a -> a.getDataAgendada().toLocalDate(),
                                                Collectors.collectingAndThen(Collectors.counting(),
                                                                count -> count > 0)));
                dto.setCalendarioAgendamentos(calendario);

                return dto;
        }

}