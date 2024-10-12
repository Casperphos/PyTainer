package com.example.scriptmaster.repository;

import com.example.scriptmaster.model.Script;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ScriptRepository extends MongoRepository<Script, String> {
    Optional<Script> findByProcessKey(String processKey);
}
