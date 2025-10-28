package com.fag.Autofinance.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import com.fag.Autofinance.exception.EnviarException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

@Service

public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final boolean enabled;
    private final String fromEmail;

    public EmailService(
            JavaMailSender mailSender,
            @Value("${email.enabled:true}") boolean enabled,
            @Value("${spring.mail.username}") String fromEmail) {

        this.mailSender = mailSender;
        this.enabled = enabled;
        this.fromEmail = fromEmail;

        if (!enabled) {
            log.warn("************************************************************");
            log.warn("MODO DE TESTE: O envio de E-MAIL está DESABILITADO.");
            log.warn("Para habilitar, defina 'email.enabled=true' em application.properties");
            log.warn("************************************************************");
        }
    }

    public void enviarEmail(String para, String assunto, String conteudoHtml) {

        if (!enabled) {
            log.warn("Envio de email pulado (serviço desabilitado). Destinatário: {}", para);
            return;
        }

        try {
            log.info("Enviando email para: {} | Assunto: {}", para, assunto);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(para);
            helper.setSubject(assunto);
            helper.setText(conteudoHtml, true);

            mailSender.send(message);

            log.info("Email enviado com sucesso para: {}", para);

        } catch (MessagingException e) {

            log.error("Falha ao enviar email para {}: {}", para, e.getMessage(), e);
            throw new EnviarException("Erro ao enviar e-mail!");
        }
    }
}
