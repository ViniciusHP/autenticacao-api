package com.vhp.autenticacao.api.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
public class EnvioEmail {

    private String assunto;
    private String corpo;
    private List<String> destinatarios = new ArrayList<>();

    public void setDestinatario(String destinatario) {
        destinatarios = Collections.singletonList(destinatario);
    }

    public String getPrimeiroDestinatario() {
        if(destinatarios == null || destinatarios.size() <= 0) {
            return null;
        }
        return destinatarios.get(0);
    }
}
