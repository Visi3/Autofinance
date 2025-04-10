package com.fag.Autofinance.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fag.Autofinance.entities.Usuarios;

public interface UsuarioRepository extends JpaRepository<Usuarios, Long> {
    public Optional<Usuarios> findByEmail(String email);
}