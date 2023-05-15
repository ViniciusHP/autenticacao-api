package com.vhp.autenticacao.api.configurations.security;

import com.vhp.autenticacao.api.document.Usuario;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AutenticacaoViaTokenFilterTest {

    @Mock
    private TokenService tokenService;

    @Mock
    private AutenticacaoService autenticacaoService;

    @InjectMocks
    private AutenticacaoViaTokenFilter autenticacaoViaTokenFilter;

    private AutoCloseable autoCloseable;

    @Mock
    private FilterChain mockFilterChain;

    private MockHttpServletRequest mockRequest;

    private MockHttpServletResponse mockResponse;

    @BeforeEach
    public void beforeEach() {
        autoCloseable = MockitoAnnotations.openMocks(this);
        //mockFilterChain = Mockito.mock(FilterChain.class);
        mockRequest = new MockHttpServletRequest();
        mockResponse = new MockHttpServletResponse();
    }

    @AfterEach
    public void afterEach() throws Exception {
        autoCloseable.close();
    }

    @Test
    @DisplayName("#doFilterInternal SHOULD do nothing WHEN called with invalid header token.")
    public void doFilterInternalShouldDoNothingWhenCalledWithInvalidHeaderToken() throws ServletException, IOException {

        mockRequest.addHeader(HttpHeaders.AUTHORIZATION, "");

        Mockito.when(tokenService.isTokenValido(""))
                .thenReturn(false);

        autenticacaoViaTokenFilter.doFilterInternal(mockRequest, mockResponse, mockFilterChain);

        Mockito.verify(tokenService, Mockito.never())
                .getIdUsuario("");
        Mockito.verifyNoInteractions(autenticacaoService);

        assertEquals("", "");
    }

    @Test
    @DisplayName("#doFilterInternal SHOULD authenticate user WHEN called valid header token.")
    public void doFilterInternalShouldAuthenticateUserWhenCalledWithValidHeaderToken() throws ServletException, IOException {

        mockRequest.addHeader(HttpHeaders.AUTHORIZATION, "Bearer my-valid-token");
        UUID uuid = UUID.fromString("e621e1f8-c36c-495a-93fc-0c247a3e6e5f");
        LocalDateTime now = LocalDateTime.now();

        Usuario mockedUsuario = new Usuario();
        mockedUsuario.setId(uuid);
        mockedUsuario.setNome("Test name");
        mockedUsuario.setEmail("test@email.com");
        mockedUsuario.setSenha("p4$$w0rd");
        mockedUsuario.setDataCriacao(now);
        mockedUsuario.setAtivo(true);

        Mockito.when(tokenService.isTokenValido("my-valid-token"))
                .thenReturn(true);
        Mockito.when(tokenService.getIdUsuario("my-valid-token"))
                .thenReturn(uuid);

        Mockito.when(autenticacaoService.loadUserById(uuid))
                .thenReturn(mockedUsuario);

        autenticacaoViaTokenFilter.doFilterInternal(mockRequest, mockResponse, mockFilterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Usuario usuarioAutenticado = (Usuario) authentication.getPrincipal();

        assertEquals(uuid, usuarioAutenticado.getId());
        assertEquals("Test name", usuarioAutenticado.getNome());
        assertEquals("test@email.com", usuarioAutenticado.getEmail());
        assertEquals("p4$$w0rd", usuarioAutenticado.getSenha());
        assertEquals(now, usuarioAutenticado.getDataCriacao());
        assertTrue(usuarioAutenticado.isAtivo());
    }
}
