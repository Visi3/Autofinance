package com.fag.Autofinance.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.fag.Autofinance.Entities.Usuarios;
import com.fag.Autofinance.Repositories.UsuarioRepository;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public UsuarioService(UsuarioRepository usuarioRepository, BCryptPasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Usuarios registrarUsuario(Usuarios usuarios) {
        String senhaCriptografada = passwordEncoder.encode(usuarios.getSenha());
        usuarios.setSenha(senhaCriptografada);

        return usuarioRepository.save(usuarios);
    }

    public boolean autenticarUsuario(String email, String senha) {
        Usuarios usuarios = usuarioRepository.findbyEmail(email).orElse(null);
        if (usuarios != null && passwordEncoder.matches(senha, usuarios.getSenha())) {
            return true;
        }
        return false;
    }

}
