package com.vhp.autenticacao.api.exceptions;

import com.vhp.autenticacao.api.messages.MessageService;
import org.springframework.http.HttpStatus;

public class EmailJaCadastradoException extends ApplicationAbstractException{

    public EmailJaCadastradoException(String email, MessageService messageService) {
        super(messageService);
        addMensagem("error.email-x-ja-foi-cadastrado", email);
        setStatus(HttpStatus.BAD_REQUEST);
    }
}
