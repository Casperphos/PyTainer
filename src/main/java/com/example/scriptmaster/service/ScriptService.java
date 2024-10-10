package com.example.scriptmaster.service;

import com.example.scriptmaster.model.Script;
import com.example.scriptmaster.model.ScriptStatus;
import com.example.scriptmaster.repository.ScriptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.example.scriptmaster.util.PathHelper.getNormalizedPath;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScriptService {
    private final ScriptRepository scriptRepository;
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final String SCRIPT_UPLOAD_DIR = "script_upload/";
    private final String SCRIPT_LOG_DIR = "script_log/";
    private final ConcurrentHashMap<UUID, Process> runningProcesses = new ConcurrentHashMap<>();

    public Script getScriptData(UUID scriptUUID) {
        return scriptRepository.findByUUID(scriptUUID)
                .orElseThrow(() -> new ResourceNotFoundException("Couldn't find script with UUID: " + scriptUUID));
    }

    public List<Script> getAllScriptData() {
        return scriptRepository.findAll();
    }

    public Script uploadScript(MultipartFile file) throws IOException {
        // Make sure that SCRIPT_UPLOAD_DIR exists
        Path scriptUploadDirPath = getNormalizedPath(SCRIPT_UPLOAD_DIR);
        if (!Files.exists(scriptUploadDirPath))
            Files.createDirectories(scriptUploadDirPath);

        String fileName = file.getOriginalFilename();
        Path filePath = getNormalizedPath(SCRIPT_UPLOAD_DIR + fileName);
        Files.write(filePath, file.getBytes());

        Script script = Script.builder()
                .UUID(UUID.randomUUID())
                .name(fileName)
                .filePath(filePath.toString())
                .status(ScriptStatus.NOT_STARTED)
                .lastExecutionTime(null)
                .build();

        return scriptRepository.save(script);
    }

    public void runScript(UUID scriptUUID) throws IOException {
        // Make sure that SCRIPT_LOG_DIR exists
        Path scriptLogDirPath = getNormalizedPath(SCRIPT_LOG_DIR);
        if (!Files.exists(scriptLogDirPath))
            Files.createDirectories(scriptLogDirPath);

        Script script = scriptRepository.findByUUID(scriptUUID)
                .orElseThrow(() -> new ResourceNotFoundException("Couldn't find script with UUID: " + scriptUUID));

        executorService.submit(() -> {
            try {
                ProcessBuilder processBuilder = new ProcessBuilder("python", script.getFilePath());
                processBuilder.redirectErrorStream(true);
                File logFile = new File(SCRIPT_LOG_DIR + script.getName() + ".log");
                processBuilder.redirectOutput(logFile);

                Process process = processBuilder.start();
                runningProcesses.put(scriptUUID, process);

                script.setStatus(ScriptStatus.RUNNING);
                script.setLastExecutionTime(LocalDateTime.now());
                scriptRepository.save(script);

            } catch (Exception exception) {
                script.setStatus(ScriptStatus.FAILED);
                scriptRepository.save(script);
                log.error("Script execution failed: {}", exception.getMessage());
            }
        });
    }

    public void stopScript(UUID scriptUUID) {
        Process process = runningProcesses.get(scriptUUID);

        if (process != null) {
            process.destroy();
            runningProcesses.remove(scriptUUID);

            Script script = scriptRepository.findByUUID(scriptUUID)
                    .orElseThrow(() -> new ResourceNotFoundException("Couldn't find script with UUID: " + scriptUUID));

            script.setStatus(ScriptStatus.STOPPED);
            scriptRepository.save(script);
        }
    }

    // TODO: Implement maxLines
    public String getScriptLog(UUID scriptUUID, int maxLines) throws IOException {
        // Make sure that SCRIPT_LOG_DIR exists
        Path scriptLogDirPath = getNormalizedPath(SCRIPT_LOG_DIR);
        if (!Files.exists(scriptLogDirPath))
            Files.createDirectories(scriptLogDirPath);

        Script script = scriptRepository.findByUUID(scriptUUID)
                .orElseThrow(() -> new ResourceNotFoundException("Couldn't find script with UUID: " + scriptUUID));

        Path logPath = getNormalizedPath(SCRIPT_LOG_DIR + script.getName() + ".log");
        return new String(Files.readAllBytes(logPath));
    }

    public Script getScriptStatus(UUID scriptUUID) {
        return scriptRepository.findByUUID(scriptUUID)
                .orElseThrow(() -> new ResourceNotFoundException("Couldn't find script with UUID: " + scriptUUID));
    }
}
