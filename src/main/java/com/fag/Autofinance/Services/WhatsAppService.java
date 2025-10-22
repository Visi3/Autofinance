package com.fag.Autofinance.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fag.Autofinance.exception.EnviarException;
import com.twilio.Twilio;
import com.twilio.exception.ApiException;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

@Service
public class WhatsAppService {

    private final String fromNumber;

    public WhatsAppService(
            @Value("${twilio.accountSid}") String accountSid,
            @Value("${twilio.authToken}") String authToken,
            @Value("${twilio.whatsappFromNumber}") String fromNumber) {

        Twilio.init(accountSid, authToken);
        if (!fromNumber.startsWith("whatsapp:")) {
            this.fromNumber = "whatsapp:" + fromNumber;
        } else {
            this.fromNumber = fromNumber;
        }
    }

    public void enviarMensagem(String numeroDestino, String mensagem) {
        try {
            String numeroFormatado = formatarNumeroWhatsapp(numeroDestino);
            System.out.println(mensagem);
            Message.creator(
                    new PhoneNumber(numeroFormatado),
                    new PhoneNumber(fromNumber),
                    mensagem).create();

        } catch (ApiException e) {
            throw new EnviarException(
                    "Não foi possível enviar a mensagem via WhatsApp. Verifique o número ou a conexão com o Twilio.");
        } catch (Exception e) {
            throw new EnviarException("Erro inesperado ao tentar enviar a mensagem via WhatsApp.");
        }
    }

    private String formatarNumeroWhatsapp(String numero) {
        if (numero == null || numero.isBlank()) {
            throw new IllegalArgumentException("Número de WhatsApp não pode ser vazio.");
        }

        // Remove tudo que não for número
        String apenasNumeros = numero.replaceAll("\\D", "");

        // Garante o código do país (Brasil = 55)
        if (!apenasNumeros.startsWith("55")) {
            apenasNumeros = "55" + apenasNumeros;
        }

        return "whatsapp:+" + apenasNumeros;
    }
}
