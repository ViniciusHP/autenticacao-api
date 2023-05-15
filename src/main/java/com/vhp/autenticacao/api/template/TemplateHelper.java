package com.vhp.autenticacao.api.template;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

/**
 * Serviço responsável por manipular páginas HTML com o Thymeleaf.
 */
@Service
public class TemplateHelper {

    private final ITemplateEngine thymeleaf;

    @Autowired
    public TemplateHelper(ITemplateEngine thymeleaf) {
        this.thymeleaf = thymeleaf;
    }

    /**
     * Obtém página HTML processada pelo Thymeleaf.
     *
     * @param template Template HTML
     * @param variaveis Variáveis a serem passadas para o HTML.
     * @return Conteúdo da página HTML processada.
     */
    public String getConteudoTemplate(String template, Map<String, Object> variaveis) {
        if(template == null || template.isBlank())
            throw new IllegalArgumentException("É necessário informar um template válido.");

        Context context = new Context(LocaleContextHolder.getLocale());

        if(variaveis != null ) {
            variaveis.forEach(context::setVariable);
        }

        return thymeleaf.process(template, context);
    }
}
