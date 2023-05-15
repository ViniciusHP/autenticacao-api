package com.vhp.autenticacao.api.exceptions;

import com.vhp.autenticacao.api.messages.MessageService;
import org.springframework.http.HttpStatus;

public class EmailNaoCadastradoException extends  ApplicationAbstractException{

    public EmailNaoCadastradoException(String email, MessageService messageService) {
        super(messageService);
        addMensagem("error.email-x-nao-possui-cadastrado", email);
        setStatus(HttpStatus.NOT_FOUND);
    }
}
