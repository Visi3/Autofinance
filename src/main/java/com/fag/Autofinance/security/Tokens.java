package com.fag.Autofinance.security;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class Tokens {
    private final Map<String, String> tokenMap = new ConcurrentHashMap<>();

    public void salvarToken(String email, String token) {
        tokenMap.put(email, token);
    }

    public String getToken(String email) {
        return tokenMap.get(email);
    }

    public void removerToken(String email) {
        tokenMap.remove(email);
    }
}
