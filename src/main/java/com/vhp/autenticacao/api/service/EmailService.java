package com.vhp.autenticacao.api.service;

import com.vhp.autenticacao.api.configurations.property.AutenticacaoProperty;
import com.vhp.autenticacao.api.model.EnvioEmail;
import com.vhp.autenticacao.api.document.EmailDocument;
import com.vhp.autenticacao.api.document.EmailStatus;
import com.vhp.autenticacao.api.repository.EmailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Serviço responsável por buscar/salvar emails no repositório.
 */
@Service
public class EmailService {

    private final EmailRepository emailRepository;
    private final AutenticacaoProperty carteiraProperty;

    private final Clock clock;

    @Autowired
    public EmailService(EmailRepository emailRepository, AutenticacaoProperty carteiraProperty, Clock clock) {
        this.emailRepository = emailRepository;
        this.carteiraProperty = carteiraProperty;
        this.clock = clock;
    }

    /**
     * Busca lista de emails não processados no repositório.
     * @return @{@link List<EmailDocument>} de emails não processados.
     */
    public List<EmailDocument> buscarEmaisNaoProcessados() {
        return this.buscarEmaisNaoProcessados(0, 10);
    }

    /**
     * Busca lista de emails não processados no repositório.
     * @param skip Quantidade de registros a serem ignorados.
     * @param limit Quantidade de registros a serem buscados.
     * @return {@link List<EmailDocument>} de emails não processados.
     */
    public List<EmailDocument> buscarEmaisNaoProcessados(int skip, int limit) {
        return emailRepository.findByStatus(EmailStatus.NAO_PROCESSADO, skip, limit);
    }

    /**
     * Salva a lista de emails fornecida no repositório.
     * @param emailDocuments Lista de emails a serem salvos.
     * @return {@link List<EmailDocument>} de emails salvos.
     */
    public List<EmailDocument> salvarEmails(List<EmailDocument> emailDocuments) {
        if(emailDocuments == null || emailDocuments.isEmpty()) {
            throw new IllegalArgumentException("Não foi possível salvar emails pois eles estão vazios.");
        }

        return emailRepository.saveAll(emailDocuments);
    }

    /**
     * Salva no repositório o email enviado.
     * @param envioEmail Email enviado.
     */
    public void registrarEnvioEmail(EnvioEmail envioEmail) {
        validateEnvioEmail(envioEmail);

        EmailDocument emailDocument = createNewEmail();
        emailDocument.setAssunto(envioEmail.getAssunto());
        emailDocument.setDestinatarios(envioEmail.getDestinatarios());
        emailDocument.setCorpo(envioEmail.getCorpo());

        emailRepository.save(emailDocument);
    }

    /**
     * Cria um email já pré-configurado.
     * @return Email pré-configurado.
     */
    private EmailDocument createNewEmail() {
        EmailDocument emailDocument = new EmailDocument();
        emailDocument.setEmailRemetente(carteiraProperty.getMail().getUsername());
        emailDocument.setRemetente(carteiraProperty.getMail().getRemetente());
        emailDocument.setDataCriacao(LocalDateTime.now(clock));
        emailDocument.setStatus(EmailStatus.NAO_PROCESSADO);
        return emailDocument;
    }

    /**
     * Valida informações do email enviado.
     * @param envioEmail Email enviado.
     * @throws IllegalArgumentException
     */
    private void validateEnvioEmail(EnvioEmail envioEmail) throws IllegalArgumentException{
        if(envioEmail == null)
            throw new IllegalArgumentException("Não é possivel registrar email para envio de objeto nulo.");

        if(envioEmail.getAssunto() == null || envioEmail.getAssunto().isBlank())
            throw  new IllegalArgumentException("Assunto de email não pode ser vazio para registrar email para envio.");

        if(envioEmail.getCorpo() == null || envioEmail.getCorpo().isBlank())
            throw  new IllegalArgumentException("Corpo do email não pode ser vazio para registrar email para envio.");

        if(envioEmail.getDestinatarios() == null || envioEmail.getDestinatarios().isEmpty())
            throw new IllegalArgumentException("Os destinatários devem ser informados para registrar email para envio.");

        if(envioEmail.getDestinatarios().stream().anyMatch(Objects::isNull)
                || envioEmail.getDestinatarios().stream().anyMatch(String::isBlank))
            throw new IllegalArgumentException("Os conteúdo de destinatários não podem estar vazios ou nulos para registrar email para envio.");
    }
}
