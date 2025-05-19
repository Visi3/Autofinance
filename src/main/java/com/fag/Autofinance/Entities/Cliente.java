package com.fag.Autofinance.entities;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
public class Cliente {

    @Id
    @Column(name = "cpf_cnpj")
    private String cpfCnpj;

    @NotEmpty
    @Size(min = 2, max = 100)
    private String nome;

    @Pattern(regexp = "^(\\+\\d{1,2}\\s?)?\\(?\\d{2}\\)?\\s?\\d{4,5}-?\\d{4}$", message = "Formato de celular inválido")
    private String celular;

    @CreationTimestamp
    @Column(name = "data_cadastro", updatable = false, nullable = false)
    private LocalDateTime dataCadastro;

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Veiculo> veiculos = new HashSet<>();

    public String getCpfCnpj() {
        return cpfCnpj;
    }

    public void setCpfCnpj(String cpfCnpj) {
        this.cpfCnpj = cpfCnpj;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCelular() {
        return celular;
    }

    public void setCelular(String celular) {
        this.celular = celular;
    }

    public LocalDateTime getDataCadastro() {
        return dataCadastro;
    }

    public void setDataCadastro(LocalDateTime dataCadastro) {
        this.dataCadastro = dataCadastro;
    }

    public Set<Veiculo> getVeiculos() {
        return veiculos;
    }

    public void setVeiculos(Set<Veiculo> veiculos) {
        this.veiculos = veiculos;
    }

}
