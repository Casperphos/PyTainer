package com.example.scriptmaster.controller;

import com.example.scriptmaster.model.FileNode;
import com.example.scriptmaster.model.Script;
import com.example.scriptmaster.model.ScriptStatus;
import com.example.scriptmaster.service.ScriptService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/v1/script")
@RequiredArgsConstructor
public class ScriptController {
    private final ScriptService scriptService;

    @GetMapping("/all")
    public ResponseEntity<List<Script>> getAllScriptData() {
        return ResponseEntity.ok(scriptService.getAllScriptData());
    }

    @GetMapping("/process-key")
    public ResponseEntity<Script> getScriptData(@RequestParam String processKey) {
        return ResponseEntity.ok(scriptService.getScriptData(processKey));
    }

    @PostMapping("/upload")
    public ResponseEntity<Script> uploadScript(@RequestParam MultipartFile file) throws IOException {
        return ResponseEntity.ok(scriptService.uploadScript(file));
    }

    @PostMapping("/run")
    public ResponseEntity<Script> runScript(@RequestParam String processKey) throws IOException, ExecutionException, InterruptedException {
        return ResponseEntity.ok(scriptService.runScript(processKey));
    }

    @PostMapping("/stop")
    public ResponseEntity<Script> stopScript(@RequestParam String processKey) {
        return ResponseEntity.ok(scriptService.stopScript(processKey));
    }

    @GetMapping("/log")
    public ResponseEntity<List<String>> getScriptLog(
            @RequestParam String processKey,
            @RequestParam(required = false, defaultValue = "100") int maxLines) throws IOException {
        return ResponseEntity.ok(scriptService.getScriptLog(processKey, maxLines));
    }

    @GetMapping("/status")
    public ResponseEntity<ScriptStatus> getScriptStatus(@RequestParam String processKey) {
        return ResponseEntity.ok(scriptService.getScriptStatus(processKey));
    }

    @GetMapping("/files")
    public ResponseEntity<FileNode> getScriptFileTree(@RequestParam String processKey) throws IOException {
        return ResponseEntity.ok(scriptService.getScriptFileTree(processKey));
    }

    @GetMapping("/download")
    public ResponseEntity<InputStreamResource> downloadFile(
            @RequestParam String processKey,
            @RequestParam String filePath) throws IOException {
        return scriptService.downloadFile(processKey, filePath);
    }

    @GetMapping(value = "/log/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<?>> streamScriptLog(@RequestParam String processKey) {
        return scriptService.streamScriptLog(processKey);
    }
}
