package com.fag.Autofinance.services;

import java.time.LocalDateTime;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ResetaSenhaService {

    private final UsuarioRepository usuariosRepository;
    private final ResetaSenhaRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    private static final Logger log = LoggerFactory.getLogger(ResetaSenhaService.class);

    @Value("${app.frontend.reset-url}")
    private String resetUrl;

    @Transactional
    public void solicitarResetSenha(String email) {

        log.info("Iniciando solicitação de reset de senha para: {}", email);

        Usuarios usuario = usuariosRepository.findByEmail(email)
                .orElseThrow(() -> new NaoEncontradoException("Usuário não encontrado com esse e-mail"));

        log.debug("Invalidando tokens de reset antigos para o usuário: {}", usuario.getUsername());
        tokenRepository.deleteByUsuarioAndUsedIsFalse(usuario);

        String token = UUID.randomUUID().toString();
        LocalDateTime expiry = LocalDateTime.now().plusHours(1);

        ResetaSenha resetToken = new ResetaSenha(token, expiry, usuario);
        tokenRepository.save(resetToken);

        String link = resetUrl + "?token=" + token;

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
        log.info("Email de reset de senha enviado para: {}", email);
    }

    @Transactional
    public void redefinirSenha(String token, String novaSenha) {
        log.info("Tentando redefinir senha com token...");

        ResetaSenha resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new TokenException("Token inválido"));

        if (resetToken.isUsed()) {
            log.warn("Token de reset já utilizado: {}", token);
            throw new TokenException("Token já utilizado");
        }

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            log.warn("Token de reset expirado: {}", token);
            throw new TokenException("Token expirado");
        }

        Usuarios usuario = resetToken.getUsuario();
        usuario.setPassword(passwordEncoder.encode(novaSenha));
        usuariosRepository.save(usuario);

        resetToken.setUsed(true);
        tokenRepository.save(resetToken);

        log.info("Senha redefinida com sucesso para o usuário: {}", usuario.getUsername());
    }
}