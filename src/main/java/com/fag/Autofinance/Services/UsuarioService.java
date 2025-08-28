package com.fag.Autofinance.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.fag.Autofinance.dto.UsuariosDTO;
import com.fag.Autofinance.entities.Usuarios;
import com.fag.Autofinance.enums.StatusCadastros;
import com.fag.Autofinance.exception.NaoEncontradoException;
import com.fag.Autofinance.repositories.UsuarioRepository;

@Service
public class UsuarioService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private Usuarios getUsuarioLogado() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new NaoEncontradoException("Usuário autenticado não encontrado"));
    }

    public List<UsuariosDTO> listarTodos() {
        Long empresaId = getUsuarioLogado().getEmpresa().getId();
        return usuarioRepository.findAllByEmpresaId(empresaId)
                .stream()
                .map(UsuariosDTO::new)
                .collect(Collectors.toList());
    }

    public List<UsuariosDTO> listarPorStatus(StatusCadastros status) {
        Long empresaId = getUsuarioLogado().getEmpresa().getId();
        return usuarioRepository.findByStatusAndEmpresaId(status, empresaId)
                .stream()
                .map(UsuariosDTO::new)
                .collect(Collectors.toList());
    }

    public UsuariosDTO listarPorUsername(String username) {
        Long empresaId = getUsuarioLogado().getEmpresa().getId();
        Usuarios usuario = usuarioRepository.findByUsernameAndEmpresaId(username, empresaId)
                .orElseThrow(() -> new NaoEncontradoException("Usuário não encontrado"));
        return new UsuariosDTO(usuario);
    }

    public Usuarios criar(Usuarios usuarioNovo) {
        Usuarios usuarioLogado = getUsuarioLogado();

        usuarioNovo.setEmpresa(usuarioLogado.getEmpresa());
        usuarioNovo.setPassword(passwordEncoder.encode(usuarioNovo.getPassword()));
        usuarioNovo.setStatus(StatusCadastros.ATIVO);

        return usuarioRepository.save(usuarioNovo);
    }

    public Usuarios atualizar(String username, Usuarios usuarioAtualizado) {
        Long empresaId = getUsuarioLogado().getEmpresa().getId();

        Usuarios usuarioExistente = usuarioRepository.findByUsernameAndEmpresaId(username, empresaId)
                .orElseThrow(() -> new NaoEncontradoException("Usuário não encontrado"));

        usuarioExistente.setEmail(usuarioAtualizado.getEmail());
        usuarioExistente.setRole(usuarioAtualizado.getRole());
        usuarioExistente.setStatus(usuarioAtualizado.getStatus());

        if (usuarioAtualizado.getPassword() != null && !usuarioAtualizado.getPassword().isBlank()) {
            usuarioExistente.setPassword(passwordEncoder.encode(usuarioAtualizado.getPassword()));
        }

        return usuarioRepository.save(usuarioExistente);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new NaoEncontradoException("Usuário não encontrado"));
    }
}