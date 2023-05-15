package com.vhp.autenticacao.api.configurations.property;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Classe de propriedades de token JWT da aplicação.
 */
@Getter
@Setter
@NoArgsConstructor
public class JwtProperty {
    private String audience;
    private String issuer;
    private int accessTokenValiditySeconds = 1800;
    private int refreshTokenValiditySeconds = 3600 * 24;
    private String secret;
    private boolean secure;
}
