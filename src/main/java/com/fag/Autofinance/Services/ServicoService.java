package com.fag.Autofinance.services;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    public Page<ServicoDTO> listarTodos(Pageable pageable) {
        UUID empresaId = getUsuarioLogado().getEmpresa().getId();
        return servicoRepository.findAllByEmpresaId(empresaId, pageable)
                .map(ServicoDTO::new);
    }

    public Page<ServicoDTO> listarPorStatus(StatusCadastros status, Pageable pageable) {
        UUID empresaId = getUsuarioLogado().getEmpresa().getId();
        return servicoRepository.findByStatusAndEmpresaId(status, empresaId, pageable)
                .map(ServicoDTO::new);
    }

    public Servico criar(Servico servico) {
        Usuarios usuarioLogado = getUsuarioLogado();

        boolean existe = servicoRepository
                .findByNomeContainingIgnoreCaseAndEmpresaId(servico.getNome(), usuarioLogado.getEmpresa().getId())
                .stream().anyMatch(s -> s.getNome().equalsIgnoreCase(servico.getNome()));

        if (existe) {
            throw new JaExisteException("Já existe um serviço com este nome nesta empresa");
        }

        servico.setStatus(StatusCadastros.ATIVO);
        servico.setEmpresa(usuarioLogado.getEmpresa());

        return servicoRepository.save(servico);
    }

    public Servico atualizar(Long id, Servico servicoAtualizado) {
        UUID empresaId = getUsuarioLogado().getEmpresa().getId();

        Servico servicoExistente = servicoRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new NaoEncontradoException("Serviço não encontrado"));

        servicoExistente.setNome(servicoAtualizado.getNome());
        servicoExistente.setDescricao(servicoAtualizado.getDescricao());
        servicoExistente.setPreco(servicoAtualizado.getPreco());
        servicoExistente.setDuracao(servicoAtualizado.getDuracao());
        servicoExistente.setPossuiRetorno(servicoAtualizado.getPossuiRetorno());
        servicoExistente.setMesesRetornoPadrao(servicoAtualizado.getMesesRetornoPadrao());
        servicoExistente.setMensagemRetornoPadrao(servicoAtualizado.getMensagemRetornoPadrao());
        servicoExistente.setStatus(servicoAtualizado.getStatus());

        return servicoRepository.save(servicoExistente);
    }

}