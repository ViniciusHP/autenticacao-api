package com.vhp.autenticacao.api.exceptions;

import com.vhp.autenticacao.api.messages.MessageService;
import org.springframework.http.HttpStatus;

public class TokenRedefinicaoSenhaExpiradoException extends ApplicationAbstractException{

    public TokenRedefinicaoSenhaExpiradoException(MessageService messageService) {
        super(messageService);
        addMensagem("error.token-redefinicao-senha-expirado");
        setStatus(HttpStatus.UNAUTHORIZED);
    }
}
