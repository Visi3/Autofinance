package com.fag.Autofinance.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fag.Autofinance.entities.Usuarios;
import com.fag.Autofinance.enums.StatusCadastros;

public interface UsuarioRepository extends JpaRepository<Usuarios, UUID> {

    Optional<Usuarios> findByUsernameAndEmpresaId(String username, UUID empresaId);

    Optional<Usuarios> findByUsername(String username);

    List<Usuarios> findByStatusAndEmpresaId(StatusCadastros status, UUID empresaId);

    List<Usuarios> findAllByEmpresaId(UUID empresaId);

    Optional<Usuarios> findByEmail(String email);
}