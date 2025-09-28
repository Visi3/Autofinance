package com.fag.Autofinance.entities;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Empresa {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "UUID", updatable = false, nullable = false)
    private UUID id;

    @NotBlank
    @Size(min = 3, max = 100)
    private String nome;

    @NotBlank
    @Column(unique = true)
    private String cnpj;

    private String endereco;

    private String cidade;

    private String telefone;

    private String cep;

    @CreationTimestamp
    @Column(name = "data_cadastro", updatable = false)
    private LocalDateTime dataCadastro;

    @OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL)
    private List<Usuarios> usuarios;

    @OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL)
    private List<Cliente> clientes;

    @OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL)
    private List<Veiculo> veiculos;

    @OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL)
    private List<Servico> servicos;

    @OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL)
    private List<Orcamento> orcamentos;

}
