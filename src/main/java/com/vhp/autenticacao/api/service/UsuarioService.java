package com.vhp.autenticacao.api.service;

import com.vhp.autenticacao.api.configurations.property.AutenticacaoProperty;
import com.vhp.autenticacao.api.controller.form.NovoUsuarioForm;
import com.vhp.autenticacao.api.controller.form.RecuperarSenhaForm;
import com.vhp.autenticacao.api.controller.form.RedefinirSenhaForm;
import com.vhp.autenticacao.api.document.RecuperacaoSenha;
import com.vhp.autenticacao.api.document.Usuario;
import com.vhp.autenticacao.api.exceptions.EmailJaCadastradoException;
import com.vhp.autenticacao.api.exceptions.EmailNaoCadastradoException;
import com.vhp.autenticacao.api.exceptions.TokenRedefinicaoSenhaExpiradoException;
import com.vhp.autenticacao.api.exceptions.UsuarioInvalidoException;
import com.vhp.autenticacao.api.messages.MessageService;
import com.vhp.autenticacao.api.model.EnvioEmail;
import com.vhp.autenticacao.api.repository.UsuarioRepository;
import com.vhp.autenticacao.api.template.TemplateHelper;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Serviço responsável por se comunicar com o repositório de usuário.
 */
@Service
@NoArgsConstructor
public class UsuarioService {

    @Autowired
    private AutenticacaoProperty carteiraProperty;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RecuperacaoSenhaService recuperacaoSenhaService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private TemplateHelper templateService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MessageService messageService;

    public UsuarioService(AutenticacaoProperty carteiraProperty, UsuarioRepository usuarioRepository, RecuperacaoSenhaService recuperacaoSenhaService, EmailService emailService, TemplateHelper templateService, PasswordEncoder passwordEncoder, MessageService messageService) {
        this.carteiraProperty = carteiraProperty;
        this.usuarioRepository = usuarioRepository;
        this.recuperacaoSenhaService = recuperacaoSenhaService;
        this.emailService = emailService;
        this.templateService = templateService;
        this.passwordEncoder = passwordEncoder;
        this.messageService = messageService;
    }

    /**
     * Verifica se o email já foi cadastrado para algum usuário.
     *
     * @param email Email a ser verificado.
     * @return true se nenhum usuário está cadastrado com este email, caso contrário, false.
     */
    public boolean isEmailDisponivel(String email) {
        Optional<Usuario> usuarioOptional = usuarioRepository.findByEmail(email);
        return usuarioOptional.isEmpty();
    }

    /**
     * Registra novo usuário.
     *
     * @param usuarioForm - Informações para registro de novo usuário.
     */
    public void novoUsuario(NovoUsuarioForm usuarioForm) {
        if(!isEmailDisponivel(usuarioForm.getEmail())) {
            throw new EmailJaCadastradoException(usuarioForm.getEmail(), messageService);
        }
        usuarioForm.setPassword(passwordEncoder.encode(usuarioForm.getPassword()));
        usuarioRepository.save(usuarioForm.toUsuario());
    }

    /**
     * Busca usuário a partir de seu email.
     *
     * @param id Id do usuário.
     * @return {@link Optional<Usuario>} do usuário com o id informado.
     */
    public Optional<Usuario> find(UUID id){
        return usuarioRepository.findById(id);
    }

    /**
     * Busca usuário a partir de seu email.
     *
     * @param email Email do usuário.
     * @return Usuário com email informado.
     * @throws UsuarioInvalidoException Caso usuário com email informado não existe.
     */
    public Usuario findPorEmail(String email) throws UsuarioInvalidoException{
        Optional<Usuario> optionalUsuario = usuarioRepository.findByEmail(email);
        return optionalUsuario.orElseThrow(() -> new UsuarioInvalidoException(messageService));
    }

    /**
     *  Busca usuário a partir de seu id.
     *
     * @param id Id do usuário.
     * @return Usuário com id informado.
     * @throws UsuarioInvalidoException Caso usuário com id informado não existe.
     */
    public Usuario findPorId(UUID id) throws UsuarioInvalidoException{
        Optional<Usuario> optionalUsuario = find(id);
        return optionalUsuario.orElseThrow(() -> new UsuarioInvalidoException(messageService));
    }

    /**
     * Inicia o processo de recuperação de senha do usuário, criando registro de recuperação de senha e enviando email para redefinição de senha.
     *
     * @param recuperarSenhaForm Formulário de recuperação de senha.
     */
    public void recuperarSenha(RecuperarSenhaForm recuperarSenhaForm) {
        Optional<Usuario> usuarioOptional = usuarioRepository.findByEmail(recuperarSenhaForm.getEmail());

        Usuario usuario = usuarioOptional.orElseThrow(() -> new EmailNaoCadastradoException(recuperarSenhaForm.getEmail(), messageService));
        String token = UUID.randomUUID().toString();
        RecuperacaoSenha recuperacaoSenha = recuperacaoSenhaService.salvarRecuperacaoSenhaToken(usuario, token);

        String destinatario = usuario.getEmail();
        String assunto = this.messageService.getMessage("message.recuperacao-senha");
        String corpo = getConteudoPaginaRecuperacaoSenha(recuperacaoSenha);

        EnvioEmail envioEmail = new EnvioEmail();
        envioEmail.setAssunto(assunto);
        envioEmail.setDestinatario(destinatario);
        envioEmail.setCorpo(corpo);

        emailService.registrarEnvioEmail(envioEmail);
    }

    /**
     * Efetiva a redefinição de senha do usuário.
     *
     * @param redefinirSenhaForm Formulário de redefinição de senha.
     */
    public void redefinirSenha(RedefinirSenhaForm redefinirSenhaForm) {
        redefinirSenhaForm.setPassword(passwordEncoder.encode(redefinirSenhaForm.getPassword()));

        RecuperacaoSenha recuperacaoSenha = recuperacaoSenhaService.buscarRecuperacaoSenhaToken(redefinirSenhaForm.getToken());

        if(recuperacaoSenha == null) {
            throw new TokenRedefinicaoSenhaExpiradoException(messageService);
        }

        String novaSenha = redefinirSenhaForm.getPassword();

        Usuario usuario = recuperacaoSenha.getUsuario();
        usuario.setSenha(novaSenha);
        usuarioRepository.save(usuario);
    }

    /**
     * Gera corpo de email de recuperação de senha.
     *
     * @param recuperacaoSenha Registro de recuperação de senha.
     * @return Corpo de email de recuperação de senha.
     */
    private String getConteudoPaginaRecuperacaoSenha(RecuperacaoSenha recuperacaoSenha) {
        Map<String, Object> variaveis = new HashMap<>();
        variaveis.put("nomeUsuario", recuperacaoSenha.getUsuario().getNome());
        variaveis.put("token", recuperacaoSenha.getToken());
        variaveis.put("urlRecuperacao", carteiraProperty.getRecuperacaoSenha().getUrl());
        return this.templateService.getConteudoTemplate("mail/recuperar-senha", variaveis);
    }
}
