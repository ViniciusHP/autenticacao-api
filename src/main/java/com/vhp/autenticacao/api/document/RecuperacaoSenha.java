package com.vhp.autenticacao.api.document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document("recuperacao_senha")
public class RecuperacaoSenha {

    @MongoId
    private ObjectId id;

    @Indexed(unique = true)
    private String token;

    private LocalDateTime dataExpiracao;

    @DBRef()
    private Usuario usuario;

}
