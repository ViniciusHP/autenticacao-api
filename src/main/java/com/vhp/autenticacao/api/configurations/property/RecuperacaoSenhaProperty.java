package com.vhp.autenticacao.api.configurations.property;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *  Classe de propriedades de recuperação de senha da aplicação.
 */
@Getter
@Setter
@NoArgsConstructor
public class RecuperacaoSenhaProperty {
    private String url;
    private int tokenValidityMinutes = 60;
}
