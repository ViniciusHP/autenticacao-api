package com.vhp.autenticacao.api.exceptions;

import com.vhp.autenticacao.api.messages.MessageService;
import org.springframework.http.HttpStatus;

public class UsuarioInvalidoException extends ApplicationAbstractException {

    public UsuarioInvalidoException(MessageService messageService) {
        super(messageService);
        addMensagem("error.dados-usuario-invalido");
        setStatus(HttpStatus.BAD_REQUEST);
    }
}
