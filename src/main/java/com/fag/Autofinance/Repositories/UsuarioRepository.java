package com.fag.Autofinance.repositories;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import com.fag.Autofinance.entities.Usuarios;
import com.fag.Autofinance.enums.StatusCadastros;

public interface UsuarioRepository extends JpaRepository<Usuarios, Long> {
    Optional<Usuarios> findByUsername(String username);

    List<Usuarios> findByStatus(StatusCadastros status);
}