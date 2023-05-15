package com.vhp.autenticacao.api.configurations.application;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class ApplicationConfig {

    @Bean
    public Clock getClock() {
        return Clock.systemDefaultZone();
    }
}
