package com.fag.Autofinance.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fag.Autofinance.entities.ResetaSenha;

public interface ResetaSenhaRepository extends JpaRepository<ResetaSenha, Long> {

    Optional<ResetaSenha> findByToken(String token);
}