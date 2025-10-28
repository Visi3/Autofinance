package com.fag.Autofinance.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fag.Autofinance.entities.Usuarios;
import com.fag.Autofinance.enums.StatusCadastros;

public interface UsuarioRepository extends JpaRepository<Usuarios, UUID> {

    // (Específico da Empresa) Usado nas lógicas de CRUD (listar, atualizar)
    Optional<Usuarios> findByUsernameAndEmpresaId(String username, UUID empresaId);

    // (Global) Usado para Login e validação de duplicidade
    Optional<Usuarios> findByUsername(String username);

    // (Global) Usado para Reset de Senha e validação de duplicidade
    Optional<Usuarios> findByEmail(String email);

    // (Global) Validação de unicidade no 'criar()' (mais leve que 'findByUsername')
    boolean existsByUsername(String username);

    // (Específico da Empresa)
    List<Usuarios> findByStatusAndEmpresaId(StatusCadastros status, UUID empresaId);

    // (Específico da Empresa)
    List<Usuarios> findAllByEmpresaId(UUID empresaId);
}