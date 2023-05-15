package com.vhp.autenticacao.api.configurations.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

class ExtracaoRefreshTokenFilterTest {

    @InjectMocks
    private ExtracaoRefreshTokenFilter extracaoRefreshTokenFilter;

    private MockHttpServletRequest request;

    private MockHttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private AutoCloseable autoCloseable;

    @BeforeEach
    public void beforeEach() {
        autoCloseable = MockitoAnnotations.openMocks(this);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @AfterEach
    public void afterEach() throws Exception {
        autoCloseable.close();
    }

    @Test
    @DisplayName("#doFilterInternal SHOULD do nothing WHEN has no refresh token.")
    public void shouldDoNothingWhenHasNoRefreshToken() throws ServletException, IOException {
        request.setRequestURI("/oauth/refresh-token");
        request.setCookies(new Cookie("other-cookie", "other-value"));

        extracaoRefreshTokenFilter.doFilterInternal(request, response, filterChain);

        Assertions.assertNull(request.getAttribute("refreshToken"));
    }

    @Test
    @DisplayName("#doFilterInternal SHOULD extract refresh token from cookies WHEN present.")
    public void shouldExtractRefreshTokenFromCookiesWhenPresent() throws ServletException, IOException {
        request.setRequestURI("/oauth/refresh-token");
        request.setCookies(new Cookie("refreshToken", "test-refresh-token"));

        extracaoRefreshTokenFilter.doFilterInternal(request, response, filterChain);

        Assertions.assertEquals("test-refresh-token", request.getAttribute("refreshToken"));
    }
}
