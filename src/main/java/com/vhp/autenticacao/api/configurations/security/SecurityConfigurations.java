package com.vhp.autenticacao.api.configurations.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfigurations {

    private final AutenticacaoService autenticacaoService;

    private final TokenService tokenService;

    private final PasswordEncoder passwordEncoder;

    private static final String[] SWAGGER_PATHS = {"/swagger-ui.html", "/v3/api-docs/**", "/swagger-ui/**", "/webjars/swagger-ui/**"};

    @Autowired
    public SecurityConfigurations(AutenticacaoService autenticacaoService, TokenService tokenService, PasswordEncoder passwordEncoder) {
        this.autenticacaoService = autenticacaoService;
        this.tokenService = tokenService;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public SecurityFilterChain  securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors()
                .and()
                .authorizeHttpRequests((requests) -> requests
                        .requestMatchers(SWAGGER_PATHS).permitAll()
                        .requestMatchers("/oauth/**", "/usuarios/**").permitAll()
                        .anyRequest()
                        .authenticated()
                )
                    .csrf()
                        .disable()
                    .sessionManagement()
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                    .addFilterBefore(
                            new AutenticacaoViaTokenFilter(tokenService, autenticacaoService),
                            UsernamePasswordAuthenticationFilter.class)
                    .addFilterAfter(new ExtracaoRefreshTokenFilter(),
                            AutenticacaoViaTokenFilter.class)
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManagerBean(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.userDetailsService(autenticacaoService).passwordEncoder(passwordEncoder);
        return authenticationManagerBuilder.build();
    }

}
