package com.vhp.autenticacao.api.repository;

import com.vhp.autenticacao.api.document.EmailDocument;
import com.vhp.autenticacao.api.document.EmailStatus;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface EmailRepository extends MongoRepository<EmailDocument, ObjectId>{

    @Aggregation(pipeline = {
        "{ '$match': { 'status': ?0 } }",
        "{ '$sort': { 'dataCriacao': 1 } }",
        "{ '$skip': ?1 }",
        "{ '$limit': ?2 }"
    })
    List<EmailDocument> findByStatus(EmailStatus status, int skip, int limit);
}
