package com.fag.Autofinance.services;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.fag.Autofinance.dto.CriarEmpresaDTO;
import com.fag.Autofinance.dto.EmpresaDTO;
import com.fag.Autofinance.entities.Empresa;
import com.fag.Autofinance.entities.Usuarios;
import com.fag.Autofinance.enums.RoleUsuario;
import com.fag.Autofinance.exception.JaExisteException;
import com.fag.Autofinance.repositories.EmpresaRepository;
import com.fag.Autofinance.repositories.UsuarioRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class EmpresaService {

    private final EmpresaRepository empresaRepository;
    private final UsuarioRepository usuariosRepository;
    private final PasswordEncoder passwordEncoder;

    public EmpresaService(EmpresaRepository empresaRepository,
            UsuarioRepository usuariosRepository,
            PasswordEncoder passwordEncoder) {
        this.empresaRepository = empresaRepository;
        this.usuariosRepository = usuariosRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private String formatarCnpj(String cnpj) {
        if (cnpj == null || cnpj.isBlank()) {
            throw new IllegalArgumentException("CNPJ não pode ser vazio");
        }
        return cnpj.replaceAll("\\D", "");
    }

    public EmpresaDTO criarEmpresaComAdmin(CriarEmpresaDTO dto) {
        String cnpjNormalizado = formatarCnpj(dto.getCnpj());

        boolean existe = empresaRepository.existsByCnpjIgnorePunctuation(cnpjNormalizado);
        if (existe) {
            throw new JaExisteException("Já existe uma empresa com este CNPJ.");
        }

        Empresa empresa = new Empresa();
        empresa.setNome(dto.getNome());
        empresa.setCnpj(cnpjNormalizado);
        empresa.setEndereco(dto.getEndereco());
        empresa.setCidade(dto.getCidade());
        empresa.setTelefone(dto.getTelefone());
        empresa.setCep(dto.getCep());

        empresaRepository.save(empresa);

        Usuarios admin = new Usuarios();
        admin.setNome(dto.getNomeAdmin());
        admin.setUsername(dto.getUsernameAdmin());
        admin.setEmail(dto.getEmailAdmin());
        admin.setPassword(passwordEncoder.encode(dto.getSenhaAdmin()));
        admin.setEmpresa(empresa);
        admin.setRole(RoleUsuario.ADMIN);
        admin.setTelefone(dto.getTelefone());

        usuariosRepository.save(admin);

        return new EmpresaDTO(empresa);
    }
}
