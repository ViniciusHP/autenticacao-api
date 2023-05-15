package com.vhp.autenticacao.api.scheduler;

import com.vhp.autenticacao.api.document.RecuperacaoSenha;
import com.vhp.autenticacao.api.document.Usuario;
import com.vhp.autenticacao.api.service.RecuperacaoSenhaService;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ClearResetPasswordScheluderTest {

    @Mock
    private RecuperacaoSenhaService recuperacaoSenhaService;

    @Mock
    private Clock clock;

    @InjectMocks
    private ClearResetPasswordScheluder clearResetPasswordScheluder;

    @Captor
    private ArgumentCaptor<List<RecuperacaoSenha>> recuperacaoSenhaListCaptor;

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
    @DisplayName("#clearResetPassword SHOULD delete all expired password recovery records WHEN called.")
    public void shouldDeleteAllExpiredPasswordRecoveryRecordsWhenCalled() {

        RecuperacaoSenha rs1 = new RecuperacaoSenha();
        rs1.setId(new ObjectId());
        rs1.setToken("f4k3-t0k3n");
        rs1.setDataExpiracao(LocalDateTime.of(2023, 4, 26, 20, 50, 9));
        rs1.setUsuario(new Usuario());

        RecuperacaoSenha rs2 = new RecuperacaoSenha();
        rs2.setId(new ObjectId());
        rs2.setToken("t0k3n-f4k3");
        rs2.setDataExpiracao(LocalDateTime.of(2022, 4, 26, 20, 50, 9));
        rs2.setUsuario(new Usuario());

        List<RecuperacaoSenha> expiredPasswordRecoveryMock = Arrays.asList(rs1, rs2);

        LocalDateTime now = LocalDateTime.of(2023, 4, 26, 20, 53, 49);
        Instant instant = now.atZone(ZoneId.systemDefault()).toInstant();

        when(clock.getZone())
            .thenReturn(ZoneId.systemDefault());
        when(clock.instant())
            .thenReturn(instant);
        when(recuperacaoSenhaService.findRecuperacaoSenhaDataMenorQue(now, 0, 500))
            .thenReturn(expiredPasswordRecoveryMock);

        clearResetPasswordScheluder.clearResetPassword();

        verify(recuperacaoSenhaService)
            .deleteAll(recuperacaoSenhaListCaptor.capture());

        List<RecuperacaoSenha> deletedList = recuperacaoSenhaListCaptor.getValue();

        assertEquals(expiredPasswordRecoveryMock,deletedList);
    }

    @Test
    @DisplayName("#clearResetPassword SHOULD do nothing WHEN there is no record to delete")
    public void shouldDoNothingWhenThereIsNoRecordToDelete() {
        List<RecuperacaoSenha> mockList = new ArrayList<>();
        LocalDateTime now = LocalDateTime.of(2023, 4, 26, 20, 53, 49);
        Instant instant = now.atZone(ZoneId.systemDefault()).toInstant();

        when(clock.getZone())
                .thenReturn(ZoneId.systemDefault());
        when(clock.instant())
                .thenReturn(instant);
        when(recuperacaoSenhaService.findRecuperacaoSenhaDataMenorQue(now, 0, 500))
                .thenReturn(mockList);

        clearResetPasswordScheluder.clearResetPassword();

        verify(recuperacaoSenhaService, never())
                .deleteAll(mockList);
    }
}
