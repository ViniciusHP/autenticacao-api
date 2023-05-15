package com.vhp.autenticacao.api.configurations.security;

import com.vhp.autenticacao.api.service.UsuarioService;
import com.vhp.autenticacao.api.document.Usuario;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AutenticacaoServiceTest {

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private AutenticacaoService autenticacaoService;

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
    @DisplayName("#loadUserByUsername SHOULD return user WHEN called with valid username.")
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

        Mockito.when(usuarioService.findPorEmail("test@email.com"))
                .thenReturn(mockedUsuario);

        Usuario usuarioRetornado = (Usuario) autenticacaoService.loadUserByUsername("test@email.com");

        assertEquals(uuid, usuarioRetornado.getId());
        assertEquals("Test name", usuarioRetornado.getNome());
        assertEquals("test@email.com", usuarioRetornado.getEmail());
        assertEquals("p4$$0rd", usuarioRetornado.getPassword());
        assertTrue(usuarioRetornado.isAtivo());
        assertEquals(now, usuarioRetornado.getDataCriacao());
    }

    @Test
    @DisplayName("#loadUserById SHOULD return user WHEN called valid UUID.")
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

        Mockito.when(usuarioService.findPorId(uuid))
                .thenReturn(mockedUsuario);

        Usuario usuarioRetornado = (Usuario) autenticacaoService.loadUserById(uuid);

        assertEquals(uuid, usuarioRetornado.getId());
        assertEquals("Test name", usuarioRetornado.getNome());
        assertEquals("test@email.com", usuarioRetornado.getEmail());
        assertEquals("p4$$0rd", usuarioRetornado.getPassword());
        assertTrue(usuarioRetornado.isAtivo());
        assertEquals(now, usuarioRetornado.getDataCriacao());
    }
}
