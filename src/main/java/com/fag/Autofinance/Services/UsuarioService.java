package com.fag.Autofinance.services;

import java.util.List;
import java.util.UUID;
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
import com.fag.Autofinance.exception.JaExisteException;
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
        UUID empresaId = getUsuarioLogado().getEmpresa().getId();
        return usuarioRepository.findAllByEmpresaId(empresaId)
                .stream()
                .map(UsuariosDTO::new)
                .collect(Collectors.toList());
    }

    public List<UsuariosDTO> listarPorStatus(StatusCadastros status) {
        UUID empresaId = getUsuarioLogado().getEmpresa().getId();
        return usuarioRepository.findByStatusAndEmpresaId(status, empresaId)
                .stream()
                .map(UsuariosDTO::new)
                .collect(Collectors.toList());
    }

    public UsuariosDTO listarPorUsername(String username) {
        UUID empresaId = getUsuarioLogado().getEmpresa().getId();
        Usuarios usuario = usuarioRepository.findByUsernameAndEmpresaId(username, empresaId)
                .orElseThrow(() -> new NaoEncontradoException("Usuário não encontrado"));
        return new UsuariosDTO(usuario);
    }

    public Usuarios criar(Usuarios usuarioNovo) {
        Usuarios usuarioLogado = getUsuarioLogado();

        boolean emailExistente = usuarioRepository.findByEmail(usuarioNovo.getEmail())
                .isPresent();
        if (emailExistente) {
            throw new JaExisteException("Esse email já esta sendo utilizado!");
        }

        usuarioNovo.setEmpresa(usuarioLogado.getEmpresa());
        usuarioNovo.setPassword(passwordEncoder.encode(usuarioNovo.getPassword()));
        usuarioNovo.setStatus(StatusCadastros.ATIVO);

        return usuarioRepository.save(usuarioNovo);
    }

    public Usuarios atualizar(String username, Usuarios usuarioAtualizado) {
        UUID empresaId = getUsuarioLogado().getEmpresa().getId();

        Usuarios usuarioExistente = usuarioRepository.findByUsernameAndEmpresaId(username, empresaId)
                .orElseThrow(() -> new NaoEncontradoException("Usuário não encontrado"));

        if (!usuarioExistente.getEmail().equals(usuarioAtualizado.getEmail())) {
            boolean emailExistente = usuarioRepository.findByEmail(usuarioAtualizado.getEmail())
                    .isPresent();
            if (emailExistente) {
                throw new JaExisteException("Esse email já esta sendo utilizado!");
            }
            usuarioExistente.setEmail(usuarioAtualizado.getEmail());
        }

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