package com.vhp.autenticacao.api.service;

import com.vhp.autenticacao.api.configurations.property.AutenticacaoProperty;
import com.vhp.autenticacao.api.configurations.property.MailProperty;
import com.vhp.autenticacao.api.document.EmailDocument;
import com.vhp.autenticacao.api.document.EmailStatus;
import com.vhp.autenticacao.api.model.EnvioEmail;
import com.vhp.autenticacao.api.repository.EmailRepository;
import org.junit.jupiter.api.*;
import org.mockito.*;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EmailServiceTest {

    @Mock
    private EmailRepository emailRepository;

    @Mock
    private AutenticacaoProperty carteiraProperty;

    @Mock
    private Clock clock;

    @InjectMocks
    private EmailService emailService;

    @Captor
    private ArgumentCaptor<List<EmailDocument>> listEmailCaptor;

    @Captor
    private ArgumentCaptor<EmailDocument> emailCaptor;

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
    @DisplayName("#buscarEmaisNaoProcessados SHOULD get 10 not processed emails with no skip WHEN called no arguments")
    void shouldGet10EmailsNotProcessed_whenCalledWithoutArguments() {
        emailService.buscarEmaisNaoProcessados();

        verify(emailRepository).findByStatus(EmailStatus.NAO_PROCESSADO, 0, 10);
    }

    @Test
    @DisplayName("#buscarEmaisNaoProcessados SHOULD get 20 not processed emails with no skip WHEN called with skip 0 and limit 20")
    void shouldGet20EmailsNotProcessed_whenCalledWith20Limit() {
        emailService.buscarEmaisNaoProcessados(0, 20);

        verify(emailRepository).findByStatus(EmailStatus.NAO_PROCESSADO, 0, 20);
    }

    @Test
    @DisplayName("#buscarEmaisNaoProcessados SHOULD get 20 not processed emails with 20 skip WHEN called with skip 20 and limit 20")
    void shouldGet20EmailsNotProcessedWith20Skips_whenCalledWith20SkipsAnd20Limit() {
        emailService.buscarEmaisNaoProcessados(20, 20);

        verify(emailRepository).findByStatus(EmailStatus.NAO_PROCESSADO, 20, 20);
    }

    @Test
    @DisplayName("#salvarEmails SHOULD save email list WHEN is called")
    void shouldSaveEmails() {
        EmailDocument emailDocument = new EmailDocument();
        emailDocument.setEmailRemetente("teste@email.com");
        emailDocument.setRemetente("remetente_teste");
        emailDocument.setDataCriacao(LocalDateTime.now());
        emailDocument.setStatus(EmailStatus.NAO_PROCESSADO);
        emailDocument.setAssunto("assunto teste");
        emailDocument.setDestinatarios(List.of("destinatario_teste@email.com"));
        emailDocument.setCorpo("corpo teste");

        emailService.salvarEmails(List.of(emailDocument));

        verify(emailRepository).saveAll(listEmailCaptor.capture());

        List<EmailDocument> emailsCaptor = listEmailCaptor.getValue();

        assertEquals(emailDocument.getEmailRemetente(), emailsCaptor.get(0).getEmailRemetente());
        assertEquals(emailDocument.getRemetente(), emailsCaptor.get(0).getRemetente());
        assertEquals(emailDocument.getDataCriacao(), emailsCaptor.get(0).getDataCriacao());
        assertEquals(emailDocument.getStatus(), emailsCaptor.get(0).getStatus());
        assertEquals(emailDocument.getAssunto(), emailsCaptor.get(0).getAssunto());
        assertEquals(emailDocument.getDestinatarios(), emailsCaptor.get(0).getDestinatarios());
        assertEquals(emailDocument.getCorpo(), emailsCaptor.get(0).getCorpo());
    }

    @Test
    @DisplayName("#salvarEmails SHOULD throw exception WHEN is called with empty list")
    void shouldThrowException_whenTrySaveEmptyList() {
        Throwable throwable = assertThrows(IllegalArgumentException.class,
                () -> emailService.salvarEmails(List.of()));
        assertEquals("Não foi possível salvar emails pois eles estão vazios.", throwable.getMessage());
    }

    @Test
    @DisplayName("#salvarEmails SHOULD throw exception WHEN is called with null")
    void shouldThrowException_whenTrySaveNullList() {
        Throwable throwable = assertThrows(IllegalArgumentException.class,
                () -> emailService.salvarEmails(null));
        assertEquals("Não foi possível salvar emails pois eles estão vazios.", throwable.getMessage());
    }

    @Test
    @DisplayName("#registrarEnvioEmail SHOULD register email for sending WHEN called")
    void shouldRegisterEmailForSending_whenCalled() {
        EnvioEmail envioEmail = new EnvioEmail();
        envioEmail.setCorpo("Email body");
        envioEmail.setAssunto("Email title");
        envioEmail.setDestinatarios(Arrays.asList("test@email.com", "mock_email@email.com"));

        MailProperty mail = new MailProperty();
        mail.setUsername("fake_username@email.com");
        mail.setRemetente("fake-username");

        LocalDateTime dataAtual = LocalDateTime.of(2022, 10, 31, 14,0,0);
        Instant intant = dataAtual.atZone(ZoneId.systemDefault())
                .toInstant();

        Mockito.when(clock.instant())
                .thenReturn(intant);
        Mockito.when(clock.getZone())
                .thenReturn(ZoneId.systemDefault());

        Instant now = Instant.now(clock);

        when(clock.instant()).thenReturn(now);
        when(carteiraProperty.getMail()).thenReturn(mail);

        emailService.registrarEnvioEmail(envioEmail);
        verify(emailRepository).save(emailCaptor.capture());

        EmailDocument envioEmailSalvo = emailCaptor.getValue();

        Assertions.assertEquals("Email body", envioEmailSalvo.getCorpo());
        Assertions.assertEquals("Email title", envioEmailSalvo.getAssunto());
        Assertions.assertEquals(Arrays.asList("test@email.com", "mock_email@email.com"), envioEmailSalvo.getDestinatarios());
        Assertions.assertEquals("fake_username@email.com", envioEmailSalvo.getEmailRemetente());
        Assertions.assertEquals("fake-username", envioEmailSalvo.getRemetente());
        Assertions.assertEquals(EmailStatus.NAO_PROCESSADO, envioEmailSalvo.getStatus());
        Assertions.assertEquals(dataAtual, envioEmailSalvo.getDataCriacao());
        Assertions.assertNull(envioEmailSalvo.getDataProcessamento());
    }

    @Test
    @DisplayName("#registrarEnvioEmail SHOULD throw exception WHEN called with null value")
    void shouldThrowException_whenCalledWithNullValue() {
        Throwable throwable = assertThrows(IllegalArgumentException.class,
                () -> emailService.registrarEnvioEmail(null));
        assertEquals("Não é possivel registrar email para envio de objeto nulo.", throwable.getMessage());
    }

    @Test
    @DisplayName("#registrarEnvioEmail SHOULD throw exception WHEN called with null body email")
    void shouldThrowException_whenCalledWithNullBodyEmail() {
        EnvioEmail envioEmail = new EnvioEmail();
        envioEmail.setAssunto("Email title");
        envioEmail.setDestinatarios(Arrays.asList("test@email.com", "mock_email@email.com"));

        Throwable throwable = assertThrows(IllegalArgumentException.class,
                () -> emailService.registrarEnvioEmail(envioEmail));
        assertEquals("Corpo do email não pode ser vazio para registrar email para envio.", throwable.getMessage());
    }

    @Test
    @DisplayName("#registrarEnvioEmail SHOULD throw exception WHEN called with empty body email")
    void shouldThrowException_whenCalledWithEmptyBodyEmail() {
        EnvioEmail envioEmail = new EnvioEmail();
        envioEmail.setCorpo("");
        envioEmail.setAssunto("Email title");
        envioEmail.setDestinatarios(Arrays.asList("test@email.com", "mock_email@email.com"));

        Throwable throwable = assertThrows(IllegalArgumentException.class,
                () -> emailService.registrarEnvioEmail(envioEmail));
        assertEquals("Corpo do email não pode ser vazio para registrar email para envio.", throwable.getMessage());
    }

    @Test
    @DisplayName("#registrarEnvioEmail SHOULD throw exception WHEN called with null subject email")
    void shouldThrowException_whenCalledWithNullSubjectEmail() {
        EnvioEmail envioEmail = new EnvioEmail();
        envioEmail.setCorpo("Email body");
        envioEmail.setDestinatarios(Arrays.asList("test@email.com", "mock_email@email.com"));

        Throwable throwable = assertThrows(IllegalArgumentException.class,
                () -> emailService.registrarEnvioEmail(envioEmail));
        assertEquals("Assunto de email não pode ser vazio para registrar email para envio.", throwable.getMessage());
    }

    @Test
    @DisplayName("#registrarEnvioEmail SHOULD throw exception WHEN called with empty subject email")
    void shouldThrowException_whenCalledWithEmptySubjectEmail() {
        EnvioEmail envioEmail = new EnvioEmail();
        envioEmail.setCorpo("Email body");
        envioEmail.setAssunto("");
        envioEmail.setDestinatarios(Arrays.asList("test@email.com", "mock_email@email.com"));

        Throwable throwable = assertThrows(IllegalArgumentException.class,
                () -> emailService.registrarEnvioEmail(envioEmail));
        assertEquals("Assunto de email não pode ser vazio para registrar email para envio.", throwable.getMessage());
    }

    @Test
    @DisplayName("#registrarEnvioEmail SHOULD throw exception WHEN called with null recipients email")
    void shouldThrowException_whenCalledWithNullRecipientsEmail() {
        EnvioEmail envioEmail = new EnvioEmail();
        envioEmail.setCorpo("Email body");
        envioEmail.setAssunto("Email assunto");

        Throwable throwable = assertThrows(IllegalArgumentException.class,
                () -> emailService.registrarEnvioEmail(envioEmail));
        assertEquals("Os destinatários devem ser informados para registrar email para envio.", throwable.getMessage());
    }

    @Test
    @DisplayName("#registrarEnvioEmail SHOULD throw exception WHEN called with empty recipients email")
    void shouldThrowException_whenCalledWithEmptyRecipientsEmail() {
        EnvioEmail envioEmail = new EnvioEmail();
        envioEmail.setCorpo("Email body");
        envioEmail.setAssunto("Email assunto");
        envioEmail.setDestinatarios(new ArrayList<>());

        Throwable throwable = assertThrows(IllegalArgumentException.class,
                () -> emailService.registrarEnvioEmail(envioEmail));

        assertEquals("Os destinatários devem ser informados para registrar email para envio.", throwable.getMessage());
    }

    @Test
    @DisplayName("#registrarEnvioEmail SHOULD throw exception WHEN called with null recipient email")
    void shouldThrowException_whenCalledWithNullRecipientEmail() {
        EnvioEmail envioEmail = new EnvioEmail();
        envioEmail.setCorpo("Email body");
        envioEmail.setAssunto("Email assunto");
        envioEmail.setDestinatarios(Arrays.asList("teste@email.com", null));

        Throwable throwable = assertThrows(IllegalArgumentException.class,
                () -> emailService.registrarEnvioEmail(envioEmail));

        assertEquals("Os conteúdo de destinatários não podem estar vazios ou nulos para registrar email para envio.", throwable.getMessage());
    }

    @Test
    @DisplayName("#registrarEnvioEmail SHOULD throw exception WHEN called with empty recipient email")
    void shouldThrowException_whenCalledWithEmptyRecipientEmail() {
        EnvioEmail envioEmail = new EnvioEmail();
        envioEmail.setCorpo("Email body");
        envioEmail.setAssunto("Email assunto");
        envioEmail.setDestinatarios(Arrays.asList("teste@email.com", ""));

        Throwable throwable = assertThrows(IllegalArgumentException.class,
                () -> emailService.registrarEnvioEmail(envioEmail));

        assertEquals("Os conteúdo de destinatários não podem estar vazios ou nulos para registrar email para envio.", throwable.getMessage());
    }
}
