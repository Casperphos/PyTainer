package com.example.scriptmaster.controller;

import com.example.scriptmaster.model.Script;
import com.example.scriptmaster.service.ScriptService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/script")
@RequiredArgsConstructor
public class ScriptController {

    private final ScriptService scriptService;

    @GetMapping("/all")
    public ResponseEntity<List<Script>> getAllScriptData() {
        List<Script> scripts = scriptService.getAllScriptData();
        return ResponseEntity.ok(scripts);
    }

    @GetMapping("/UUID")
    public ResponseEntity<Script> getScriptData(@RequestParam UUID scriptUUID) {
        Script script = scriptService.getScriptData(scriptUUID);
        return ResponseEntity.ok(script);
    }

    @PostMapping("/upload")
    public ResponseEntity<Script> uploadScript(@RequestParam MultipartFile file) throws IOException {
        Script uploadedScript = scriptService.uploadScript(file);
        return ResponseEntity.ok(uploadedScript);
    }

    @PostMapping("/run")
    public ResponseEntity<Void> runScript(@RequestParam UUID scriptUUID) throws IOException {
        scriptService.runScript(scriptUUID);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/stop")
    public ResponseEntity<Void> stopScript(@RequestParam UUID scriptUUID) {
        scriptService.stopScript(scriptUUID);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/log")
    public ResponseEntity<String> getScriptLog(
            @RequestParam UUID scriptUUID,
            @RequestParam(required = false, defaultValue = "100") int maxLines) throws IOException {
        String log = scriptService.getScriptLog(scriptUUID, maxLines);
        return ResponseEntity.ok(log);
    }

    @GetMapping("/status")
    public ResponseEntity<Script> getScriptStatus(@RequestParam UUID scriptUUID) {
        Script script = scriptService.getScriptStatus(scriptUUID);
        return ResponseEntity.ok(script);
    }
}
