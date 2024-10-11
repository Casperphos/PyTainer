package com.example.scriptmaster.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;


@Data
@AllArgsConstructor
@Document(collection = "script")
public class Script {
    @Id
    private String UUID;
    private String name;
    private String filePath;
    private ScriptStatus status;
    private LocalDateTime lastExecutionTime;
    private ProcessKey processKey;
}
