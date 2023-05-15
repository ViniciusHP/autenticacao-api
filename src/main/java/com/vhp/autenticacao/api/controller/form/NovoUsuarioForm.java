package com.vhp.autenticacao.api.controller.form;

import com.vhp.autenticacao.api.document.Usuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class NovoUsuarioForm {

    @NotNull
    @NotBlank
    private String name;

    @NotNull
    @NotBlank
    @Email
    private String email;

    @NotNull
    @NotBlank
    private String password;

    public void setPassword(String password) {
        this.password = password;
    }

    public Usuario toUsuario() {
        Usuario u = new Usuario();
        u.setEmail(email);
        u.setSenha(password);
        u.setNome(name);

        return u;
    }
}
