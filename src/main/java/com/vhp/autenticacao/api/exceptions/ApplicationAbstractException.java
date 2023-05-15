package com.vhp.autenticacao.api.exceptions;

import com.vhp.autenticacao.api.exceptionshandler.ApplicationExceptionMessage;
import com.vhp.autenticacao.api.messages.MessageService;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstração para exceções do sistema.
 */
public abstract class ApplicationAbstractException extends RuntimeException implements ApplicationException{

    private List<ApplicationExceptionMessage> mensagens = new ArrayList<>();
    private HttpStatus status = HttpStatus.BAD_REQUEST;

    private MessageService messageService;

    protected ApplicationAbstractException(MessageService messageService) {
        this.messageService = messageService;
    }

    public void addMensagem(String codigoMensagem, Object ...args) {
        String message = messageService.getMessage(codigoMensagem, args);
        ApplicationExceptionMessage applicationExceptionMessage = new ApplicationExceptionMessage();
        applicationExceptionMessage.setMessage(message);
        mensagens.add(applicationExceptionMessage);
    }

    public void setStatus(HttpStatus status) {
        if(status != null) {
            this.status = status;
        }
    }

    @Override
    public List<ApplicationExceptionMessage> getMensagens() {
        return mensagens;
    }

    @Override
    public HttpStatus getHttpStatusCode() {
        return status;
    }
}
