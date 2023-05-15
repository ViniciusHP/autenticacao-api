package com.vhp.autenticacao.api.controller.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RecuperarSenhaForm {

    @NotNull
    @NotBlank
    @Email
    private String email;
}
