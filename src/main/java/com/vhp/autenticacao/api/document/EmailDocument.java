package com.vhp.autenticacao.api.document;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Document("email")
public class EmailDocument {

    @MongoId
    private ObjectId id;
    private String emailRemetente;
    private String remetente;
    private List<String> destinatarios;
    private String assunto;
    private String corpo;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataProcessamento;

    @Indexed(unique = false)
    private EmailStatus status;

    private String mensagemErro;

    public void setDestinatario(String destinatario) {
        destinatarios = List.of(destinatario);
    }
}
