package com.vhp.autenticacao.api.exceptionshandler;

import com.vhp.autenticacao.api.exceptions.ApplicationAbstractException;
import com.vhp.autenticacao.api.exceptions.ApplicationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class ApplicationExceptionHandler {

    @ExceptionHandler({ApplicationAbstractException.class})
    public ResponseEntity<List<ApplicationExceptionMessage>> handleException(ApplicationException ex) {
        return ResponseEntity
                .status(ex.getHttpStatusCode())
                .body(ex.getMensagens());
    }
}
