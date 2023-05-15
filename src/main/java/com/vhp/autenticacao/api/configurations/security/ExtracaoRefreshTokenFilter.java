package com.vhp.autenticacao.api.configurations.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro de extração do refresh token do Cookie.
 */
public class ExtracaoRefreshTokenFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        if("/oauth/refresh-token".equals(requestURI) && isComCookies(request)) {
            request.setAttribute("refreshToken", getRefreshTokenCookie(request.getCookies()));
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Verifica se a requisição possui cookies.
     * @param request - Requisição atual.
     * @return true se possui cookies, caso contrário, false
     */
    private boolean isComCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        return cookies != null && cookies.length > 0;
    }

    /**
     * Extrai refresh token dos cookies.
     * @param cookies - Lista de cookies da requisição.
     * @return refresh token caso esteja presente, caso contrário, null.
     */
    private String getRefreshTokenCookie(Cookie[] cookies) {
        for(Cookie cookie: cookies) {
            if(cookie.getName().equals("refreshToken")) {
                return cookie.getValue();
            }
        }

        return null;
    }
}
