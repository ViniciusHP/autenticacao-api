package com.vhp.autenticacao.api.configurations.security;

import com.vhp.autenticacao.api.configurations.property.AutenticacaoProperty;
import com.vhp.autenticacao.api.configurations.property.JwtProperty;
import com.vhp.autenticacao.api.document.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.TextCodec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

/**
 * Serviço para manipulação e geração de token JWT.
 */
@Service
public class TokenService {
    private final AutenticacaoProperty carteiraProperty;

    private final Clock clock;

    @Autowired
    public TokenService(AutenticacaoProperty carteiraProperty, Clock clock) {
        this.carteiraProperty = carteiraProperty;
        this.clock = clock;
    }

    /**
     * Gera novo access token a partir de usuário.
     * @param usuarioLogado - Usuário utilizado para gerar access token.
     * @return Access token gerado.
     */
    public String gerarAccessToken(Usuario usuarioLogado) {
        if(usuarioLogado == null || usuarioLogado.getId() == null) {
            throw new IllegalArgumentException("Usuário inválido.");
        }

        JwtProperty jwtProperty = getJwtApplicationConfiguration();
        return gerarToken(jwtProperty.getAccessTokenValiditySeconds(), usuarioLogado);
    }

    /**
     * Gera novo refresh token a partir de usuário.
     * @param usuarioLogado - Usuário utilizado para gerar access token.
     * @return Refresh token gerado.
     */
    public String gerarRefreshToken(Usuario usuarioLogado) {
        if(usuarioLogado == null || usuarioLogado.getId() == null) {
            throw new IllegalArgumentException("Usuário inválido.");
        }

        JwtProperty jwtProperty = getJwtApplicationConfiguration();
        return gerarToken(jwtProperty.getRefreshTokenValiditySeconds(), usuarioLogado);
    }

    /**
     * Verifica se token é valido.
     * @param token - Token a ser verificado.
     * @return false caso token seja vazio, null ou inválido, caso contrário, true.
     */
    public boolean isTokenValido(String token) {
        try{
            if(token == null || token.isBlank()) {
                return false;
            }

            getJws(token);
            return true;
        }catch (Exception e) {
            return false;
        }
    }

    /**
     * Verifica se token é inválido.
     * @param token - Token a ser verificado.
     * @return true caso token seja vazio, null ou inválido, caso contrário, false.
     */
    public boolean isTokenInvalido(String token) {
        return !this.isTokenValido(token);
    }

    /**
     * Obtém Id do usuário a partir do token fornecido.
     * @param token
     * @return Id do usuário.
     */
    public UUID getIdUsuario(String token) {
        if(isTokenInvalido(token)) {
            throw new IllegalArgumentException("O token fornecido é inválido.");
        }

        Claims claims = getJws(token).getBody();
        return UUID.fromString(claims.getSubject());
    }

    /**
     * Gera novo token JWT.
     * @param validadeToken - Validade do token em segundos
     * @param usuarioLogado - Usuário atual
     * @return Token JWT.
     */
    private String gerarToken(int validadeToken, Usuario usuarioLogado) {
        JwtProperty jwtProperty = getJwtApplicationConfiguration();
        Instant hoje = Instant.now(clock);
        Instant dataExpiracao = hoje.plus(validadeToken, ChronoUnit.SECONDS);

        return Jwts.builder()
                .setIssuer(jwtProperty.getIssuer())
                .setAudience(jwtProperty.getAudience())
                .setSubject(usuarioLogado.getId().toString())
                .claim("name", usuarioLogado.getNome())
                .setIssuedAt(Date.from(hoje))
                .setExpiration(Date.from(dataExpiracao))
                .signWith(SignatureAlgorithm.HS256, getKey())
                .compact();
    }

    /**
     * Obtém payload do token JWT.
     * @param token
     * @return Payload do token JWT.
     */
    private Jws<Claims> getJws(String token) {
        JwtProperty jwtProperty = getJwtApplicationConfiguration();
        return Jwts.parser()
                .setSigningKey(getKey())
                .requireAudience(jwtProperty.getAudience())
                .requireIssuer(jwtProperty.getIssuer())
                .parseClaimsJws(token);
    }

    /**
     * Obtém chave de assinatura do token JWT.
     * @return Chave de assinatura do token.
     */
    private byte[] getKey() {
        JwtProperty jwtProperty = getJwtApplicationConfiguration();
        return TextCodec.BASE64.decode(jwtProperty.getSecret());
    }

    /**
     * Obtém propriedades JWT da aplicação.
     * @return Propriedades JWT.
     */
    private JwtProperty getJwtApplicationConfiguration() {
        return this.carteiraProperty.getJwt();
    }
}
