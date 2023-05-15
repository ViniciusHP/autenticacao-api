package com.vhp.autenticacao.api.service;

import com.vhp.autenticacao.api.configurations.property.AutenticacaoProperty;
import com.vhp.autenticacao.api.configurations.property.RecuperacaoSenhaProperty;
import com.vhp.autenticacao.api.document.RecuperacaoSenha;
import com.vhp.autenticacao.api.document.Usuario;
import com.vhp.autenticacao.api.exceptions.ApplicationAbstractException;
import com.vhp.autenticacao.api.exceptions.TokenRedefinicaoSenhaExpiradoException;
import com.vhp.autenticacao.api.messages.MessageService;
import com.vhp.autenticacao.api.repository.RecuperacaoSenhaRepository;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.http.HttpStatus;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

class RecuperacaoSenhaServiceTest {

    @Mock
    private AutenticacaoProperty carteiraProperty;

    @Mock
    private RecuperacaoSenhaRepository recuperacaoSenhaRepository;

    @Mock
    private MessageService messageService;

    @Mock
    private Clock clock;

    @InjectMocks
    private RecuperacaoSenhaService recuperacaoSenhaService;

    @Captor
    private ArgumentCaptor<RecuperacaoSenha> recuperacaoCaptor;

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
    @DisplayName("#deleteAll SHOULD do nothing WHEN called with null parameter")
    public void shouldDoNothingWhenCalledWithNullParameter() {
        recuperacaoSenhaService.deleteAll(null);
        Mockito.verifyNoInteractions(recuperacaoSenhaRepository);
    }
    @Test
    @DisplayName("#deleteAll SHOULD do nothing WHEN called with empty parameter")
    public void shouldDoNothingWhenCalledWithEmptyParameter() {
        recuperacaoSenhaService.deleteAll(new ArrayList<>());
        Mockito.verifyNoInteractions(recuperacaoSenhaRepository);
    }

    @Test
    @DisplayName("#deleteAll SHOULD delete all password recovery records WHEN called with password recovery records to delete")
    public void shouldDeleteAllPasswordRecoveryRecordsWhenCalledWithPasswordRecoveryRecordsToDelete() {
        RecuperacaoSenha recuperacaoSenha = new RecuperacaoSenha(new ObjectId(), "fake-token", LocalDateTime.now(), new Usuario());
        List<RecuperacaoSenha> registrosExclusao = List.of(recuperacaoSenha);

        recuperacaoSenhaService.deleteAll(registrosExclusao);
        Mockito.verify(recuperacaoSenhaRepository).deleteAll(registrosExclusao);
    }

    @Test
    @DisplayName("#findRecuperacaoSenhaDataMenorQue SHOULD return password recovery list WHEN called")
    public void shouldReturnPasswordRecoveryListWhenCalled() {
        recuperacaoSenhaService.findRecuperacaoSenhaDataMenorQue(
                LocalDateTime.of(2023, 4, 23, 13, 42, 56),
                0,
                20);

        Mockito.verify(recuperacaoSenhaRepository).findByDataExpiracaoLesserThan(
                LocalDateTime.of(2023, 4, 23, 13, 42, 56),
                0,
                20);
    }

    @Test
    @DisplayName("#findRecuperacaoSenhaDataMenorQue SHOULD return password recovery list before now WHEN called with null date")
    public void shouldReturnPasswordRecoveryListBeforeNowWhenCalledWithNullDate() {

        LocalDateTime dateTime = LocalDateTime.of(2023, 4, 23, 13, 42, 56);
        Instant instant = dateTime.atZone(ZoneId.systemDefault())
                .toInstant();

        Mockito.when(clock.instant())
                .thenReturn(instant);
        Mockito.when(clock.getZone())
                .thenReturn(ZoneId.systemDefault());

        recuperacaoSenhaService.findRecuperacaoSenhaDataMenorQue(null, 0, 20);

        Mockito.verify(recuperacaoSenhaRepository).findByDataExpiracaoLesserThan(
                dateTime,
                0,
                20);
    }

    @Test
    @DisplayName("#buscarRecuperacaoSenhaToken SHOULD throw exception WHEN called with null token")
    public void findShouldThrowExceptionWhenCalledWithNullToken() {
        Mockito.when(messageService.getMessage("error.token-redefinicao-senha-expirado"))
                .thenReturn("error.token-redefinicao-senha-expirado");

        ApplicationAbstractException throwable = Assertions.assertThrows(TokenRedefinicaoSenhaExpiradoException.class,
                () -> recuperacaoSenhaService.buscarRecuperacaoSenhaToken(null));

        Assertions.assertEquals("error.token-redefinicao-senha-expirado", throwable.getMensagens().get(0).getMessage());
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, throwable.getHttpStatusCode());
    }

    @Test
    @DisplayName("#buscarRecuperacaoSenhaToken SHOULD throw exception WHEN called with empty token")
    public void findShouldThrowExceptionWhenCalledWithEmptyToken() {
        Mockito.when(messageService.getMessage("error.token-redefinicao-senha-expirado"))
                .thenReturn("error.token-redefinicao-senha-expirado");

        ApplicationAbstractException throwable = Assertions.assertThrows(TokenRedefinicaoSenhaExpiradoException.class,
                () -> recuperacaoSenhaService.buscarRecuperacaoSenhaToken(""));

        Assertions.assertEquals("error.token-redefinicao-senha-expirado", throwable.getMensagens().get(0).getMessage());
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, throwable.getHttpStatusCode());
    }

    @Test
    @DisplayName("#buscarRecuperacaoSenhaToken SHOULD throw exception WHEN called nonexistent token")
    public void shouldThrowExceptionWhenCalledNonexistentToken() {
        Mockito.when(messageService.getMessage("error.token-redefinicao-senha-expirado"))
                .thenReturn("error.token-redefinicao-senha-expirado");
        Mockito.when(recuperacaoSenhaRepository.findByToken("nonexistent-token"))
                .thenReturn(Optional.empty());

        ApplicationAbstractException throwable = Assertions.assertThrows(TokenRedefinicaoSenhaExpiradoException.class,
                () -> recuperacaoSenhaService.buscarRecuperacaoSenhaToken("nonexistent-token"));

        Assertions.assertEquals("error.token-redefinicao-senha-expirado", throwable.getMensagens().get(0).getMessage());
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, throwable.getHttpStatusCode());
    }

    @Test
    @DisplayName("#buscarRecuperacaoSenhaToken SHOULD throw exception WHEN saved token is expired")
    public void shouldThrowExceptionWhenSavedTokenIsExpired() {

        RecuperacaoSenha recuperacaoSenhaMock = new RecuperacaoSenha(
                new ObjectId(),
                "test-token",
                LocalDateTime.of(2023, 4, 23, 13, 56, 50),
                new Usuario());

        LocalDateTime now = LocalDateTime.of(2023, 4, 23, 13, 56, 51);
        Instant instant = now.atZone(ZoneId.systemDefault()).toInstant();

        Mockito.when(clock.instant())
                .thenReturn(instant);

        Mockito.when(clock.getZone())
                .thenReturn(ZoneId.systemDefault());

        Mockito.when(messageService.getMessage("error.token-redefinicao-senha-expirado"))
                .thenReturn("error.token-redefinicao-senha-expirado");

        Mockito.when(recuperacaoSenhaRepository.findByToken("test-token"))
                .thenReturn(Optional.of(recuperacaoSenhaMock));

        ApplicationAbstractException throwable = Assertions.assertThrows(TokenRedefinicaoSenhaExpiradoException.class,
                () -> recuperacaoSenhaService.buscarRecuperacaoSenhaToken("test-token"));

        Assertions.assertEquals("error.token-redefinicao-senha-expirado", throwable.getMensagens().get(0).getMessage());
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, throwable.getHttpStatusCode());
    }

    @Test
    @DisplayName("#buscarRecuperacaoSenhaToken SHOULD return password recovery token WHEN called")
    public void shouldReturnPasswordRecoveryTokenWhenCalled() {
        RecuperacaoSenha mock = new RecuperacaoSenha(
                new ObjectId(),
                "test-token",
                LocalDateTime.of(2023, 4, 23, 13, 56, 51),
                new Usuario());

        LocalDateTime now = LocalDateTime.of(2023, 4, 23, 13, 56, 50);
        Instant instant = now.atZone(ZoneId.systemDefault()).toInstant();

        Mockito.when(clock.instant())
                .thenReturn(instant);

        Mockito.when(clock.getZone())
                .thenReturn(ZoneId.systemDefault());

        Mockito.when(recuperacaoSenhaRepository.findByToken("test-token"))
                .thenReturn(Optional.of(mock));

        RecuperacaoSenha recuperacaoSenhaRetornado = recuperacaoSenhaService.buscarRecuperacaoSenhaToken("test-token");

        Assertions.assertEquals("test-token", recuperacaoSenhaRetornado.getToken());
        Assertions.assertEquals(mock.getId(), recuperacaoSenhaRetornado.getId());
        Assertions.assertEquals(mock.getDataExpiracao(), recuperacaoSenhaRetornado.getDataExpiracao());
        Assertions.assertEquals(mock.getUsuario(), recuperacaoSenhaRetornado.getUsuario());
    }

    @Test
    @DisplayName("#salvarRecuperacaoSenhaToken SHOULD throw exception WHEN called with null user")
    public void saveShouldThrowExceptionWhenCalledWithNullUser() {
        Throwable throwable = Assertions.assertThrows(IllegalArgumentException.class,
                () -> recuperacaoSenhaService.salvarRecuperacaoSenhaToken(null, "token"));
        Assertions.assertEquals("O usuário deve ser informado.", throwable.getMessage());
    }

    @Test
    @DisplayName("#salvarRecuperacaoSenhaToken SHOULD throw exception WHEN called with null token")
    public void saveShouldThrowExceptionWhenCalledWithNullToken() {
        Throwable throwable = Assertions.assertThrows(IllegalArgumentException.class,
                () -> recuperacaoSenhaService.salvarRecuperacaoSenhaToken(new Usuario(), null));
        Assertions.assertEquals("O token deve se informado.", throwable.getMessage());
    }

    @Test
    @DisplayName("#salvarRecuperacaoSenhaToken SHOULD throw exception WHEN called with empty token")
    public void saveShouldThrowExceptionWhenCalledWithEmptyToken() {
        Throwable throwable = Assertions.assertThrows(IllegalArgumentException.class,
                () -> recuperacaoSenhaService.salvarRecuperacaoSenhaToken(new Usuario(), ""));
        Assertions.assertEquals("O token deve se informado.", throwable.getMessage());
    }

    @Test
    @DisplayName("#salvarRecuperacaoSenhaToken SHOULD save password recovery token WHEN called")
    public void shouldSavePasswordRecoveryTokenWhenCalled() {

        RecuperacaoSenhaProperty recuperacaoSenhaProperty = new RecuperacaoSenhaProperty();
        recuperacaoSenhaProperty.setTokenValidityMinutes(60);

        Usuario usuarioMock = new Usuario();

        LocalDateTime now = LocalDateTime.of(2023, 4, 23, 14, 10, 10);
        Instant instant = now.atZone(ZoneId.systemDefault()).toInstant();

        Mockito.when(clock.instant())
                .thenReturn(instant);

        Mockito.when(clock.getZone())
                .thenReturn(ZoneId.systemDefault());

        Mockito.when(carteiraProperty.getRecuperacaoSenha())
                .thenReturn(recuperacaoSenhaProperty);

        recuperacaoSenhaService.salvarRecuperacaoSenhaToken(usuarioMock, "test-token");

        Mockito.verify(recuperacaoSenhaRepository).save(recuperacaoCaptor.capture());

        RecuperacaoSenha recuperacaoSenhaSalva = recuperacaoCaptor.getValue();

        Assertions.assertEquals("test-token", recuperacaoSenhaSalva.getToken());
        Assertions.assertEquals(usuarioMock, recuperacaoSenhaSalva.getUsuario());
        Assertions.assertEquals(LocalDateTime.of(2023, 4, 23, 15, 10, 10), recuperacaoSenhaSalva.getDataExpiracao());
    }

    @Test
    @DisplayName("#isTokenValido SHOULD return false WHEN called with null")
    public void shouldReturnFalseWhenCalledWithNull() {
        boolean isValido = recuperacaoSenhaService.isTokenValido(null);
        Assertions.assertFalse(isValido);
    }

    @Test
    @DisplayName("#isTokenValido SHOULD return false WHEN expiration date is before now")
    public void shouldReturnFalseWhenExpirationDateIsBeforeNow() {
        RecuperacaoSenha mock = new RecuperacaoSenha(
                new ObjectId(),
                "test-token",
                LocalDateTime.of(2023, 4, 23, 13, 56, 50),
                new Usuario());

        LocalDateTime now = LocalDateTime.of(2023, 4, 23, 13, 56, 51);
        Instant instant = now.atZone(ZoneId.systemDefault()).toInstant();

        Mockito.when(clock.instant())
                .thenReturn(instant);

        Mockito.when(clock.getZone())
                .thenReturn(ZoneId.systemDefault());

        boolean isValido = recuperacaoSenhaService.isTokenValido(mock);
        Assertions.assertFalse(isValido);
    }

    @Test
    @DisplayName("#isTokenValido SHOULD return true WHEN expiration date is after now")
    public void shouldReturnTrueWhenExpirationDateIsAfterNow() {
        RecuperacaoSenha mock = new RecuperacaoSenha(
                new ObjectId(),
                "test-token",
                LocalDateTime.of(2023, 4, 23, 13, 56, 51),
                new Usuario());

        LocalDateTime now = LocalDateTime.of(2023, 4, 23, 13, 56, 50);
        Instant instant = now.atZone(ZoneId.systemDefault()).toInstant();

        Mockito.when(clock.instant())
                .thenReturn(instant);

        Mockito.when(clock.getZone())
                .thenReturn(ZoneId.systemDefault());

        boolean isValido = recuperacaoSenhaService.isTokenValido(mock);
        Assertions.assertTrue(isValido);
    }
}
