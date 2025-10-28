package com.fag.Autofinance.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import com.fag.Autofinance.entities.ResetaSenha;
import com.fag.Autofinance.entities.Usuarios;

public interface ResetaSenhaRepository extends JpaRepository<ResetaSenha, Long> {

    Optional<ResetaSenha> findByToken(String token);

    // Deleta outros tokens
    @Modifying
    void deleteByUsuarioAndUsedIsFalse(Usuarios usuario);
}