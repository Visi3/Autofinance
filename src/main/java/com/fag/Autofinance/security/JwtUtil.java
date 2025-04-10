package com.fag.Autofinance.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    private static final String SECRET_KEY = "umsegredobemgrandeaqui1234567890seguro";
    private static final long EXPIRATION_TIME = 1000 * 60 * 60;

    private final Tokens tokens;

    public JwtUtil(Tokens tokens) {
        this.tokens = tokens;
    }

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    public String gerarToken(UserDetails userDetails) {
        String token = Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();

        tokens.salvarToken(userDetails.getUsername(), token);
        return token;
    }

    public String extrairEmail(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validarToken(String token) {
        try {
            String email = extrairEmail(token);
            return tokens.tokenEhValido(email, token);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public void revogarToken(String email) {
        tokens.removerToken(email);
    }
}