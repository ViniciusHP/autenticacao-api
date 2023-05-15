package com.vhp.autenticacao.api.document.listeners;

import com.vhp.autenticacao.api.document.Usuario;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UsuarioDocumentEventListenerTest {

    @InjectMocks
    private UsuarioDocumentEventListener usuarioDocumentEventListener;

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
    @DisplayName("#onBeforeConvert SHOULD generate UUID WHEN UUID is null.")
    public void shouldGenerateUUIDWhenUUIDIsNull() {
        Usuario mockedUsuario = new Usuario();
        mockedUsuario.setId(null);
        mockedUsuario.setNome("Test Name");
        mockedUsuario.setSenha("p4$$0rd");

        BeforeConvertEvent<Usuario> beforeConvertEvent = new BeforeConvertEvent<>(mockedUsuario, "test-collection");

        usuarioDocumentEventListener.onBeforeConvert(beforeConvertEvent);
        assertNotNull(mockedUsuario.getId());
    }

    @Test
    @DisplayName("#onBeforeConvert SHOULD do nothing WHEN UUID is present.")
    public void shouldDoNothingWhenUUIDIsPresent() {
        Usuario mockedUsuario = new Usuario();
        mockedUsuario.setId(UUID.fromString("e621e1f8-c36c-495a-93fc-0c247a3e6e5f"));
        mockedUsuario.setNome("Test Name");
        mockedUsuario.setSenha("p4$$0rd");

        BeforeConvertEvent<Usuario> beforeConvertEvent = new BeforeConvertEvent<>(mockedUsuario, "test-collection");

        usuarioDocumentEventListener.onBeforeConvert(beforeConvertEvent);
        assertEquals(UUID.fromString("e621e1f8-c36c-495a-93fc-0c247a3e6e5f"), mockedUsuario.getId());
    }
}
