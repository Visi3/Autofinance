package com.fag.Autofinance.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fag.Autofinance.Entities.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, String> {

}