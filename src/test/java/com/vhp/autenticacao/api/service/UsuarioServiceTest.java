package com.vhp.autenticacao.api.service;

import com.vhp.autenticacao.api.configurations.property.AutenticacaoProperty;
import com.vhp.autenticacao.api.configurations.property.RecuperacaoSenhaProperty;
import com.vhp.autenticacao.api.controller.form.NovoUsuarioForm;
import com.vhp.autenticacao.api.controller.form.RecuperarSenhaForm;
import com.vhp.autenticacao.api.controller.form.RedefinirSenhaForm;
import com.vhp.autenticacao.api.document.RecuperacaoSenha;
import com.vhp.autenticacao.api.document.Usuario;
import com.vhp.autenticacao.api.exceptions.EmailJaCadastradoException;
import com.vhp.autenticacao.api.exceptions.EmailNaoCadastradoException;
import com.vhp.autenticacao.api.exceptions.TokenRedefinicaoSenhaExpiradoException;
import com.vhp.autenticacao.api.exceptions.UsuarioInvalidoException;
import com.vhp.autenticacao.api.messages.MessageService;
import com.vhp.autenticacao.api.model.EnvioEmail;
import com.vhp.autenticacao.api.repository.UsuarioRepository;
import com.vhp.autenticacao.api.template.TemplateHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UsuarioServiceTest {

    @Mock
    private AutenticacaoProperty carteiraProperty;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RecuperacaoSenhaService recuperacaoSenhaService;

    @Mock
    private EmailService emailService;

    @Mock
    private TemplateHelper templateService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private MessageService messageService;

    @InjectMocks
    private UsuarioService usuarioService;

    @Captor
    private ArgumentCaptor<Usuario> usuarioCaptor;

    @Captor
    private ArgumentCaptor<EnvioEmail> envioEmailCaptor;

    @Captor
    private ArgumentCaptor<Map<String, Object>> variaveisTemplateCaptor;

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
    @DisplayName("#isEmailDisponivel SHOULD return false WHEN unavailable email")
    public void shouldReturnFalseWhenUnavailableEmail() {
        String email = "unavailable@email.com";

        Mockito.when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(new Usuario()));
        boolean emailDisponivel = usuarioService.isEmailDisponivel(email);
        assertFalse(emailDisponivel);
    }

    @Test
    @DisplayName("#isEmailDisponivel SHOULD return true WHEN available email")
    public void shouldReturnTrueWhenAvailableEmail() {
        String email = "unavailable@email.com";

        Mockito.when(usuarioRepository.findByEmail(email)).thenReturn(Optional.empty());
        boolean emailDisponivel = usuarioService.isEmailDisponivel(email);
        assertTrue(emailDisponivel);
    }

    @Test
    @DisplayName("#novoUsuario SHOULD save user WHEN email is available")
    public void shouldSaveUserWhenEmailIsAvailable() {
        NovoUsuarioForm form = new NovoUsuarioForm();
        form.setName("Test");
        form.setEmail("test@email.com");
        form.setPassword("strongpassword");

        Mockito.when(passwordEncoder.encode("strongpassword")).thenReturn("$tr0ngpa$$w0rd");
        Mockito.when(usuarioRepository.findByEmail("test@email.com")).thenReturn(Optional.empty());

        usuarioService.novoUsuario(form);

        Mockito.verify(usuarioRepository).save(usuarioCaptor.capture());

        Usuario usuario = usuarioCaptor.getValue();
        assertEquals(form.getName(), usuario.getNome());
        assertEquals(form.getEmail(), usuario.getEmail());
        assertEquals(form.getPassword(), usuario.getPassword());
    }

    @Test
    @DisplayName("#novoUsuario SHOULD throw exception WHEN try create new user with unavailable email")
    public void shouldThrowExceptionWhenTryCreateNewUserWithUnavailableEmail() {
        NovoUsuarioForm form = new NovoUsuarioForm();
        form.setName("Test");
        form.setEmail("test@email.com");
        form.setPassword("strongpassword");

        Mockito.when(passwordEncoder.encode("strongpassword")).thenReturn("$tr0ngpa$$w0rd");
        Mockito.when(usuarioRepository.findByEmail("test@email.com")).thenReturn(Optional.of(new Usuario()));
        Mockito.when(messageService.getMessage("error.email-x-ja-foi-cadastrado", "test@email.com"))
                .thenReturn("error.email-x-ja-foi-cadastrado");

        EmailJaCadastradoException throwable = assertThrows(EmailJaCadastradoException.class, () -> {
            usuarioService.novoUsuario(form);
        });
        assertEquals("error.email-x-ja-foi-cadastrado", throwable.getMensagens().get(0).getMessage());
    }

    @Test
    @DisplayName("#recuperarSenha SHOULD throw exception WHEN called with email not registered")
    public void shouldThrowExceptionWhenCalledWithEmailNotRegistered() {
        RecuperarSenhaForm form = new RecuperarSenhaForm();
        form.setEmail("not_registered@email.com");

        Mockito.when(usuarioRepository.findByEmail(Mockito.any())).thenReturn(Optional.empty());
        Mockito.when(messageService.getMessage("error.email-x-nao-possui-cadastrado", "not_registered@email.com"))
                .thenReturn("error.email-x-nao-possui-cadastrado");

        EmailNaoCadastradoException throwable = assertThrows(EmailNaoCadastradoException.class, () -> {
            usuarioService.recuperarSenha(form);
        });
        assertEquals("error.email-x-nao-possui-cadastrado", throwable.getMensagens().get(0).getMessage());
    }

    @Test
    @DisplayName("#recuperarSenha SHOULD generate UUID token and send recovery email WHEN called with registered email")
    public void shouldGenerateUUIDTokenAndSendRecoveryEmailWhenCalledWithRegisteredEmail() {
        RecuperarSenhaForm form = new RecuperarSenhaForm();
        form.setEmail("registered@email.com");

        Usuario mockedUser = new Usuario();
        mockedUser.setNome("Test name");
        mockedUser.setEmail("registered@email.com");

        RecuperacaoSenha mockedRecuperacaoSenha = new RecuperacaoSenha();
        mockedRecuperacaoSenha.setToken("T0k3n");
        mockedRecuperacaoSenha.setDataExpiracao(LocalDateTime.of(2023, 4, 19, 20, 58, 51));
        mockedRecuperacaoSenha.setUsuario(mockedUser);

        RecuperacaoSenhaProperty mockedRecuperacaoSenhaProperty = new RecuperacaoSenhaProperty();
        mockedRecuperacaoSenhaProperty.setUrl("https://fake-url-recovery-password/");

        Mockito.when(usuarioRepository.findByEmail(Mockito.any()))
                .thenReturn(Optional.of(mockedUser));
        Mockito.when(recuperacaoSenhaService.salvarRecuperacaoSenhaToken(Mockito.eq(mockedUser), Mockito.any()))
                .thenReturn(mockedRecuperacaoSenha);
        Mockito.when(messageService.getMessage(Mockito.any()))
                .thenReturn("Recovery password");
        Mockito.when(carteiraProperty.getRecuperacaoSenha())
                .thenReturn(mockedRecuperacaoSenhaProperty);
        Mockito.when(templateService.getConteudoTemplate(Mockito.eq("mail/recuperar-senha"), Mockito.any()))
                .thenReturn("This is email body");

        usuarioService.recuperarSenha(form);

        Mockito.verify(templateService)
                .getConteudoTemplate(Mockito.eq("mail/recuperar-senha"), variaveisTemplateCaptor.capture());
        Mockito.verify(emailService)
                .registrarEnvioEmail(envioEmailCaptor.capture());

        Map<String, Object> variaveisTemplate = variaveisTemplateCaptor.getValue();
        EnvioEmail envioEmail = envioEmailCaptor.getValue();

        assertEquals("Test name", variaveisTemplate.get("nomeUsuario"));
        assertEquals("T0k3n", variaveisTemplate.get("token"));
        assertEquals("https://fake-url-recovery-password/", variaveisTemplate.get("urlRecuperacao"));
        assertEquals("registered@email.com", envioEmail.getPrimeiroDestinatario());
        assertEquals("Recovery password", envioEmail.getAssunto());
        assertEquals("This is email body", envioEmail.getCorpo());
    }

    @Test
    @DisplayName("#redefinirSenha SHOULD redefine user password WHEN password recovery requested and user exists")
    public void shouldRedefineUserPasswordWhenPasswordRecoveryRequestedAndUserExists() {

        RedefinirSenhaForm form = new RedefinirSenhaForm();
        form.setToken("T0k3n");
        form.setPassword("n3wp4ss0rd");

        RecuperacaoSenha recuperacaoSenha = new RecuperacaoSenha();
        Usuario user = new Usuario();
        user.setId(UUID.fromString("e621e1f8-c36c-495a-93fc-0c247a3e6e5f"));
        user.setSenha("0ldp4ss0rd");
        recuperacaoSenha.setUsuario(user);

        Mockito.when(recuperacaoSenhaService.buscarRecuperacaoSenhaToken("T0k3n"))
                .thenReturn(recuperacaoSenha);
        Mockito.when(passwordEncoder.encode(Mockito.any()))
                        .thenReturn("n3wp4ss0rd");

        usuarioService.redefinirSenha(form);

        Mockito.verify(usuarioRepository).save(usuarioCaptor.capture());

        Usuario savedUser = usuarioCaptor.getValue();

        assertEquals(UUID.fromString("e621e1f8-c36c-495a-93fc-0c247a3e6e5f"), savedUser.getId());
        assertEquals("n3wp4ss0rd", savedUser.getPassword());
    }

    @Test
    @DisplayName("#redefinirSenha SHOULD not redefine user password WHEN invalid token is used")
    public void shouldNotRedefineUserPasswordWhenInvalidTokenIsUsed() {

        RedefinirSenhaForm form = new RedefinirSenhaForm("n3wp4ss0rd", "invalid-token");

        Mockito.when(recuperacaoSenhaService.buscarRecuperacaoSenhaToken("invalid-token"))
                .thenReturn(null);
        Mockito.when(passwordEncoder.encode(Mockito.any()))
                .thenReturn("n3wp4ss0rd");
        Mockito.when(messageService.getMessage("error.token-redefinicao-senha-expirado"))
                .thenReturn("error.token-redefinicao-senha-expirado");

        TokenRedefinicaoSenhaExpiradoException throwable = assertThrows(TokenRedefinicaoSenhaExpiradoException.class,
                () -> usuarioService.redefinirSenha(form));

        assertEquals("error.token-redefinicao-senha-expirado", throwable.getMensagens().get(0).getMessage());
    }

    @Test
    @DisplayName("#findPorEmail SHOULD throw exception WHEN called with invalid username.")
    public void shouldThrowExceptionWhenCalledWithInvalidUsername() {
        Mockito.when(usuarioRepository.findByEmail("invalid-username"))
                .thenReturn(Optional.empty());
        Mockito.when(messageService.getMessage("error.dados-usuario-invalido"))
                .thenReturn("error.dados-usuario-invalido");

        UsuarioInvalidoException exception = assertThrows(UsuarioInvalidoException.class,
                () -> usuarioService.findPorEmail("invalid-username"));

        assertEquals("error.dados-usuario-invalido", exception.getMensagens().get(0).getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatusCode());
    }

    @Test
    @DisplayName("#findPorEmail SHOULD return user WHEN called with valid username.")
    public void shouldReturnUserWhenCalledWithValidUsername() {
        UUID uuid = UUID.fromString("e621e1f8-c36c-495a-93fc-0c247a3e6e5f");
        LocalDateTime now = LocalDateTime.now();

        Usuario mockedUsuario = new Usuario();
        mockedUsuario.setId(uuid);
        mockedUsuario.setNome("Test name");
        mockedUsuario.setEmail("test@email.com");
        mockedUsuario.setSenha("p4$$0rd");
        mockedUsuario.setAtivo(true);
        mockedUsuario.setDataCriacao(now);

        Mockito.when(usuarioRepository.findByEmail("test@email.com"))
                .thenReturn(Optional.of(mockedUsuario));

        Usuario usuarioRetornado = usuarioService.findPorEmail("test@email.com");

        assertEquals(uuid, usuarioRetornado.getId());
        assertEquals("Test name", usuarioRetornado.getNome());
        assertEquals("test@email.com", usuarioRetornado.getEmail());
        assertEquals("p4$$0rd", usuarioRetornado.getPassword());
        assertTrue(usuarioRetornado.isAtivo());
        assertEquals(now, usuarioRetornado.getDataCriacao());
    }

    @Test
    @DisplayName("#findPorId SHOULD throw exception WHEN called with invalid UUID.")
    public void shouldThrowExceptionWhenCalledWithInvalidUUID() {
        Mockito.when(usuarioRepository.findById(Mockito.any()))
                .thenReturn(Optional.empty());
        Mockito.when(messageService.getMessage("error.dados-usuario-invalido"))
                .thenReturn("error.dados-usuario-invalido");

        UsuarioInvalidoException exception = assertThrows(UsuarioInvalidoException.class,
                () -> usuarioService.findPorId(UUID.randomUUID()));

        assertEquals("error.dados-usuario-invalido", exception.getMensagens().get(0).getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatusCode());
    }

    @Test
    @DisplayName("#findPorId SHOULD return user WHEN called valid UUID.")
    public void shouldReturnUserWhenCalledWithValidUUID() {
        UUID uuid = UUID.fromString("e621e1f8-c36c-495a-93fc-0c247a3e6e5f");
        LocalDateTime now = LocalDateTime.now();

        Usuario mockedUsuario = new Usuario();
        mockedUsuario.setId(uuid);
        mockedUsuario.setNome("Test name");
        mockedUsuario.setEmail("test@email.com");
        mockedUsuario.setSenha("p4$$0rd");
        mockedUsuario.setAtivo(true);
        mockedUsuario.setDataCriacao(now);

        Mockito.when(usuarioRepository.findById(uuid))
                .thenReturn(Optional.of(mockedUsuario));

        Usuario usuarioRetornado = usuarioService.findPorId(uuid);

        assertEquals(uuid, usuarioRetornado.getId());
        assertEquals("Test name", usuarioRetornado.getNome());
        assertEquals("test@email.com", usuarioRetornado.getEmail());
        assertEquals("p4$$0rd", usuarioRetornado.getPassword());
        assertTrue(usuarioRetornado.isAtivo());
        assertEquals(now, usuarioRetornado.getDataCriacao());
    }

    @Test
    @DisplayName("#find SHOULD return user WHEN called valid UUID.")
    public void findShouldReturnUserWhenCalledWithValidUUID() {
        UUID uuid = UUID.fromString("e621e1f8-c36c-495a-93fc-0c247a3e6e5f");
        LocalDateTime now = LocalDateTime.now();

        Usuario mockedUsuario = new Usuario();
        mockedUsuario.setId(uuid);
        mockedUsuario.setNome("Test name");
        mockedUsuario.setEmail("test@email.com");
        mockedUsuario.setSenha("p4$$0rd");
        mockedUsuario.setAtivo(true);
        mockedUsuario.setDataCriacao(now);

        Mockito.when(usuarioRepository.findById(uuid))
                .thenReturn(Optional.of(mockedUsuario));

        Optional<Usuario> usuario = usuarioService.find(uuid);
        Usuario usuarioRetornado = usuario.get();

        assertEquals(uuid, usuarioRetornado.getId());
        assertEquals("Test name", usuarioRetornado.getNome());
        assertEquals("test@email.com", usuarioRetornado.getEmail());
        assertEquals("p4$$0rd", usuarioRetornado.getPassword());
        assertTrue(usuarioRetornado.isAtivo());
        assertEquals(now, usuarioRetornado.getDataCriacao());
    }

    @Test
    @DisplayName("#find SHOULD return empty WHEN called invalid UUID.")
    public void findShouldReturnEmptyWhenCalledWithInvalidUUID() {
        Mockito.when(usuarioRepository.findById(Mockito.any()))
                .thenReturn(Optional.empty());

        Optional<Usuario> usuario = usuarioService.find(null);
        assertTrue(usuario.isEmpty());
    }
}
