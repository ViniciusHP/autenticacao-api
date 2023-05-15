package com.vhp.autenticacao.api.messages;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.MessageSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class MessageServiceTest {

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private MessageService messageService;

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
    @DisplayName("#getMessage SHOULD throw exception WHEN called with null message code.")
    public void shouldThrowExceptionWhenCalledWithNullMessageCode() {
        Throwable throwable = assertThrows(IllegalArgumentException.class,
                () -> messageService.getMessage(null));
        assertEquals("Um código de mensagem de erro válido deve ser informado.", throwable.getMessage());
    }

    @Test
    @DisplayName("#getMessage SHOULD throw exception WHEN called with empty message code.")
    public void shouldThrowExceptionWhenCalledWithEmptyMessageCode() {
        Throwable throwable = assertThrows(IllegalArgumentException.class,
                () -> messageService.getMessage(""));
        assertEquals("Um código de mensagem de erro válido deve ser informado.", throwable.getMessage());
    }

    @Test
    @DisplayName("#getMessage SHOULD return message WHEN called.")
    public void shouldReturnMessageWhenCalled() {
        when(messageSource.getMessage(eq("test-code-message"), any(), any()))
                .thenReturn("My test message.");

        String message = messageService.getMessage("test-code-message");
        assertEquals("My test message.", message);
    }
}
