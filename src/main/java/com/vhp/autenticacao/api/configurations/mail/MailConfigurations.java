package com.vhp.autenticacao.api.configurations.mail;

import com.vhp.autenticacao.api.configurations.property.AutenticacaoProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

/**
 * Classe responsável pela configuração de envio de email.
 */
@Configuration
public class MailConfigurations {
    private final AutenticacaoProperty property;

    @Autowired
    public MailConfigurations(AutenticacaoProperty property) {
        this.property = property;
    }

    @Bean
    public JavaMailSender getJavaMailSender() {
        Properties props = new Properties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", true);
        props.put("mail.smtp.starttls.enable", true);
        props.put("mail.smtp.connectiontimeout", 10000);

        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setJavaMailProperties(props);
        mailSender.setHost(property.getMail().getHost());
        mailSender.setPort(property.getMail().getPort());
        mailSender.setUsername(property.getMail().getUsername());
        mailSender.setPassword(property.getMail().getPassword());

        return mailSender;
    }
}
