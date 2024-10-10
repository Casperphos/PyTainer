package com.example.scriptmaster.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Data
@Document(collection = "file_reference")
public class FileReference {
    @Id
    private UUID UUID;
    private String name;
    private String filePath;
    private UUID scriptUUID;
}
