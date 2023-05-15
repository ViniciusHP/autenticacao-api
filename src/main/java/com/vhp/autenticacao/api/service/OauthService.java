package com.vhp.autenticacao.api.service;

import com.vhp.autenticacao.api.configurations.property.AutenticacaoProperty;
import com.vhp.autenticacao.api.configurations.security.AutenticacaoService;
import com.vhp.autenticacao.api.configurations.security.TokenService;
import com.vhp.autenticacao.api.controller.dto.TokenDTO;
import com.vhp.autenticacao.api.controller.form.LoginForm;
import com.vhp.autenticacao.api.document.Usuario;
import com.vhp.autenticacao.api.exceptions.UsuarioNaoAutenticadoException;
import com.vhp.autenticacao.api.messages.MessageService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Serviço responsável pela autenticação oauth do usuário.
 */
@Service
public class OauthService {

    private final AutenticacaoProperty carteiraProperty;
    private final AuthenticationManager authManager;
    private final TokenService tokenService;
    private final AutenticacaoService autenticacaoService;
    private final MessageService messageService;

    @Autowired
    public OauthService(AutenticacaoProperty carteiraProperty, AuthenticationManager authManager, TokenService tokenService, AutenticacaoService autenticacaoService, MessageService messageService) {
        this.carteiraProperty = carteiraProperty;
        this.authManager = authManager;
        this.tokenService = tokenService;
        this.autenticacaoService = autenticacaoService;
        this.messageService = messageService;
    }

    /**
     * Autentica usuário e gera token JWT.
     *
     * @param loginForm Informações de autenticação do usuário.
     * @param req Requisição atual.
     * @param res Resposta.
     * @return Token JWT para usuário autenticado.
     */
    public TokenDTO logarUsuario(LoginForm loginForm, HttpServletRequest req, HttpServletResponse res) {
        Authentication authentication = authManager.authenticate(loginForm.converter());
        Usuario usuarioLogado = (Usuario) authentication.getPrincipal();

        String token = tokenService.gerarAccessToken(usuarioLogado);
        String refreshToken = tokenService.gerarRefreshToken(usuarioLogado);
        adicionarRefreshToken(refreshToken, req, res);

        return new TokenDTO("Bearer", token);
    }

    /**
     * Renova a autenticação do usuário e gera token JWT.
     *
     * @param req Requisição atual.
     * @param res Resposta.
     * @return Token JWT para usuário logado.
     */
    public TokenDTO renovarLogin(HttpServletRequest req, HttpServletResponse res) {
        String refreshToken = obterRefreshToken(req);
        if(tokenService.isTokenInvalido(refreshToken)) {
            throw new UsuarioNaoAutenticadoException(messageService);
        }

        UUID idUsuario = tokenService.getIdUsuario(refreshToken);
        Usuario usuario = autenticacaoService.loadUserById(idUsuario);
        return renovarLoginUsuario(usuario, req, res);
    }

    /**
     * Remove o refresh token do cookie do usuário.
     *
     * @param req Requisição atual.
     * @param res Resposta.
     */
    public void removerRefreshToken(HttpServletRequest req, HttpServletResponse res) {
        Cookie cookie = new Cookie("refreshToken", "");
        cookie.setPath(req.getContextPath() + "/oauth/refresh-token");
        cookie.setMaxAge(0);
        res.addCookie(cookie);
    }

    /**
     * Gera access token e refresh token. Adiciona refresh token no cookie da resposta e retorna objeto com o access token e o tipo de token.
     *
     * @param usuarioLogado Usuário que está autenticado.
     * @param req Requisição atual.
     * @param res Resposta.
     * @return Objeto com o access token e o tipo de token.
     */
    private TokenDTO renovarLoginUsuario(Usuario usuarioLogado, HttpServletRequest req, HttpServletResponse res) {
        String token = tokenService.gerarAccessToken(usuarioLogado);
        String refreshToken = tokenService.gerarRefreshToken(usuarioLogado);
        adicionarRefreshToken(refreshToken, req, res);
        return new TokenDTO("Bearer", token);
    }

    /**
     * Adiciona refresh token ao cookie de resposta.
     *
     * @param refreshToken - Refresh token gerado.
     * @param req Requisição atual.
     * @param res Resposta.
     */
    private void adicionarRefreshToken(String refreshToken, HttpServletRequest req, HttpServletResponse res) {
        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(carteiraProperty.getJwt().isSecure());
        cookie.setPath(String.format("%s/oauth/refresh-token", req.getContextPath()));
        cookie.setMaxAge(carteiraProperty.getJwt().getRefreshTokenValiditySeconds());
        res.addCookie(cookie);
    }

    /**
     * Obtém refresh token da requisição.
     *
     * @param req Requisição atual.
     * @return Refresh token, caso não exista refresh token, será retornado null.
     */
    private String obterRefreshToken(HttpServletRequest req) {
        return (String) req.getAttribute("refreshToken");
    }
}
