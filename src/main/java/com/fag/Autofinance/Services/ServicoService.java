package com.fag.Autofinance.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.fag.Autofinance.dto.ServicoDTO;
import com.fag.Autofinance.entities.Servico;
import com.fag.Autofinance.enums.StatusCadastros;
import com.fag.Autofinance.exception.JaExisteException;
import com.fag.Autofinance.exception.NaoEncontradoException;
import com.fag.Autofinance.repositories.ServicoRepository;

@Service
public class ServicoService {

    private final ServicoRepository servicoRepository;

    public ServicoService(ServicoRepository servicoRepository) {
        this.servicoRepository = servicoRepository;
    }

    public Page<ServicoDTO> listarTodos(Pageable pageable) {
        return servicoRepository.findAll(pageable)
                .map(ServicoDTO::new);
    }

    public Page<ServicoDTO> listarPorStatus(StatusCadastros status, Pageable pageable) {
        return servicoRepository.findByStatus(status, pageable)
                .map(ServicoDTO::new);
    }

    public Servico criar(Servico servico) {
        boolean existe = servicoRepository.findByNomeContainingIgnoreCase(servico.getNome())
                .stream().anyMatch(s -> s.getNome().equalsIgnoreCase(servico.getNome()));

        if (existe) {
            throw new JaExisteException("Já existe um serviço com este nome");
        }

        return servicoRepository.save(servico);

    }

    public Servico atualizar(Long id, Servico servicoAtualizado) {
        Servico servicoExistente = servicoRepository.findById(id)
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