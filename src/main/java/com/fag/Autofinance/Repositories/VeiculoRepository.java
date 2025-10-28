package com.fag.Autofinance.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.fag.Autofinance.entities.Veiculo;
import com.fag.Autofinance.enums.StatusCadastros;

public interface VeiculoRepository extends JpaRepository<Veiculo, String> {

    List<Veiculo> findByStatusAndEmpresaId(StatusCadastros status, UUID empresaId);

    @Query("""
            SELECT v FROM Veiculo v
            WHERE v.cliente.cpfCnpj = :cpfCnpj
              AND v.empresa.id = :empresaId
            ORDER BY
                CASE v.status
                    WHEN 'ATIVO' THEN 1
                    WHEN 'GERADO' THEN 2
                    WHEN 'INATIVO' THEN 3
                    ELSE 4
                END
            """)
    List<Veiculo> findByCliente_CpfCnpjAndEmpresaIdOrderByStatusCustom(String cpfCnpj, UUID empresaId);

    @Query("""
            SELECT v FROM Veiculo v
            WHERE LOWER(v.cliente.nome) LIKE LOWER(CONCAT('%', :nome, '%'))
              AND v.empresa.id = :empresaId
            ORDER BY
                CASE v.status
                    WHEN 'ATIVO' THEN 1
                    WHEN 'GERADO' THEN 2
                    WHEN 'INATIVO' THEN 3
                    ELSE 4
                END
            """)
    List<Veiculo> findByCliente_NomeContainingIgnoreCaseAndEmpresaIdOrderByStatusCustom(String nome, UUID empresaId);

    @Query("""
            SELECT v FROM Veiculo v
            WHERE v.empresa.id = :empresaId
            ORDER BY
                CASE v.status
                    WHEN 'ATIVO' THEN 1
                    WHEN 'GERADO' THEN 2
                    WHEN 'INATIVO' THEN 3
                    ELSE 4
                END
            """)
    List<Veiculo> findAllByEmpresaIdOrderByStatusCustom(UUID empresaId);

    Optional<Veiculo> findByPlacaAndEmpresaId(String placa, UUID empresaId);

    boolean existsByPlacaAndEmpresaId(String placa, UUID empresaId);

    long countByStatusAndEmpresaId(StatusCadastros status, UUID empresaId);

    // --- PARA DASHBOARD ---

    long countByEmpresaId(UUID empresaId);
}