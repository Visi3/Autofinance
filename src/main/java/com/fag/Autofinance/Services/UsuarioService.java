package com.fag.Autofinance.services;

import java.util.List;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.fag.Autofinance.entities.Usuarios;
import com.fag.Autofinance.repositories.UsuarioRepository;
import org.springframework.security.core.userdetails.User;

@Service
public class UsuarioService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Usuarios registrarUsuario(Usuarios usuarios) {
        return usuarioRepository.save(usuarios);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return usuarioRepository.findByEmail(email)
                .map(usuarios -> new User(
                        usuarios.getEmail(),
                        usuarios.getSenha(),
                        List.of(new SimpleGrantedAuthority("ROLE_" + usuarios.getTipo().name()))))
                .orElseThrow(() -> new UsernameNotFoundException("Email não encontrado"));
    }
}