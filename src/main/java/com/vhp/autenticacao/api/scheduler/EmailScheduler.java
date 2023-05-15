package com.vhp.autenticacao.api.scheduler;

import com.vhp.autenticacao.api.document.EmailDocument;
import com.vhp.autenticacao.api.document.EmailStatus;
import com.vhp.autenticacao.api.mail.MailSender;
import com.vhp.autenticacao.api.mapper.EmailMapper;
import com.vhp.autenticacao.api.service.EmailService;
import jakarta.mail.MessagingException;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Classe responsável por organizar o envio de emails
 */
@Component
public class EmailScheduler {

    private EmailService emailService;

    private MailSender mailSender;

    private EmailMapper emailMapper;

    private Clock clock;

    private final Logger logger = LoggerFactory.getLogger(EmailScheduler.class);

    @Autowired
    public EmailScheduler(EmailService emailService, MailSender mailSender, EmailMapper emailMapper, Clock clock) {
        this.emailService = emailService;
        this.mailSender = mailSender;
        this.emailMapper = emailMapper;
        this.clock = clock;
    }

    /**
     * Busca lista de emails não processados e os envia.
     */
    @Scheduled(fixedDelay = 3_600_000L)
    public void envioEmail() {

        logger.info("Iniciado processo de envio de emails.");

        List<EmailDocument> emailsNaoProcessados = emailService.buscarEmaisNaoProcessados();

        if(emailsNaoProcessados != null && !emailsNaoProcessados.isEmpty()) {
            logger.info("Número de emails a serem enviados: {}", emailsNaoProcessados.size());
            processarEmails(emailsNaoProcessados);
            atualizarEstadosEmails(emailsNaoProcessados);
        } else {
            logger.info("Nenhum email para ser enviado.");
        }

        logger.info("Finalizado processo de envio de emails.");
    }

    /**
     * Processa cada email não processado.
     * @param emailsNaoProcessados Lista de emails não processados.
     */
    private void processarEmails(List<EmailDocument> emailsNaoProcessados) {
        emailsNaoProcessados.forEach(e -> this.processarEmail(e));
    }

    /**
     * Envia email informado.
     * @param emailDocumentNaoProcessado Email ainda não enviado.
     */
    private void processarEmail(EmailDocument emailDocumentNaoProcessado) {
        try {
            mailSender.enviarEmail(emailMapper.toEmail(emailDocumentNaoProcessado));
            emailDocumentNaoProcessado.setStatus(EmailStatus.PROCESSADO);
            emailDocumentNaoProcessado.setDataProcessamento(LocalDateTime.now(clock));
        } catch (MessagingException | UnsupportedEncodingException e) {
            emailDocumentNaoProcessado.setStatus(EmailStatus.ERRO);
            emailDocumentNaoProcessado.setMensagemErro(ExceptionUtils.getStackTrace(e));
            emailDocumentNaoProcessado.setDataProcessamento(LocalDateTime.now(clock));
        }
    }

    /**
     * Atualiza estado dos emails processados ou com erros.
     * @param emailsProcessados Emails processados ou com erros.
     */
    private void atualizarEstadosEmails(List<EmailDocument> emailsProcessados) {
        this.emailService.salvarEmails(emailsProcessados);
    }
}
