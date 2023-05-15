package com.vhp.autenticacao.api.configurations.security;

import com.vhp.autenticacao.api.configurations.property.AutenticacaoProperty;
import com.vhp.autenticacao.api.configurations.property.JwtProperty;
import com.vhp.autenticacao.api.document.Usuario;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.TextCodec;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TokenServiceTest {

    @Mock
    private AutenticacaoProperty carteiraProperty;

    @Mock
    private Clock clock;

    @InjectMocks
    private TokenService tokenService;

    private AutoCloseable autoCloseable;

    private JwtProperty jwtProperty;

    private UUID usuarioId;

    @BeforeEach
    public void beforeEach() {
        autoCloseable = MockitoAnnotations.openMocks(this);

        usuarioId = UUID.fromString("e621e1f8-c36c-495a-93fc-0c247a3e6e5f");
        jwtProperty = new JwtProperty();
        jwtProperty.setSecret("SW52aXN0YSBlbSB2b2NlLCB2b2NlIGUgbyBtZWxob3IK");
        jwtProperty.setAudience("TESTE.API");
        jwtProperty.setIssuer("http://localhost:8080");
        jwtProperty.setRefreshTokenValiditySeconds(360000);
        jwtProperty.setAccessTokenValiditySeconds(1200);
        Mockito.when(carteiraProperty.getJwt()).thenReturn(jwtProperty);
    }

    public String gerarToken(Usuario usuario, Instant hoje, Instant dataExpiracao) {
        return Jwts.builder()
                .setIssuer(jwtProperty.getIssuer())
                .setAudience(jwtProperty.getAudience())
                .setSubject(usuario.getId().toString())
                .claim("name", "test-name")
                .setIssuedAt(Date.from(hoje))
                .setExpiration(Date.from(dataExpiracao))
                .signWith(SignatureAlgorithm.HS256, TextCodec.BASE64.decode(jwtProperty.getSecret()))
                .compact();
    }

    @AfterEach
    public void afterEach() throws Exception {
        autoCloseable.close();
    }

    @Test
    @DisplayName("#gerarAccessToken SHOULD generate access token WHEN called.")
    public void shouldGenerateAccessTokenWhenCalled() {

        Instant hoje = LocalDateTime.of(2023, 5, 3, 22, 40, 5)
                .atZone(ZoneId.systemDefault())
                .toInstant();

        Instant dataExpiracao = hoje.plus(jwtProperty.getAccessTokenValiditySeconds(), ChronoUnit.SECONDS);

        Mockito.when(clock.getZone())
                .thenReturn(ZoneId.systemDefault());
        Mockito.when(clock.instant())
                .thenReturn(hoje);


        Usuario usuario = new Usuario();
        usuario.setNome("test-name");
        usuario.setId(usuarioId);

        String myToken = gerarToken(usuario, hoje, dataExpiracao);

        String generatedToken = tokenService.gerarAccessToken(usuario);

        assertEquals(myToken, generatedToken);
    }

    @Test
    @DisplayName("#gerarAccessToken SHOULD throw exception WHEN param user is null.")
    public void gerarAccessTokenShouldThrowExceptionWhenParamUserIsNull() {
        Throwable throwable = assertThrows(IllegalArgumentException.class,
                () -> tokenService.gerarAccessToken(null));

        assertEquals("Usuário inválido.", throwable.getMessage());
    }

    @Test
    @DisplayName("#gerarAccessToken SHOULD throw exception WHEN user has no ID.")
    public void gerarAccessTokenShouldThrowExceptionWhenUserHasNoId() {
        Usuario usuario = new Usuario();
        usuario.setId(null);

        Throwable throwable = assertThrows(IllegalArgumentException.class,
                () -> tokenService.gerarAccessToken(usuario));

        assertEquals("Usuário inválido.", throwable.getMessage());
    }

    @Test
    @DisplayName("#gerarRefreshToken SHOULD generate refresh token WHEN called.")
    public void shouldGenerateRefreshTokenWhenCalled() {

        Instant hoje = LocalDateTime.of(2023, 5, 3, 22, 40, 5)
                .atZone(ZoneId.systemDefault())
                .toInstant();

        Instant dataExpiracao = hoje.plus(jwtProperty.getRefreshTokenValiditySeconds(), ChronoUnit.SECONDS);

        Mockito.when(clock.getZone())
                .thenReturn(ZoneId.systemDefault());
        Mockito.when(clock.instant())
                .thenReturn(hoje);

        Usuario usuario = new Usuario();
        usuario.setNome("test-name");
        usuario.setId(usuarioId);

        String myToken = gerarToken(usuario, hoje, dataExpiracao);

        String generatedToken = tokenService.gerarRefreshToken(usuario);

        assertEquals(myToken, generatedToken);
    }

    @Test
    @DisplayName("#gerarRefreshToken SHOULD throw exception WHEN param user is null.")
    public void gerarRefreshTokenShouldThrowExceptionWhenParamUserIsNull() {
        Throwable throwable = assertThrows(IllegalArgumentException.class,
                () -> tokenService.gerarRefreshToken(null));

        assertEquals("Usuário inválido.", throwable.getMessage());
    }

    @Test
    @DisplayName("#gerarRefreshToken SHOULD throw exception WHEN user has no ID.")
    public void gerarRefreshTokenShouldThrowExceptionWhenUserHasNoId() {
        Usuario usuario = new Usuario();
        usuario.setId(null);

        Throwable throwable = assertThrows(IllegalArgumentException.class,
                () -> tokenService.gerarRefreshToken(usuario));

        assertEquals("Usuário inválido.", throwable.getMessage());
    }

    @Test
    @DisplayName("#isTokenValido SHOULD return false WHEN token is null.")
    public void isTokenValidoShouldReturnFalseWhenTokenIsNull() {
        assertFalse(tokenService.isTokenValido(null));
    }

    @Test
    @DisplayName("#isTokenValido SHOULD return false WHEN token is empty.")
    public void isTokenValidoShouldReturnFalseWhenTokenEmpty() {
        assertFalse(tokenService.isTokenValido(""));
    }

    @Test
    @DisplayName("#isTokenValido SHOULD return false WHEN token is expired.")
    public void isTokenValidoShouldReturnFalseWhenTokenExpired() {
        Instant hoje = LocalDateTime.of(2023, 5, 6, 14, 45, 5)
                .atZone(ZoneId.systemDefault())
                .toInstant();

        Instant dataExpiracao = hoje.minus(jwtProperty.getRefreshTokenValiditySeconds(), ChronoUnit.SECONDS);

        Usuario usuario = new Usuario();
        usuario.setId(usuarioId);

        String myToken = gerarToken(usuario, hoje, dataExpiracao);

        assertFalse(tokenService.isTokenValido(myToken));
    }

    @Test
    @DisplayName("#isTokenValido SHOULD return true WHEN called with valid token and not expired.")
    public void isTokenValidoShouldReturnTrueWhenCalledWithValidTokenAndNotExpired() {
        Instant hoje = LocalDateTime.of(2023, 5, 6, 14, 45, 5)
                .atZone(ZoneId.systemDefault())
                .toInstant();

        Instant dataExpiracao = hoje.plus(jwtProperty.getRefreshTokenValiditySeconds(), ChronoUnit.SECONDS);

        Usuario usuario = new Usuario();
        usuario.setId(usuarioId);

        String myToken = gerarToken(usuario, hoje, dataExpiracao);

        assertTrue(tokenService.isTokenValido(myToken));
    }

    @Test
    @DisplayName("#isTokenInvalido SHOULD return true WHEN token is null.")
    public void isTokenInvalidoShouldReturnTrueWhenTokenIsNull() {
        assertTrue(tokenService.isTokenInvalido(null));
    }

    @Test
    @DisplayName("#isTokenInvalido SHOULD return true WHEN token is empty.")
    public void isTokenInvalidoShouldReturnTrueWhenTokenEmpty() {
        assertTrue(tokenService.isTokenInvalido(""));
    }

    @Test
    @DisplayName("#isTokenInvalido SHOULD return true WHEN token is expired.")
    public void isTokenInvalidoShouldReturnTrueWhenTokenExpired() {
        Instant hoje = LocalDateTime.of(2023, 5, 6, 14, 45, 5)
                .atZone(ZoneId.systemDefault())
                .toInstant();

        Instant dataExpiracao = hoje.minus(jwtProperty.getRefreshTokenValiditySeconds(), ChronoUnit.SECONDS);

        Usuario usuario = new Usuario();
        usuario.setId(usuarioId);

        String myToken = gerarToken(usuario, hoje, dataExpiracao);
        assertTrue(tokenService.isTokenInvalido(myToken));
    }

    @Test
    @DisplayName("#isTokenInvalido SHOULD return false WHEN called with valid token and not expired.")
    public void isTokenInvalidoShouldReturnFalseWhenCalledWithValidTokenAndNotExpired() {
        Instant hoje = LocalDateTime.of(2023, 5, 6, 14, 45, 5)
                .atZone(ZoneId.systemDefault())
                .toInstant();

        Instant dataExpiracao = hoje.plus(jwtProperty.getRefreshTokenValiditySeconds(), ChronoUnit.SECONDS);

        Usuario usuario = new Usuario();
        usuario.setId(usuarioId);

        String myToken = gerarToken(usuario, hoje, dataExpiracao);

        assertFalse(tokenService.isTokenInvalido(myToken));
    }

    @Test
    @DisplayName("#getIdUsuario SHOULD throw exception WHEN token is null.")
    public void getIdUsuarioShouldThrowExceptionWhenTokenIsNull() {
        Throwable throwable = assertThrows(IllegalArgumentException.class,
                () -> tokenService.getIdUsuario(null));
        assertEquals("O token fornecido é inválido.", throwable.getMessage());
    }

    @Test
    @DisplayName("#getIdUsuario SHOULD throw exception WHEN token is empty.")
    public void getIdUsuarioShouldThrowExceptionWhenTokenIsEmpty() {
        Throwable throwable = assertThrows(IllegalArgumentException.class,
                () -> tokenService.getIdUsuario(""));
        assertEquals("O token fornecido é inválido.", throwable.getMessage());
    }

    @Test
    @DisplayName("#getIdUsuario SHOULD throw exception WHEN token is expired.")
    public void getIdUsuarioShouldThrowExceptionWhenTokenExpired() {
        Instant hoje = LocalDateTime.of(2023, 5, 6, 14, 45, 5)
                .atZone(ZoneId.systemDefault())
                .toInstant();

        Instant dataExpiracao = hoje.minus(jwtProperty.getRefreshTokenValiditySeconds(), ChronoUnit.SECONDS);

        Usuario usuario = new Usuario();
        usuario.setId(usuarioId);

        String myToken = gerarToken(usuario, hoje, dataExpiracao);
        Throwable throwable = assertThrows(IllegalArgumentException.class,
                () -> tokenService.getIdUsuario(myToken));
        assertEquals("O token fornecido é inválido.", throwable.getMessage());
    }

    @Test
    @DisplayName("#getIdUsuario SHOULD return user ID WHEN called with valid token.")
    public void getIdUsuarioShouldReturnUserIDWhenCalledWithValidToken() {
        Instant hoje = LocalDateTime.of(2023, 5, 6, 14, 45, 5)
                .atZone(ZoneId.systemDefault())
                .toInstant();

        Instant dataExpiracao = hoje.plus(jwtProperty.getRefreshTokenValiditySeconds(), ChronoUnit.SECONDS);

        Usuario usuario = new Usuario();
        usuario.setId(usuarioId);

        String myToken = gerarToken(usuario, hoje, dataExpiracao);
        UUID idRetornado = tokenService.getIdUsuario(myToken);

        assertEquals(usuarioId, idRetornado);
    }
}
