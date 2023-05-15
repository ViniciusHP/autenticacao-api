package com.vhp.autenticacao.api.mail;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Email {
    private String emailRemetente;
    private String remetente;
    private List<String> destinatarios;
    private String assunto;
    private String corpo;

    public boolean isEmailRemetenteEmpty() {
        return emailRemetente == null || emailRemetente.isBlank();
    }

    public boolean isDestinatariosEmpty() {
        return destinatarios == null || destinatarios.isEmpty();
    }
}
