package com.fag.Autofinance.services;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.fag.Autofinance.entities.ResetaSenha;
import com.fag.Autofinance.entities.Usuarios;
import com.fag.Autofinance.exception.NaoEncontradoException;
import com.fag.Autofinance.exception.TokenException;
import com.fag.Autofinance.repositories.ResetaSenhaRepository;
import com.fag.Autofinance.repositories.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ResetaSenhaService {

    private final UsuarioRepository usuariosRepository;
    private final ResetaSenhaRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public void solicitarResetSenha(String email) {
        System.out.println(email);
        Optional<Usuarios> usuarioOpt = usuariosRepository.findByEmail(email);

        if (usuarioOpt.isEmpty()) {
            throw new NaoEncontradoException("Usuário não encontrado com esse e-mail");
        }

        Usuarios usuario = usuarioOpt.get();

        String token = UUID.randomUUID().toString();
        LocalDateTime expiry = LocalDateTime.now().plusHours(1);

        ResetaSenha resetToken = new ResetaSenha(token, expiry, usuario);
        tokenRepository.save(resetToken);

        String link = "https://autofinance.azurewebsites.net/auth/resetar-senha?token=" + token;

        String htmlContent = """
                <html>
                    <body>
                        <h2>Recuperação de senha</h2>
                        <p>Olá, clique no link abaixo para redefinir sua senha:</p>
                        <a href='%s'>Redefinir senha</a>
                        <p>Esse link expira em 1 hora.</p>
                    </body>
                </html>
                """.formatted(link);

        emailService.enviarEmail(usuario.getEmail(), "Recuperação de senha", htmlContent);
    }

    public void redefinirSenha(String token, String novaSenha) {
        ResetaSenha resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new TokenException("Token inválido"));

        if (resetToken.isUsed()) {
            throw new TokenException("Token já utilizado");
        }

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new TokenException("Token expirado");
        }

        Usuarios usuario = resetToken.getUsuario();
        usuario.setPassword(passwordEncoder.encode(novaSenha));
        usuariosRepository.save(usuario);

        resetToken.setUsed(true);
        tokenRepository.save(resetToken);
    }
}
