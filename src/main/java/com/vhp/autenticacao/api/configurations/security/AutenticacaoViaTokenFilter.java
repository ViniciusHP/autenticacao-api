package com.vhp.autenticacao.api.configurations.security;

import com.vhp.autenticacao.api.document.Usuario;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Filtro utilizado para autenticar usuário a partir do header Authorization.
 */
public class AutenticacaoViaTokenFilter extends OncePerRequestFilter {

    private final TokenService tokenService;

    private final AutenticacaoService autenticacaoService;

    public AutenticacaoViaTokenFilter(TokenService tokenService, AutenticacaoService autenticacaoService) {
        this.tokenService = tokenService;
        this.autenticacaoService = autenticacaoService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = recuperarToken(request);
        boolean isTokenValido = tokenService.isTokenValido(token);

        if(isTokenValido) {
            autenticarUsuario(token);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Autentica usuário a partir de access token.
     * @param token - Access token.
     */
    private void autenticarUsuario(String token) {
        UUID idUsuario = tokenService.getIdUsuario(token);
        Usuario usuario = autenticacaoService.loadUserById(idUsuario);

       if(usuario != null) {
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
    }

    /**
     * Extrai access token do header Authorization da requisição.
     * @param request - Requisição atual.
     * @return Access token caso header Authorization tenha token Bearer, caso contrário, retornará null.
     */
    private String recuperarToken(HttpServletRequest request) {
        String token = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (token == null || token.isBlank() || !token.startsWith("Bearer ")) {
            return null;
        }

        return token.substring(7, token.length());
    }
}
