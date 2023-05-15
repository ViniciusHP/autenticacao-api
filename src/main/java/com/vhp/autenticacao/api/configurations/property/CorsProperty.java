package com.vhp.autenticacao.api.configurations.property;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *  Classe de propriedades de CORS da aplicação.
 */
@Getter
@Setter
@NoArgsConstructor
public class CorsProperty {
    private String origemPermitida;
}
