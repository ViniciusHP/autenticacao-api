package com.vhp.autenticacao.api.controller;

import com.vhp.autenticacao.api.controller.dto.TokenDTO;
import com.vhp.autenticacao.api.controller.form.LoginForm;
import com.vhp.autenticacao.api.service.OauthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

/**
 * Controller de autenticação de usuários.
 */
@RestController
@RequestMapping("/oauth")
public class OauthController {

    @Autowired
    private OauthService oauthService;

    @PostMapping("/token")
    public ResponseEntity<TokenDTO> autenticar(@RequestBody @Valid LoginForm loginForm, HttpServletRequest req, HttpServletResponse res) {
        try {
            TokenDTO tokenDTO = oauthService.logarUsuario(loginForm, req, res);
            return ResponseEntity.ok(tokenDTO);
        } catch (AuthenticationException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<TokenDTO> atualizarAutenticacao(HttpServletRequest req, HttpServletResponse res) {
        try {
            TokenDTO tokenDTO = oauthService.renovarLogin(req, res);
            return ResponseEntity.ok(tokenDTO);
        } catch (AuthenticationException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/revoke")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removerAutenticacao(HttpServletRequest req, HttpServletResponse res) {
        oauthService.removerRefreshToken(req, res);
    }
}
