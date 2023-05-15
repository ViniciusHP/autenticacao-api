package com.vhp.autenticacao.api.repository;

import com.vhp.autenticacao.api.document.RecuperacaoSenha;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RecuperacaoSenhaRepository extends MongoRepository<RecuperacaoSenha, ObjectId> {

    @Query("{ token: '?0' }")
    Optional<RecuperacaoSenha> findByToken(String token);

    @Aggregation (pipeline = {
            "{ '$match': { 'dataExpiracao': { '$lte': ?0 } } }",
            "{ '$sort': { 'dataCriacao': 1 } }",
            "{ '$skip': ?1 }",
            "{ '$limit': ?2 }"})
    List<RecuperacaoSenha> findByDataExpiracaoLesserThan(LocalDateTime data, int skip, int limit);
}
