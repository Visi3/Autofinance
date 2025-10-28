package com.fag.Autofinance.repositories;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fag.Autofinance.entities.Empresa;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, UUID> {

    Optional<Empresa> findByCnpj(String cnpj);

    boolean existsByCnpj(String cnpjNormalizado);
}
