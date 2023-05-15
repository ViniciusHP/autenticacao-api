package com.vhp.autenticacao.api.mapper;

import com.vhp.autenticacao.api.document.EmailDocument;
import com.vhp.autenticacao.api.mail.Email;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

class EmailMapperTest {

    private EmailMapper mapper;

    @BeforeEach
    public void beforeEach() {
        mapper = EmailMapper.INSTANCE;
    }

    @Test
    @DisplayName("#toEmail SHOULD convert EmailDocument to Email WHEN called")
    public void shouldConvertEmailDocumentoToEmailWhenCalled() {
        EmailDocument emailDocument = new EmailDocument();
        emailDocument.setId(new ObjectId());
        emailDocument.setCorpo("Email body");
        emailDocument.setRemetente("Email sender");
        emailDocument.setDestinatario("recipient@email.com");
        emailDocument.setEmailRemetente("sender@email.com");
        emailDocument.setAssunto("Email subject");
        emailDocument.setDataCriacao(null);
        emailDocument.setDataProcessamento(null);
        emailDocument.setStatus(null);
        emailDocument.setMensagemErro(null);

        Email email = mapper.toEmail(emailDocument);

        Assertions.assertEquals("Email body", email.getCorpo());
        Assertions.assertEquals("Email sender", email.getRemetente());
        Assertions.assertEquals("sender@email.com", email.getEmailRemetente());
        Assertions.assertEquals("Email subject", email.getAssunto());
        Assertions.assertEquals(List.of("recipient@email.com"), email.getDestinatarios());
    }
}
