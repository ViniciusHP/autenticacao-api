package com.vhp.autenticacao.api.exceptionshandler;

import com.vhp.autenticacao.api.exceptions.UsuarioNaoAutenticadoException;
import com.vhp.autenticacao.api.messages.MessageService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ApplicationExceptionHandlerTest {

    @Mock
    private MessageService messageService;

    @InjectMocks
    private ApplicationExceptionHandler applicationExceptionHandler;

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
    @DisplayName("#handleException SHOULD transform exception into error message WHEN called.")
    public void shouldTransformExceptionIntoErrorMessageWhenCalled() {

        Mockito.when(messageService.getMessage("error.usuario-nao-autenticado"))
                .thenReturn("error.usuario-nao-autenticado");

        UsuarioNaoAutenticadoException exception = new UsuarioNaoAutenticadoException(messageService);

        ResponseEntity<List<ApplicationExceptionMessage>> responseEntity = applicationExceptionHandler.handleException(exception);

        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
        assertEquals("error.usuario-nao-autenticado", responseEntity.getBody().get(0).getMessage());
    }

}
