package com.vhp.autenticacao.api.configurations.mongodb;

import com.vhp.autenticacao.api.document.listeners.UsuarioDocumentEventListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Classe responsável por prover listeners relacionados ao banco MongoDB
 */
@Configuration
public class MongoDBConfigurations {
    @Bean
    public UsuarioDocumentEventListener getUsuarioDocumentEventListener() {
        return new UsuarioDocumentEventListener();
    }
}
