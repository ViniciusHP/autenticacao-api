package com.vhp.autenticacao.api.scheduler;

import com.vhp.autenticacao.api.document.EmailDocument;
import com.vhp.autenticacao.api.document.EmailStatus;
import com.vhp.autenticacao.api.mail.Email;
import com.vhp.autenticacao.api.mail.MailSender;
import com.vhp.autenticacao.api.mapper.EmailMapper;
import com.vhp.autenticacao.api.service.EmailService;
import jakarta.mail.MessagingException;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.io.UnsupportedEncodingException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class EmailSchedulerTest {

    @Mock
    private EmailService emailService;

    @Mock
    private MailSender mailSender;

    @Mock
    private EmailMapper emailMapper;

    @Mock
    private Clock clock;

    @InjectMocks
    private EmailScheduler emailScheduler;

    @Captor
    private ArgumentCaptor<List<EmailDocument>> emailDocumentListCaptor;

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
    @DisplayName("#envioEmail SHOULD do nothing WHEN it has no unprocessed records.")
    public void shouldDoNothinWhenItHasNoUnprocessedRecords() {
        when(emailService.buscarEmaisNaoProcessados()).thenReturn(new ArrayList<>());

        emailScheduler.envioEmail();

        verifyNoInteractions(mailSender);
        verify(emailService, never()).salvarEmails(any());
    }

    @Test
    @DisplayName("#envioEmail SHOULD send email and update email status WHEN called.")
    public void shouldSendEmailAndUpdateEmailStatusWhenCalled() throws MessagingException, UnsupportedEncodingException {
        List<EmailDocument> unprocessedEmailsDocumentMock = getUnprocessedEmailsDocumentMock();
        List<Email> unprocessedEmailsMock = getUnprocessedEmailsMock();

        LocalDateTime now = LocalDateTime.of(2023, 4, 26, 21, 48, 56);
        Instant instant = now.atZone(ZoneId.systemDefault()).toInstant();

        when(clock.getZone()).thenReturn(ZoneId.systemDefault());
        when(clock.instant()).thenReturn(instant);

        when(emailService.buscarEmaisNaoProcessados())
            .thenReturn(unprocessedEmailsDocumentMock);

        when(emailMapper.toEmail(unprocessedEmailsDocumentMock.get(0)))
            .thenReturn(unprocessedEmailsMock.get(0));

        when(emailMapper.toEmail(unprocessedEmailsDocumentMock.get(1)))
            .thenReturn(unprocessedEmailsMock.get(1));

        emailScheduler.envioEmail();

        verify(mailSender).enviarEmail(unprocessedEmailsMock.get(0));
        verify(mailSender).enviarEmail(unprocessedEmailsMock.get(1));
        verify(emailService).salvarEmails(emailDocumentListCaptor.capture());

        List<EmailDocument> updatedList = emailDocumentListCaptor.getValue();
        EmailDocument updatedEmail1 = updatedList.get(0);
        EmailDocument updatedEmail2 = updatedList.get(1);


        assertEquals(EmailStatus.PROCESSADO, updatedEmail1.getStatus());
        assertEquals(now, updatedEmail1.getDataProcessamento());
        assertEquals(EmailStatus.PROCESSADO, updatedEmail2.getStatus());
        assertEquals(now, updatedEmail2.getDataProcessamento());
    }

    @Test
    @DisplayName("#envioEmail SHOULD update email status WHEN called and not send email.")
    public void shouldUpdateEmailStatusWhenCalledAndNotSendEmail() throws MessagingException, UnsupportedEncodingException {
        List<EmailDocument> unprocessedEmailsDocumentMock = getUnprocessedEmailsDocumentMock();
        List<Email> unprocessedEmailsMock = getUnprocessedEmailsMock();

        LocalDateTime now = LocalDateTime.of(2023, 4, 26, 21, 48, 56);
        Instant instant = now.atZone(ZoneId.systemDefault()).toInstant();

        when(clock.getZone()).thenReturn(ZoneId.systemDefault());
        when(clock.instant()).thenReturn(instant);

        when(emailService.buscarEmaisNaoProcessados())
                .thenReturn(unprocessedEmailsDocumentMock);

        when(emailMapper.toEmail(unprocessedEmailsDocumentMock.get(0)))
                .thenReturn(unprocessedEmailsMock.get(0));

        when(emailMapper.toEmail(unprocessedEmailsDocumentMock.get(1)))
                .thenReturn(unprocessedEmailsMock.get(1));


        MessagingException exception = new MessagingException("test-exception-message");
        doThrow(exception)
            .when(mailSender)
            .enviarEmail(any());

        emailScheduler.envioEmail();

        verify(emailService).salvarEmails(emailDocumentListCaptor.capture());

        List<EmailDocument> updatedList = emailDocumentListCaptor.getValue();
        EmailDocument updatedEmail1 = updatedList.get(0);
        EmailDocument updatedEmail2 = updatedList.get(1);


        assertEquals(EmailStatus.ERRO, updatedEmail1.getStatus());
        assertEquals(now, updatedEmail1.getDataProcessamento());
        assertTrue(updatedEmail1.getMensagemErro().contains("test-exception-message"));
        assertEquals(EmailStatus.ERRO, updatedEmail2.getStatus());
        assertEquals(now, updatedEmail2.getDataProcessamento());
        assertTrue(updatedEmail2.getMensagemErro().contains("test-exception-message"));
    }

    private List<EmailDocument> getUnprocessedEmailsDocumentMock() {
        EmailDocument e1 = new EmailDocument();
        e1.setId(new ObjectId());
        e1.setStatus(EmailStatus.NAO_PROCESSADO);
        e1.setCorpo("Email-1 body");
        e1.setAssunto("Email-1 Subject");
        e1.setRemetente("Email-1");
        e1.setEmailRemetente("email_1@email.com");
        e1.setDataCriacao(LocalDateTime.now());
        e1.setDestinatario("email_1_recipient@email.com");

        EmailDocument e2 = new EmailDocument();
        e2.setId(new ObjectId());
        e2.setStatus(EmailStatus.NAO_PROCESSADO);
        e2.setCorpo("Email-2 body");
        e2.setAssunto("Email-2 Subject");
        e2.setRemetente("Email-2");
        e2.setEmailRemetente("email_2@email.com");
        e2.setDataCriacao(LocalDateTime.now());
        e2.setDestinatario("email_2_recipient@email.com");

        return Arrays.asList(e1, e2);
    }

    private List<Email> getUnprocessedEmailsMock() {
        Email e1 = new Email();
        e1.setCorpo("Email-1 body");
        e1.setAssunto("Email-1 Subject");
        e1.setRemetente("Email-1");
        e1.setEmailRemetente("email_1@email.com");

        Email e2 = new Email();
        e2.setCorpo("Email-2 body");
        e2.setAssunto("Email-2 Subject");
        e2.setRemetente("Email-2");
        e2.setEmailRemetente("email_2@email.com");

        return Arrays.asList(e1, e2);
    }
}
