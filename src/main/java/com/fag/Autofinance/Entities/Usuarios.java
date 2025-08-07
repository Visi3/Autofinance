package com.fag.Autofinance.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fag.Autofinance.enums.RoleUsuario;
import com.fag.Autofinance.enums.StatusCadastros;

@Entity
@Getter
@Setter
public class Usuarios implements UserDetails {

    @Id
    @NotBlank
    @Size(min = 3, max = 20)
    @Column(unique = true, nullable = false)
    private String username;

    @Email
    private String email;

    private String telefone;

    @NotBlank
    private String password;

    @CreationTimestamp
    @Column(name = "data_cadastro", updatable = false, nullable = false)
    private LocalDateTime dataCadastro;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoleUsuario role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusCadastros status = StatusCadastros.ATIVO;

    public Usuarios() {
    }

    public Usuarios(String username, String password, String email, RoleUsuario role) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
        this.status = StatusCadastros.ATIVO;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(() -> "ROLE_" + role.name());
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return status == StatusCadastros.ATIVO;
    }

}
