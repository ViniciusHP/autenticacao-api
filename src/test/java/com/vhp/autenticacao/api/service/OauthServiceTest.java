package com.vhp.autenticacao.api.service;

import com.vhp.autenticacao.api.configurations.property.AutenticacaoProperty;
import com.vhp.autenticacao.api.configurations.property.JwtProperty;
import com.vhp.autenticacao.api.configurations.security.AutenticacaoService;
import com.vhp.autenticacao.api.configurations.security.TokenService;
import com.vhp.autenticacao.api.controller.dto.TokenDTO;
import com.vhp.autenticacao.api.controller.form.LoginForm;
import com.vhp.autenticacao.api.exceptions.UsuarioNaoAutenticadoException;
import com.vhp.autenticacao.api.document.Usuario;
import com.vhp.autenticacao.api.messages.MessageService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.UUID;

class OauthServiceTest {

    @Mock
    private AutenticacaoProperty carteiraProperty;

    @Mock
    private AuthenticationManager authManager;

    @Mock
    private TokenService tokenService;

    @Mock
    private AutenticacaoService autenticacaoService;

    @Mock
    private MessageService messageService;

    @InjectMocks
    private OauthService oauthService;

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
    @DisplayName("#logarUsuario SHOULD authenticate user AND return token and refresh token WHEN called")
    public void shouldAuthenticateUserAndReturnTokenAndRefreshTokenWhenCalled() {

        LoginForm form = new LoginForm();
        form.setEmail("teste@email.com");
        form.setPassword("p4$$w0rd");

        Authentication authenticationMock = Mockito.mock(Authentication.class);

        Usuario usuarioMock = new Usuario();
        usuarioMock.setNome("Teste");
        usuarioMock.setSenha("p4$$0rd");

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setContextPath("/test-context");

        MockHttpServletResponse res = new MockHttpServletResponse();

        JwtProperty jwtProperty = new JwtProperty();
        jwtProperty.setSecure(true);
        jwtProperty.setRefreshTokenValiditySeconds(20000);

        Mockito.when(carteiraProperty.getJwt())
                        .thenReturn(jwtProperty);

        Mockito.when(authManager.authenticate(new UsernamePasswordAuthenticationToken("teste@email.com", "p4$$w0rd")))
                .thenReturn(authenticationMock);

        Mockito.when(authenticationMock.getPrincipal())
                .thenReturn(usuarioMock);

        Mockito.when(tokenService.gerarAccessToken(usuarioMock))
                        .thenReturn("F4k3-T0k3n");

        Mockito.when(tokenService.gerarRefreshToken(usuarioMock))
                        .thenReturn("F4k3-R3fr3sh-T0k3n");


        TokenDTO tokenDTO = oauthService.logarUsuario(form, req, res);
        Cookie cookie = res.getCookie("refreshToken");

        Assertions.assertEquals("Bearer", tokenDTO.getTipo());
        Assertions.assertEquals("F4k3-T0k3n", tokenDTO.getToken());
        Assertions.assertEquals("F4k3-R3fr3sh-T0k3n", cookie.getValue());
        Assertions.assertEquals("/test-context/oauth/refresh-token", cookie.getPath());
        Assertions.assertTrue(cookie.getSecure());
        Assertions.assertEquals(20000, cookie.getMaxAge());
    }

    @Test
    @DisplayName("#removerRefreshToken SHOULD remove refresh token WHEN called")
    public void shouldRemoveRefreshTokenWhenCalled() {

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setContextPath("/test-context");

        MockHttpServletResponse res = new MockHttpServletResponse();

        oauthService.removerRefreshToken(req, res);
        Cookie cookie = res.getCookie("refreshToken");

        Assertions.assertTrue(cookie.getValue().isEmpty());
        Assertions.assertEquals("/test-context/oauth/refresh-token", cookie.getPath());
        Assertions.assertEquals(0, cookie.getMaxAge());
    }

    @Test
    @DisplayName("#renovarLogin SHOULD throw exception WHEN called with invalid refresh token")
    public void shouldThrowExceptionWhenCalledWithInvalidRefreshToken() {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setAttribute("refreshToken", "");
        MockHttpServletResponse res = new MockHttpServletResponse();

        Mockito.when(tokenService.isTokenInvalido(""))
                .thenReturn(true);

        Mockito.when(messageService.getMessage(Mockito.eq("error.usuario-nao-autenticado"), Mockito.any()))
                        .thenReturn("error.usuario-nao-autenticado");

        UsuarioNaoAutenticadoException exception = Assertions.assertThrows(UsuarioNaoAutenticadoException.class, () -> oauthService.renovarLogin(req, res));

        Assertions.assertEquals("error.usuario-nao-autenticado", exception.getMensagens().get(0).getMessage());
    }

    @Test
    @DisplayName("#renovarLogin SHOULD refresh authentication AND return token and refresh token WHEN called with valid refresh token")
    public void shouldRefreshAuthenticationAndReturnTokenAndRefreshTokenWhenCalledWithValidRefreshToken() {

        Authentication authenticationMock = Mockito.mock(Authentication.class);

        UUID userId = UUID.fromString("e621e1f8-c36c-495a-93fc-0c247a3e6e5f");
        Usuario usuarioMock = new Usuario();
        usuarioMock.setNome("Teste");
        usuarioMock.setSenha("p4$$0rd");
        usuarioMock.setId(userId);

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setContextPath("/test-context");
        req.setAttribute("refreshToken", "F4k3-R3fr3sh-T0k3n");

        MockHttpServletResponse res = new MockHttpServletResponse();

        JwtProperty jwtProperty = new JwtProperty();
        jwtProperty.setSecure(true);
        jwtProperty.setRefreshTokenValiditySeconds(20000);

        Mockito.when(tokenService.getIdUsuario("F4k3-R3fr3sh-T0k3n"))
                        .thenReturn(userId);

        Mockito.when(autenticacaoService.loadUserById(userId))
                .thenReturn(usuarioMock);

        Mockito.when(carteiraProperty.getJwt())
                .thenReturn(jwtProperty);

        Mockito.when(tokenService.gerarAccessToken(usuarioMock))
                .thenReturn("N3W-F4k3-T0k3n");

        Mockito.when(tokenService.gerarRefreshToken(usuarioMock))
                .thenReturn("N3W-F4k3-R3fr3sh-T0k3n");


        TokenDTO tokenDTO = oauthService.renovarLogin(req, res);
        Cookie cookie = res.getCookie("refreshToken");

        Assertions.assertEquals("Bearer", tokenDTO.getTipo());
        Assertions.assertEquals("N3W-F4k3-T0k3n", tokenDTO.getToken());
        Assertions.assertEquals("N3W-F4k3-R3fr3sh-T0k3n", cookie.getValue());
        Assertions.assertEquals("/test-context/oauth/refresh-token", cookie.getPath());
        Assertions.assertTrue(cookie.getSecure());
        Assertions.assertEquals(20000, cookie.getMaxAge());
    }
}
