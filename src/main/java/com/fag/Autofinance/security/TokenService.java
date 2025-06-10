package com.fag.Autofinance.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import java.util.Date;

@Component
public class TokenService {

    private static final String SECRET_KEY = "umsegredobemgrandeaqui1234567890seguro";
    private static final long EXPIRATION_TIME = 1000 * 60 * 60;

    private final Tokens tokens;

    public TokenService(Tokens tokens) {
        this.tokens = tokens;
    }

    public String gerarToken(UserDetails userDetails) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);
            String token = JWT.create()
                    .withSubject(userDetails.getUsername())
                    .withIssuer("login-api")
                    .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                    .sign(algorithm);

            tokens.salvarToken(userDetails.getUsername(), token);
            return token;

        } catch (JWTCreationException e) {
            throw new RuntimeException("Erro ao gerar token", e);
        }
    }

    public String validarToken(String token) {
        try {
            String cleanToken = token.replace("Bearer ", "");
            Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);
            String email = JWT.require(algorithm)
                    .withIssuer("login-api")
                    .build()
                    .verify(cleanToken)
                    .getSubject();

            String tokenSalvo = tokens.getToken(email);

            if (tokenSalvo != null && tokenSalvo.equals(cleanToken)) {
                return email;
            }

            throw new JWTVerificationException("Token não é mais válido ou foi revogado.");

        } catch (JWTVerificationException e) {
            throw new JWTVerificationException("Token inválido: " + e.getMessage());
        }
    }

    public void revogarToken(String email) {
        tokens.removerToken(email);
    }
}