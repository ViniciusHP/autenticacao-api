package com.vhp.autenticacao.api.controller.dto;

import lombok.Getter;

@Getter
public class TokenDTO {
    private String tipo;
    private String token;

    public TokenDTO(String tipo, String token) {
        this.tipo = tipo;
        this.token = token;
    }
}
