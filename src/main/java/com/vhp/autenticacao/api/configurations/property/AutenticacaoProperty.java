package com.vhp.autenticacao.api.configurations.property;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Classe de propriedades da aplicação.
 */
@ConfigurationProperties("autenticacao")
@Getter
public class AutenticacaoProperty {
    private final JwtProperty jwt = new JwtProperty();
    private final CorsProperty cors = new CorsProperty();
    private final MailProperty mail = new MailProperty();
    private final RecuperacaoSenhaProperty recuperacaoSenha = new RecuperacaoSenhaProperty();
}
