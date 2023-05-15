package com.vhp.autenticacao.api.configurations.mail;

import com.vhp.autenticacao.api.configurations.property.AutenticacaoProperty;
import com.vhp.autenticacao.api.configurations.property.MailProperty;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class MailConfigurationsTest {

    @Mock
    private AutenticacaoProperty property;

    @InjectMocks
    private MailConfigurations mailConfigurations;

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
    @DisplayName("#getJavaMailSender SHOULD configure Java Mail Sender WHEN called.")
    public void shouldConfigureJavaMailSenderWhenCalled() {
        MailProperty mailProperty = new MailProperty();
        mailProperty.setUsername("Test-Username");
        mailProperty.setPassword("p4$$0rd");
        mailProperty.setPort(4200);
        mailProperty.setHost("smtp-mail.outlook.com");

        when(property.getMail()).thenReturn(mailProperty);

        JavaMailSenderImpl javaMailSender = (JavaMailSenderImpl) mailConfigurations.getJavaMailSender();
        Properties javaMailProperties = javaMailSender.getJavaMailProperties();

        assertEquals("Test-Username", javaMailSender.getUsername());
        assertEquals("p4$$0rd", javaMailSender.getPassword());
        assertEquals(4200, javaMailSender.getPort());
        assertEquals("smtp-mail.outlook.com", javaMailSender.getHost());
        assertEquals("smtp", javaMailProperties.getProperty("mail.transport.protocol"));
        assertTrue((boolean) javaMailProperties.get("mail.smtp.auth"));
        assertTrue((boolean) javaMailProperties.get("mail.smtp.starttls.enable"));
        assertEquals(10000, (int) javaMailProperties.get("mail.smtp.connectiontimeout"));
    }
}
