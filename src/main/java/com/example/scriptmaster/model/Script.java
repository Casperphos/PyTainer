package com.example.scriptmaster.model;

import lombok.Builder;
import lombok.Data;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@Document(collection = "script")
public class Script {
    @Id
    private UUID UUID;
    private String name;
    private String filePath;
    private ScriptStatus status;
    private LocalDateTime lastExecutionTime;
}
