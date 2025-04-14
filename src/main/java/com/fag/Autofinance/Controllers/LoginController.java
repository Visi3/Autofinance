package com.fag.Autofinance.controllers;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fag.Autofinance.dto.LoginRequest;
import com.fag.Autofinance.dto.TokenResponse;
import com.fag.Autofinance.security.TokenService;

@RestController
@RequestMapping("/auth")
public class LoginController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private TokenService tokenService;

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest request) {
        try {
            Authentication authentication = autenticarUsuario(request);
            String token = gerarToken(authentication);
            return ResponseEntity.ok(new TokenResponse(token));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String authHeader) {
        // String token = extrairToken(authHeader);
        String email = tokenService.validarToken(authHeader);
        tokenService.revogarToken(email);
        return ResponseEntity.ok("Logout realizado com sucesso.");
    }

    private Authentication autenticarUsuario(LoginRequest request) {
        return authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getSenha()));
    }

    private String gerarToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return tokenService.gerarToken(userDetails);
    }

    /*
     * private String extrairToken(String authHeader) {
     * return authHeader.replace("Bearer ", "");
     * }
     */
}
