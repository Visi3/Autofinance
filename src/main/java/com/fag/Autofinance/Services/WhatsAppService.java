package com.fag.Autofinance.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.twilio.Twilio;
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

    public void enviarMensagem(String paraNumero, String mensagem) {
        String to = paraNumero.startsWith("whatsapp:") ? paraNumero : "whatsapp:" + paraNumero;

        System.out.println(to);
        System.out.println(mensagem);
        System.out.println(fromNumber);

        Message message = Message.creator(
                new PhoneNumber(to),
                new PhoneNumber(fromNumber),
                mensagem).create();

        System.out.println("Mensagem enviada SID: " + message.getSid());
    }
}
