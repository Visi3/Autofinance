package com.fag.Autofinance.services;

import java.util.UUID;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.fag.Autofinance.dto.ServicoDTO;
import com.fag.Autofinance.entities.Servico;
import com.fag.Autofinance.entities.Usuarios;
import com.fag.Autofinance.enums.StatusCadastros;
import com.fag.Autofinance.exception.JaExisteException;
import com.fag.Autofinance.exception.NaoEncontradoException;
import com.fag.Autofinance.repositories.ServicoRepository;
import com.fag.Autofinance.repositories.UsuarioRepository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class ServicoService {

    private final ServicoRepository servicoRepository;
    private final UsuarioRepository usuarioRepository;

    public ServicoService(ServicoRepository servicoRepository, UsuarioRepository usuarioRepository) {
        this.servicoRepository = servicoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    private Usuarios getUsuarioLogado() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new NaoEncontradoException("Usuário autenticado não encontrado"));
    }

    public List<ServicoDTO> listarTodos() {
        UUID empresaId = getUsuarioLogado().getEmpresa().getId();
        return servicoRepository.findAllByEmpresaId(empresaId)
                .stream()
                .map(ServicoDTO::new)
                .toList();
    }

    public List<ServicoDTO> listarPorStatus(StatusCadastros status) {
        UUID empresaId = getUsuarioLogado().getEmpresa().getId();
        return servicoRepository.findByStatusAndEmpresaId(status, empresaId)
                .stream()
                .map(ServicoDTO::new)
                .toList();
    }

    @Transactional
    public Servico criar(Servico servico) {
        Usuarios usuarioLogado = getUsuarioLogado();
        UUID empresaId = usuarioLogado.getEmpresa().getId();
        servicoRepository.findByNomeIgnoreCaseAndEmpresaId(servico.getNome(), empresaId)
                .ifPresent(s -> {
                    throw new JaExisteException("Já existe um serviço com este nome nesta empresa");
                });

        servico.setStatus(StatusCadastros.ATIVO);
        servico.setEmpresa(usuarioLogado.getEmpresa());

        return servicoRepository.save(servico);
    }

    @Transactional
    public Servico atualizar(Long id, Servico servicoAtualizado) {
        UUID empresaId = getUsuarioLogado().getEmpresa().getId();

        Servico servicoExistente = servicoRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new NaoEncontradoException("Serviço não encontrado"));

        if (servicoAtualizado.getNome() != null
                && !servicoAtualizado.getNome().equalsIgnoreCase(servicoExistente.getNome())) {

            servicoRepository.findByNomeIgnoreCaseAndEmpresaId(servicoAtualizado.getNome(), empresaId)
                    .ifPresent(s -> {
                        throw new JaExisteException("Já existe outro serviço com este nome nesta empresa");
                    });
            servicoExistente.setNome(servicoAtualizado.getNome());
        }

        if (servicoAtualizado.getDescricao() != null) {
            servicoExistente.setDescricao(servicoAtualizado.getDescricao());
        }
        if (servicoAtualizado.getPreco() != null) {
            servicoExistente.setPreco(servicoAtualizado.getPreco());
        }
        if (servicoAtualizado.getDuracao() != null) {
            servicoExistente.setDuracao(servicoAtualizado.getDuracao());
        }
        if (servicoAtualizado.getPossuiRetorno() != null) {
            servicoExistente.setPossuiRetorno(servicoAtualizado.getPossuiRetorno());
        }
        if (servicoAtualizado.getMesesRetornoPadrao() != null) {
            servicoExistente.setMesesRetornoPadrao(servicoAtualizado.getMesesRetornoPadrao());
        }
        if (servicoAtualizado.getMensagemRetornoPadrao() != null) {
            servicoExistente.setMensagemRetornoPadrao(servicoAtualizado.getMensagemRetornoPadrao());
        }
        if (servicoAtualizado.getStatus() != null) {
            servicoExistente.setStatus(servicoAtualizado.getStatus());
        }

        return servicoRepository.save(servicoExistente);
    }

}