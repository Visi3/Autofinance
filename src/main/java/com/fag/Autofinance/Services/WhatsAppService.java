package com.fag.Autofinance.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fag.Autofinance.exception.EnviarException;
import com.twilio.Twilio;
import com.twilio.exception.ApiException;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

@Service
public class WhatsAppService {

    private static final Logger log = LoggerFactory.getLogger(WhatsAppService.class);

    private final String fromNumber;
    private final boolean enabled;

    public WhatsAppService(
            @Value("${twilio.accountSid}") String accountSid,
            @Value("${twilio.authToken}") String authToken,
            @Value("${twilio.whatsappFromNumber}") String fromNumber,

            @Value("${whatsapp.enabled:true}") boolean enabled) {

        Twilio.init(accountSid, authToken);
        this.fromNumber = fromNumber;
        this.enabled = enabled;

        if (!enabled) {
            log.warn("************************************************************");
            log.warn("MODO DE TESTE: O envio de mensagens via WhatsApp está DESABILITADO.");
            log.warn("Para habilitar, defina 'whatsapp.enabled=true' em application.properties");
            log.warn("************************************************************");
        }
    }

    public void enviarMensagem(String numeroDestino, String mensagem) {
        try {
            String numeroFormatado = formatarNumeroWhatsapp(numeroDestino);

            log.info("Enviando WhatsApp para: {}", numeroFormatado);
            log.debug("Mensagem: {}", mensagem);

            if (!enabled) {
                log.warn("Envio de WhatsApp pulado (serviço desabilitado).");
                return;
            }

            Message.creator(
                    new PhoneNumber(numeroFormatado),
                    new PhoneNumber(fromNumber),
                    mensagem).create();

            log.info("Mensagem enviada com sucesso.");

        } catch (ApiException e) {

            log.error("Erro da API do Twilio ao enviar para {}: {} (Código: {})",
                    numeroDestino, e.getMessage(), e.getCode(), e);
            throw new EnviarException(
                    "Não foi possível enviar a mensagem via WhatsApp. Verifique o número ou a conexão com o Twilio.");

        } catch (IllegalArgumentException e) {
            log.warn("Tentativa de envio de WhatsApp com número inválido: {}", e.getMessage());
            throw new EnviarException("Número de WhatsApp inválido: " + e.getMessage());

        } catch (Exception e) {
            log.error("Erro inesperado ao enviar WhatsApp para {}: {}", numeroDestino, e.getMessage(), e);
            throw new EnviarException("Erro inesperado ao tentar enviar a mensagem via WhatsApp.");
        }
    }

    private String formatarNumeroWhatsapp(String numero) {
        if (numero == null || numero.isBlank()) {
            throw new IllegalArgumentException("Número de WhatsApp não pode ser vazio.");
        }

        if (numero.trim().startsWith("+")) {

            String apenasNumeros = numero.substring(1).replaceAll("\\D", "");
            return "whatsapp:+" + apenasNumeros;
        }

        String apenasNumeros = numero.replaceAll("\\D", "");

        if (!apenasNumeros.startsWith("55")) {

            if (apenasNumeros.length() < 10 || apenasNumeros.length() > 11) {
                log.warn("Número de WhatsApp local ({}) parece ter formatação incorreta.", numero);

            }
            apenasNumeros = "55" + apenasNumeros;
        }

        return "whatsapp:+" + apenasNumeros;
    }
}