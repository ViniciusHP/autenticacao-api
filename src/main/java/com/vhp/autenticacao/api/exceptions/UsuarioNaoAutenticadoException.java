package com.vhp.autenticacao.api.exceptions;

import com.vhp.autenticacao.api.messages.MessageService;
import org.springframework.http.HttpStatus;

public class UsuarioNaoAutenticadoException extends ApplicationAbstractException{
    public UsuarioNaoAutenticadoException(MessageService messageService) {
        super(messageService);
        addMensagem("error.usuario-nao-autenticado");
        setStatus(HttpStatus.UNAUTHORIZED);
    }
}
