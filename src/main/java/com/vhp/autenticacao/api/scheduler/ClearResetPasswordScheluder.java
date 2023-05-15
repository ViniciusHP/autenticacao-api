package com.vhp.autenticacao.api.scheduler;

import com.vhp.autenticacao.api.document.RecuperacaoSenha;
import com.vhp.autenticacao.api.service.RecuperacaoSenhaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Classe responsável pela remoção das solicitações de recuperação de senhas.
 */
@Component
public class ClearResetPasswordScheluder {

    private final RecuperacaoSenhaService recuperacaoSenhaService;

    private final Clock clock;

    private final Logger logger = LoggerFactory.getLogger(ClearResetPasswordScheluder.class);

    @Autowired
    public ClearResetPasswordScheluder(RecuperacaoSenhaService recuperacaoSenhaService, Clock clock) {
        this.recuperacaoSenhaService = recuperacaoSenhaService;
        this.clock = clock;
    }

    /**
     * Remove as solicitações de recuperação de senhas já expiradas.
     */
    @Scheduled(fixedDelay = 3_600_000L)
    public void clearResetPassword() {

        logger.info("Initializing reset password clear process.");

        List<RecuperacaoSenha> resetPasswordExpired = recuperacaoSenhaService.findRecuperacaoSenhaDataMenorQue(LocalDateTime.now(clock), 0, 500);
        if(resetPasswordExpired != null && !resetPasswordExpired.isEmpty()) {
            recuperacaoSenhaService.deleteAll(resetPasswordExpired);
        }

        logger.info("Finishing reset password clear process.");
    }
}
