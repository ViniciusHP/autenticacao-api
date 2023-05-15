package com.vhp.autenticacao.api.controller;

import com.vhp.autenticacao.api.controller.form.NovoUsuarioForm;
import com.vhp.autenticacao.api.controller.form.RecuperarSenhaForm;
import com.vhp.autenticacao.api.controller.form.RedefinirSenhaForm;
import com.vhp.autenticacao.api.mail.MailSender;
import com.vhp.autenticacao.api.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller para recursos de usuários.
 */
@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private MailSender mailer;

    @GetMapping("email-disponivel")
    public ResponseEntity<Void> isEmailDisponivel(@RequestParam(required = true) String email) {
        boolean isEmailDisponivel = usuarioService.isEmailDisponivel(email);
        return isEmailDisponivel
                ? ResponseEntity.noContent().build()
                : ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    @PostMapping()
    public ResponseEntity<Void> novoUsuario(@RequestBody() @Valid NovoUsuarioForm novoUsuarioForm) {
        usuarioService.novoUsuario(novoUsuarioForm);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("recuperar-senha")
    public ResponseEntity<Void> recuperarSenha(@RequestBody() @Valid RecuperarSenhaForm recuperarSenhaForm) {
        usuarioService.recuperarSenha(recuperarSenhaForm);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("redefinir-senha")
    public ResponseEntity<Void> redefinirSenha(@RequestBody() @Valid RedefinirSenhaForm redefinirSenhaForm) {
        usuarioService.redefinirSenha(redefinirSenhaForm);
        return ResponseEntity.noContent().build();
    }
}
