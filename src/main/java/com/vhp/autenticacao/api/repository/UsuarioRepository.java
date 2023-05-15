package com.vhp.autenticacao.api.repository;

import com.vhp.autenticacao.api.document.Usuario;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepository extends MongoRepository<Usuario, UUID> {

    @Query("{ email: '?0' }")
    Optional<Usuario> findByEmail(String email);
}
