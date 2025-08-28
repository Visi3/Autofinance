package com.fag.Autofinance.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.fag.Autofinance.entities.Empresa;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, Long> {

    Optional<Empresa> findByCnpj(String cnpj);

    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END " +
            "FROM Empresa e " +
            "WHERE REPLACE(REPLACE(REPLACE(e.cnpj, '.', ''), '/', ''), '-', '') = :cnpj")
    boolean existsByCnpjIgnorePunctuation(@Param("cnpj") String cnpj);
}
