package com.vhp.autenticacao.api.configurations.cors;

import com.vhp.autenticacao.api.configurations.property.AutenticacaoProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Classe responsável pela configuração do CORS da aplicação.
 */
@Configuration
@EnableWebMvc
public class CorsConfiguration implements WebMvcConfigurer {

    @Autowired
    private AutenticacaoProperty carteiraProperty;

    private final Logger logger = LoggerFactory.getLogger(CorsConfiguration.class);

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String origemPermitida = carteiraProperty.getCors().getOrigemPermitida();

        logger.info("Origem permitida: {}", origemPermitida);

        registry.addMapping("/**")
                .allowedOrigins(origemPermitida)
                .allowCredentials(true)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "TRACE", "CONNECT")
                .allowedHeaders("x-requested-with", "authorization", "Content-Type", "content-type", "Authorization", "credential", "X-XSRF-TOKEN", "Accept")
                .maxAge(3600L);
    }
}
