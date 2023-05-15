package com.vhp.autenticacao.api;

import com.vhp.autenticacao.api.configurations.property.AutenticacaoProperty;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableMongoRepositories
@EnableConfigurationProperties(AutenticacaoProperty.class)
public class AutenticacaoApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(AutenticacaoApiApplication.class, args);
	}
}
