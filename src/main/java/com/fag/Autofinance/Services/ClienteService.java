package com.fag.Autofinance.services;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.fag.Autofinance.dto.ClienteDTO;
import com.fag.Autofinance.entities.Cliente;
import com.fag.Autofinance.entities.Usuarios;
import com.fag.Autofinance.enums.StatusCadastros;
import com.fag.Autofinance.exception.JaExisteException;
import com.fag.Autofinance.exception.NaoEncontradoException;
import com.fag.Autofinance.repositories.ClienteRepository;
import com.fag.Autofinance.repositories.UsuarioRepository;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private Usuarios getUsuarioLogado() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new NaoEncontradoException("Usuário autenticado não encontrado"));
    }

    private String formatarCpfCnpj(String cpfCnpj) {
        if (cpfCnpj == null || cpfCnpj.isBlank()) {
            throw new IllegalArgumentException("CPF/CNPJ não pode ser vazio");
        }
        return cpfCnpj.replaceAll("\\D", "");
    }

    public List<ClienteDTO> listarTodos() {
        UUID empresaId = getUsuarioLogado().getEmpresa().getId();
        return clienteRepository.findAllByEmpresaId(empresaId)
                .stream()
                .map(ClienteDTO::new)
                .collect(Collectors.toList());
    }

    public ClienteDTO listarPorCpfCnpj(String cpfCnpj) {
        UUID empresaId = getUsuarioLogado().getEmpresa().getId();
        String cpfFormatado = formatarCpfCnpj(cpfCnpj);
        Cliente cliente = clienteRepository.findByCpfCnpjAndEmpresaId(cpfFormatado, empresaId)
                .orElseThrow(() -> new NaoEncontradoException("Cliente não encontrado"));
        return new ClienteDTO(cliente);
    }

    public List<ClienteDTO> listarPorStatus(StatusCadastros status) {
        UUID empresaId = getUsuarioLogado().getEmpresa().getId();
        return clienteRepository.findByStatusAndEmpresaId(status, empresaId)
                .stream()
                .map(ClienteDTO::new)
                .collect(Collectors.toList());
    }

    public Cliente criar(Cliente cliente) {
        Usuarios usuarioLogado = getUsuarioLogado();
        String cpfFormatado = formatarCpfCnpj(cliente.getCpfCnpj());

        if (clienteRepository.existsByCpfCnpjAndEmpresaId(cpfFormatado, usuarioLogado.getEmpresa().getId())) {
            throw new JaExisteException("Já existe um cliente com este CPF/CNPJ");
        }

        cliente.setCpfCnpj(cpfFormatado);
        cliente.setEmpresa(usuarioLogado.getEmpresa());
        cliente.setStatus(StatusCadastros.ATIVO);

        return clienteRepository.save(cliente);
    }

    public Cliente atualizar(String cpfCnpj, Cliente clienteAtualizado) {
        UUID empresaId = getUsuarioLogado().getEmpresa().getId();
        String cpfFormatado = formatarCpfCnpj(cpfCnpj);

        Cliente clienteExistente = clienteRepository.findByCpfCnpjAndEmpresaId(cpfFormatado, empresaId)
                .orElseThrow(() -> new NaoEncontradoException("Cliente não encontrado"));

        clienteExistente.setNome(clienteAtualizado.getNome());
        clienteExistente.setEmail(clienteAtualizado.getEmail());
        clienteExistente.setCelular(clienteAtualizado.getCelular());
        clienteExistente.setObservacoes(clienteAtualizado.getObservacoes());
        clienteExistente.setEndereco(clienteAtualizado.getEndereco());
        clienteExistente.setCep(clienteAtualizado.getCep());
        clienteExistente.setStatus(clienteAtualizado.getStatus());

        return clienteRepository.save(clienteExistente);
    }
}
