package com.fag.Autofinance.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fag.Autofinance.dto.RequestReseta;
import com.fag.Autofinance.services.ResetaSenhaService;

import java.io.IOException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class ResetaSenhaController {

    private final ResetaSenhaService resetaSenhaService;

    @PostMapping("/resetar-senha")
    public ResponseEntity<String> solicitarReset(@RequestBody RequestReseta email) {
        resetaSenhaService.solicitarResetSenha(email.getEmail());
        return ResponseEntity.ok("E-mail de recuperação enviado!");
    }

    @PostMapping("/resetar-senha/confirmar")
    public void confirmarReset(@RequestParam("token") String token,
            @RequestParam("novaSenha") String novaSenha,
            HttpServletResponse response) throws IOException {
        resetaSenhaService.redefinirSenha(token, novaSenha);
        response.sendRedirect("/auth/resetar-senha/sucesso");
    }

    @GetMapping("/resetar-senha")
    public void exibirPaginaReset(@RequestParam("token") String token,
            HttpServletResponse response) {

        try {
            String html = """
                    <html>
                        <head>
                            <meta charset="UTF-8"/>
                            <title>Redefinir Senha - AutoFinance</title>
                            <style>
                                body {
                                    font-family: 'Inter', Arial, sans-serif;
                                    background-color: #f9fafb;
                                    display: flex;
                                    justify-content: center;
                                    align-items: center;
                                    height: 100vh;
                                }
                                .card {
                                    background: white;
                                    padding: 40px;
                                    border-radius: 12px;
                                    box-shadow: 0 4px 12px rgba(0,0,0,0.08);
                                    width: 380px;
                                    text-align: center;
                                }
                                .logo {
                                    display: flex;
                                    align-items: center;
                                    justify-content: center;
                                    gap: 8px;
                                    margin-bottom: 25px;
                                }
                                .logo-icon {
                                    background-color: #3B82F6;
                                    color: white;
                                    font-weight: bold;
                                    border-radius: 8px;
                                    width: 32px;
                                    height: 32px;
                                    display: flex;
                                    align-items: center;
                                    justify-content: center;
                                    font-size: 16px;
                                }
                                .logo-text {
                                    font-size: 20px;
                                    font-weight: 700;
                                    color: #111827;
                                }
                                h2 {
                                    color: #111827;
                                    margin-bottom: 20px;
                                }
                                label {
                                    display: block;
                                    margin: 10px 0 5px;
                                    font-size: 14px;
                                    font-weight: 500;
                                    color: #374151;
                                    text-align: left;
                                }
                                input[type="password"] {
                                    width: 100%;
                                    padding: 10px;
                                    border: 1px solid #d1d5db;
                                    border-radius: 8px;
                                    background-color: #f9fafb;
                                    margin-bottom: 5px;
                                    font-size: 14px;
                                }
                                button {
                                    background-color: #3B82F6;
                                    color: white;
                                    padding: 12px;
                                    border: none;
                                    border-radius: 8px;
                                    cursor: pointer;
                                    width: 100%;
                                    font-size: 15px;
                                    font-weight: 600;
                                    transition: background-color 0.2s;
                                    margin-top: 10px;
                                }
                                button:hover {
                                    background-color: #2563EB;
                                }
                                .error {
                                    color: #dc2626;
                                    font-size: 13px;
                                    margin-bottom: 10px;
                                    min-height: 18px; /* reserva espaço mesmo sem erro */
                                    visibility: hidden; /* invisível por padrão, mas ocupa espaço */
                                    text-align: left;
                                }
                                .error.show {
                                    visibility: visible; /* quando houver erro, fica visível */
                                }
                            </style>
                        </head>
                        <body>
                            <div class="card">
                                <div class="logo">
                                    <div class="logo-icon">A</div>
                                    <div class="logo-text">AutoFinance</div>
                                </div>
                                <h2>Redefinir Senha</h2>
                                <form method="POST" action="/auth/resetar-senha/confirmar" onsubmit="return validarSenha()">
                                    <input type="hidden" name="token" value="%s"/>
                                    <label>Nova senha</label>
                                    <input type="password" id="novaSenha" name="novaSenha" required/>
                                    <label>Confirmar senha</label>
                                    <input type="password" id="confirmarSenha" required/>
                                    <p id="erro" class="error">As senhas não coincidem.</p>
                                    <button type="submit">Salvar nova senha</button>
                                </form>
                            </div>
                            <script>
                                function validarSenha() {
                                    const senha = document.getElementById("novaSenha").value;
                                    const confirmar = document.getElementById("confirmarSenha").value;
                                    const erro = document.getElementById("erro");

                                    if (senha !== confirmar) {
                                        erro.classList.add("show");
                                        return false;
                                    }
                                    erro.classList.remove("show");
                                    return true;
                                }
                            </script>
                        </body>
                    </html>"""
                    .replace("%s", token);

            response.setContentType("text/html");
            response.getWriter().write(html);

        } catch (IOException e) {
            throw new RuntimeException("Erro ao renderizar página de reset", e);
        }
    }

    @GetMapping("/resetar-senha/sucesso")
    public void paginaSucesso(HttpServletResponse response) throws IOException {
        try {
            String html = """
                        <html>
                        <head>
                            <meta charset="UTF-8"/>
                            <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
                            <title>Senha Redefinida - AutoFinance</title>
                            <style>
                                body { font-family: 'Inter', Arial, sans-serif; background:#f9fafb; display:flex; justify-content:center; align-items:center; height:100vh; }
                                .card { background:white; padding:40px; border-radius:12px; box-shadow:0 4px 12px rgba(0,0,0,0.08); width:380px; text-align:center; }
                                .logo { display:flex; align-items:center; justify-content:center; gap:8px; margin-bottom:25px; }
                                .logo-icon { background:#3B82F6; color:white; font-weight:bold; border-radius:8px; width:32px; height:32px; display:flex; align-items:center; justify-content:center; font-size:16px; }
                                .logo-text { font-size:20px; font-weight:700; color:#111827; }
                                h2 { color:#111827; margin-bottom:20px; }
                                p { font-size:14px; color:#111827; margin-bottom:20px; }
                                a { display:inline-block; text-decoration:none; color:white; background:#3B82F6; padding:12px 24px; border-radius:8px; font-weight:600; }
                                a:hover { background:#2563EB; }
                            </style>
                        </head>
                        <body>
                            <div class="card">
                                <div class="logo">
                                    <div class="logo-icon">A</div>
                                    <div class="logo-text">AutoFinance</div>
                                </div>
                                <h2>Senha Redefinida com Sucesso!</h2>
                                <p>Sua senha foi alterada corretamente.</p>
                            </div>
                        </body>
                        </html>
                    """;
            response.setContentType("text/html");
            response.getWriter().write(html);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao renderizar página de confirmação", e);
        }
    }

}