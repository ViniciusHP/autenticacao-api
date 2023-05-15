package com.vhp.autenticacao.api.configurations.property;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Classe de propriedades de email da aplicação.
 */
@Getter
@Setter
@NoArgsConstructor
public class MailProperty {

   private String host;
   private int port;
   private String username;
   private String password;
   private String remetente;
}
