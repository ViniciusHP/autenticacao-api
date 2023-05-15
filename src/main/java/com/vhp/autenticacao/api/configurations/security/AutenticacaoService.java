package com.vhp.autenticacao.api.configurations.security;

import com.vhp.autenticacao.api.service.UsuarioService;
import com.vhp.autenticacao.api.document.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Serviço de autenticação de usuário no sistema.
 */
@Service
public class AutenticacaoService implements UserDetailsService {

    private final UsuarioService usuarioService;


    @Autowired
    public AutenticacaoService(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    /**
     * Busca usuário a partir do email.
     * @param email - Email do usuário
     * @return Usuário com email fornecido.
     */
    @Override
    public UserDetails loadUserByUsername(String email) {
        return usuarioService.findPorEmail(email);
    }

    /**
     * Busca usuário por seu id.
     * @param id - Id do usuário.
     * @return Usuário com Id fornecido.
     */
    public Usuario loadUserById(UUID id) {
        return usuarioService.findPorId(id);
    }
}
