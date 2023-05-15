package com.vhp.autenticacao.api.mail;

import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMessage.RecipientType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.mail.javamail.JavaMailSender;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MailSenderTest {

    @Mock
    private JavaMailSender javaMailSender;

    @InjectMocks
    private MailSender mailSender;

    @Captor
    private ArgumentCaptor<MimeMessage> captor;

    private AutoCloseable autoCloseable;

    @BeforeEach
    public void beforeEach() {
        autoCloseable = MockitoAnnotations.openMocks(this);
        Mockito.when(javaMailSender.createMimeMessage())
                .thenReturn(new MimeMessage((Session)null));
    }

    @AfterEach
    public void afterEach() throws Exception{
        autoCloseable.close();
    }

    @Test
    @DisplayName("#enviarEmail SHOULD send email WHEN is called")
    public void whenSendEmail_thenCorrectEmailSent() throws MessagingException, IOException {
        Email email = Email.builder()
                .remetente("remetente_teste")
                .emailRemetente("remetente_teste@gmail.com")
                .corpo("<p>corpo email teste</p>")
                .assunto("assunto teste")
                .destinatarios(Arrays.asList("testando@hotmail.com", "bugando@terra.com.br"))
                .build();

        mailSender.enviarEmail(email);

        Mockito.verify(javaMailSender).send(captor.capture());
        MimeMessage message = captor.getValue();

        InternetAddress addressFrom = (InternetAddress)Arrays.asList(message.getFrom()).get(0);

        List<String> addressTo = Arrays.stream(message.getRecipients(RecipientType.TO))
                .map((a) -> (InternetAddress) a)
                .map(InternetAddress::getAddress)
                .toList();

        assertEquals(email.getRemetente(), addressFrom.getPersonal());
        assertEquals(email.getEmailRemetente(), addressFrom.getAddress());
        assertEquals(email.getCorpo(), message.getContent().toString());
        assertEquals(email.getAssunto(), message.getSubject());
        assertEquals(email.getDestinatarios(), addressTo);
    }

    @Test
    @DisplayName("#enviarEmail SHOULD throw exception WHEN is called with email that doesn't contain sender email")
    public void whenSendEmailWithoutFrom_thenThrowException() throws MessagingException, IOException {
        Email email = Email.builder()
                .remetente("remetente_teste")
                .corpo("<p>corpo email teste</p>")
                .assunto("assunto teste")
                .destinatarios(Arrays.asList("testando@hotmail.com", "bugando@terra.com.br"))
                .build();

        ;
        Throwable throwable = assertThrows(IllegalArgumentException.class,
                () -> mailSender.enviarEmail(email));
        assertEquals("O remetente do email é obrigatório.", throwable.getMessage());
    }

    @Test
    @DisplayName("#enviarEmail SHOULD throw exception WHEN is called with email that doesn't contain recipient information")
    public void whenSendEmailWithoutTo_thenThrowException() throws MessagingException, IOException {
        Email email = Email.builder()
                .remetente("remetente_teste")
                .emailRemetente("remetente_teste@gmail.com")
                .corpo("<p>corpo email teste</p>")
                .assunto("assunto teste")
                .build();

        Throwable throwable = assertThrows(IllegalArgumentException.class,
                () -> mailSender.enviarEmail(email));
        assertEquals("Os destinatários do email são obrigatórios.", throwable.getMessage());
    }

    @Test
    @DisplayName("#enviarEmail SHOULD throw exception WHEN is called with null")
    public void whenSendNullEmail_thenThrowException() throws MessagingException, IOException {
        Throwable throwable = assertThrows(IllegalArgumentException.class,
                () -> mailSender.enviarEmail(null));
        assertEquals("É necessário informar um objeto de email não nulo.", throwable.getMessage());
    }
}
