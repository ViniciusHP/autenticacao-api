package com.vhp.autenticacao.api.mail;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * Classe responsável pelo envio de emails.
 */
@Component
public class MailSender {

    private final JavaMailSender mailSender;

    @Autowired
    public MailSender(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Envia email de acordo com objeto fornecido.
     * @param email - Objeto com informações para envio de email.
     * @throws MessagingException
     * @throws UnsupportedEncodingException
     * @throws IllegalArgumentException
     */
    public void enviarEmail(Email email) throws MessagingException, UnsupportedEncodingException, IllegalArgumentException {
        validateEmail(email);

        MimeMessage mimeMessage = mailSender.createMimeMessage();

        String emailRemetente = email.getEmailRemetente();
        String remetente = email.getRemetente();
        String assunto = email.getAssunto();
        String corpo = email.getCorpo();
        List<String> destinatarios = email.getDestinatarios();

        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");
        helper.setFrom(emailRemetente, remetente);
        helper.setTo(destinatarios.toArray(new String[0]));
        helper.setSubject(assunto);
        helper.setText(corpo, true);

        mailSender.send(mimeMessage);
    }

    /**
     * Valida informações do email.
     * @param email - Email a ser validado.
     * @throws IllegalArgumentException
     */
    private void validateEmail(Email email) throws IllegalArgumentException{
        if(email == null) {
            throw new IllegalArgumentException("É necessário informar um objeto de email não nulo.");
        }

        if(email.isEmailRemetenteEmpty()) {
            throw new IllegalArgumentException("O remetente do email é obrigatório.");
        }

        if(email.isDestinatariosEmpty()) {
            throw new IllegalArgumentException("Os destinatários do email são obrigatórios.");
        }
    }
}
