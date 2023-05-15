package com.vhp.autenticacao.api.exceptions;

import com.vhp.autenticacao.api.exceptionshandler.ApplicationExceptionMessage;
import org.springframework.http.HttpStatus;

import java.util.List;

public interface ApplicationException {
    List<ApplicationExceptionMessage> getMensagens();
    HttpStatus getHttpStatusCode();
}
