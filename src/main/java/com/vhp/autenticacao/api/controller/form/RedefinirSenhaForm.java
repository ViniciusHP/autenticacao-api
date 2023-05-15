package com.vhp.autenticacao.api.controller.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RedefinirSenhaForm {

    @NotNull
    @NotBlank
    private String password;

    @NotNull
    @NotBlank
    private String token;
}
