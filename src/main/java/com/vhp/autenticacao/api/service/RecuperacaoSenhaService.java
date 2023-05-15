package com.vhp.autenticacao.api.service;

import com.vhp.autenticacao.api.configurations.property.AutenticacaoProperty;
import com.vhp.autenticacao.api.document.RecuperacaoSenha;
import com.vhp.autenticacao.api.document.Usuario;
import com.vhp.autenticacao.api.exceptions.TokenRedefinicaoSenhaExpiradoException;
import com.vhp.autenticacao.api.repository.RecuperacaoSenhaRepository;
import com.vhp.autenticacao.api.messages.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Serviço responsável por se comunicar com o repositório das solicitações de recuperação de senha de usuário.
 */
@Service
public class RecuperacaoSenhaService {

    private final AutenticacaoProperty carteiraProperty;
    private final RecuperacaoSenhaRepository recuperacaoSenhaRepository;
    private final MessageService messageService;

    private final Clock clock;

    @Autowired
    public RecuperacaoSenhaService(AutenticacaoProperty carteiraProperty, RecuperacaoSenhaRepository recuperacaoSenhaRepository, MessageService messageService, Clock clock) {
        this.carteiraProperty = carteiraProperty;
        this.recuperacaoSenhaRepository = recuperacaoSenhaRepository;
        this.messageService = messageService;
        this.clock = clock;
    }

    /**
     * Obtém registro de recuperação de senha a partir de token.
     *
     * @param token Token gerado para recuperação de senha.
     * @return Token de recuperação de senha.
     */
    public RecuperacaoSenha buscarRecuperacaoSenhaToken(String token) {
        if(token == null || token.isBlank()) {
            throw new TokenRedefinicaoSenhaExpiradoException(messageService);
        }

        RecuperacaoSenha tokenSalvo = recuperacaoSenhaRepository.findByToken(token)
                .orElseThrow(() -> new TokenRedefinicaoSenhaExpiradoException(messageService));

        if(!isTokenValido(tokenSalvo)) {
            throw new TokenRedefinicaoSenhaExpiradoException(messageService);
        }

        return tokenSalvo;
    }

    /**
     * Busca registros de recuperação de senha para datas menores que a informada.
     *
     * @param data Data que será usada para buscar registros anteriores a ela.
     * @param skip Número de registros a serem ignorados.
     * @param limit Número máximo de registros buscados.
     * @return {@link List<RecuperacaoSenha>} de recuperação de senha.
     */
    public List<RecuperacaoSenha> findRecuperacaoSenhaDataMenorQue(LocalDateTime data, int skip, int limit) {
        if(data == null) {
            data = LocalDateTime.now(clock);
        }

        return recuperacaoSenhaRepository.findByDataExpiracaoLesserThan(data, skip, limit);
    }

    /**
     * Remove todos os registros informados do repositório.
     *
     * @param registrosRecuperacao Registros a serem removidos.
     */
    public void deleteAll(List<RecuperacaoSenha> registrosRecuperacao) {
        if(registrosRecuperacao == null || registrosRecuperacao.isEmpty()) {
            return;
        }

        recuperacaoSenhaRepository.deleteAll(registrosRecuperacao);
    }

    /**
     * Cria um novo registro de recuperação de senha a partir de usuário e token.
     *
     * @param usuario Usuário para novo registro de recuperação de senha.
     * @param token Token de recuperação de senha.
     * @return Registro de recuperação de senha criado.
     */
    public RecuperacaoSenha salvarRecuperacaoSenhaToken(Usuario usuario, String token) {

        if(usuario == null)
            throw new IllegalArgumentException("O usuário deve ser informado.");

        if(token == null || token.isBlank())
            throw new IllegalArgumentException("O token deve se informado.");

        LocalDateTime dataExpiracao = LocalDateTime.now(clock).plusMinutes(carteiraProperty.getRecuperacaoSenha().getTokenValidityMinutes());

        RecuperacaoSenha recuperacaoSenha = new RecuperacaoSenha();
        recuperacaoSenha.setUsuario(usuario);
        recuperacaoSenha.setToken(token);
        recuperacaoSenha.setDataExpiracao(dataExpiracao);

        recuperacaoSenhaRepository.save(recuperacaoSenha);

        return recuperacaoSenha;
    }

    /**
     * Verifica se o registro de recuperação está com o token válido ou com data de expiração ainda não atingida.
     *
     * @param recuperacaoSenha Registro de recuperação de senha.
     * @return true se registro não estiver expirado, caso contrário, retornará false.
     */
    public boolean isTokenValido(RecuperacaoSenha recuperacaoSenha) {
        if(recuperacaoSenha == null) {
            return false;
        }

        LocalDateTime dataExpiracao = recuperacaoSenha.getDataExpiracao();
        LocalDateTime agora = LocalDateTime.now(clock);

        return agora.isBefore(dataExpiracao);
    }
}
