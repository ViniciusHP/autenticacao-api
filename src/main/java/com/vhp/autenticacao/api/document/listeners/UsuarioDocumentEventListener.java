package com.vhp.autenticacao.api.document.listeners;

import com.vhp.autenticacao.api.document.Usuario;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;

import java.util.UUID;

/**
 * Listener para geração de chave UUID antes de inserir registro no banco MongoDB.
 */
public class UsuarioDocumentEventListener extends AbstractMongoEventListener<Usuario> {

    @Override
    public void onBeforeConvert(BeforeConvertEvent<Usuario> event) {
        super.onBeforeConvert(event);
        Usuario entity = event.getSource();

        if (entity.getId() == null) {
            entity.setId(UUID.randomUUID());
        }
    }
}
