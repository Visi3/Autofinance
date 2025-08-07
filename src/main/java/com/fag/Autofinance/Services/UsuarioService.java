package com.fag.Autofinance.services;

import java.util.List;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
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

    public List<Usuarios> listarTodos() {
        return usuarioRepository.findAll();
    }

    public List<Usuarios> listarPorStatus(StatusCadastros status) {
        return usuarioRepository.findByStatus(status);
    }

    public Usuarios listarPorUsername(String username) {
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }

    public Usuarios criar(Usuarios usuario) {
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        return usuarioRepository.save(usuario);
    }

    public Usuarios atualizar(String username, Usuarios usuarioAtualizado) {
        Usuarios usuarioExistente = usuarioRepository.findByUsername(username)
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
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
    }
}