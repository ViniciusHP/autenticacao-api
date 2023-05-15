package com.vhp.autenticacao.api.template;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TemplateHelperTest {

    @Mock
    private ITemplateEngine thymeleaf;

    @InjectMocks
    private TemplateHelper templateHelper;

    @Captor
    private ArgumentCaptor<Context> contextCaptor;

    private AutoCloseable autoCloseable;

    @BeforeEach
    public void beforeEach() {
        autoCloseable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    public void afterEach() throws Exception {
        autoCloseable.close();
    }

    @Test
    @DisplayName("#getConteudoTemplate SHOULD throw exception WHEN called with null template.")
    public void shouldThrowExceptionWhenCalledWithNullTemplate() {
        Throwable throwable = assertThrows(IllegalArgumentException.class,
                () -> templateHelper.getConteudoTemplate(null, new HashMap<>()));
        assertEquals("É necessário informar um template válido.", throwable.getMessage());
    }

    @Test
    @DisplayName("#getConteudoTemplate SHOULD throw exception WHEN called with empty template.")
    public void shouldThrowExceptionWhenCalledWithEmptyTemplate() {
        Throwable throwable = assertThrows(IllegalArgumentException.class,
                () -> templateHelper.getConteudoTemplate("", new HashMap<>()));
        assertEquals("É necessário informar um template válido.", throwable.getMessage());
    }

    @Test
    @DisplayName("#getConteudoTemplate SHOULD return template content WHEN called with template and no variables.")
    public void shouldReturnTemplateContentWhenCalledWithTemplateAndNoVariables() {
        Mockito.when(thymeleaf.process(Mockito.eq("fake-template-name"), contextCaptor.capture()))
                .thenReturn("my-content");
        String conteudoTemplate = templateHelper.getConteudoTemplate("fake-template-name", null);
        assertEquals("my-content", conteudoTemplate);
    }

    @Test
    @DisplayName("#getConteudoTemplate SHOULD return template content WHEN called with template and variables.")
    public void shouldReturnTemplateContentWhenCalledWithTemplateAndVariables() {

        HashMap<String, Object> variaveis = new HashMap<>();
        variaveis.put("test-1", "value-1");
        variaveis.put("test-2", "value-2");

        Mockito.when(thymeleaf.process(Mockito.eq("fake-template-name"), contextCaptor.capture()))
                .thenReturn("my-content");
        String conteudoTemplate = templateHelper.getConteudoTemplate("fake-template-name", variaveis);

        Context context = contextCaptor.getValue();
        assertEquals("my-content", conteudoTemplate);
        assertEquals("value-1", context.getVariable("test-1"));
        assertEquals("value-2", context.getVariable("test-2"));
    }
}
