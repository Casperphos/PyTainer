package com.example.scriptmaster.repository;

import com.example.scriptmaster.model.FileReference;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileReferenceRepository extends MongoRepository<FileReference, String> {
}