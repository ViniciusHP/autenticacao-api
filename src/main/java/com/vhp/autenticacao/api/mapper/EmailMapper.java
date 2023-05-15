package com.vhp.autenticacao.api.mapper;


import com.vhp.autenticacao.api.mail.Email;
import com.vhp.autenticacao.api.document.EmailDocument;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface EmailMapper {
    EmailMapper INSTANCE = Mappers.getMapper(EmailMapper.class);

    Email toEmail(EmailDocument emailDocument);
}
