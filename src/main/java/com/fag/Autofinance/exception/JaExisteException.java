package com.fag.Autofinance.exception;

public class JaExisteException extends RuntimeException {
    public JaExisteException(String mensagem) {
        super(mensagem);
    }
}
