package com.vhp.autenticacao.api.messages;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

/**
 * Serviço responsável pela tradução das mensagens que serão retornadas para a interface do sistema.
 */
@Service
public class MessageService {

    private final MessageSource messageSource;

    @Autowired
    public MessageService(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    /**
     * Obtém mensagem traduzida de acordo com o código da mensagem.
     * @param codigoMensagem Código associado a mensagem.
     * @param args Variáveis a serem interpoladas na mensagem.
     * @return Mensagem traduzida.
     */
    public String getMessage(String codigoMensagem, Object ...args) {
        if(codigoMensagem == null || codigoMensagem.isBlank()) {
            throw new IllegalArgumentException("Um código de mensagem de erro válido deve ser informado.");
        }
        return messageSource.getMessage(codigoMensagem, args, LocaleContextHolder.getLocale());
    }
}
