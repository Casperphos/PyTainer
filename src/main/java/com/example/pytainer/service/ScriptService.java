package com.example.pytainer.service;

import com.example.pytainer.component.ProcessManager;
import com.example.pytainer.exception.ScriptMasterException;
import com.example.pytainer.model.FileNode;
import com.example.pytainer.model.Script;
import com.example.pytainer.model.ScriptStatus;
import com.example.pytainer.repository.ScriptRepository;
import com.example.pytainer.util.RandomHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import static com.example.pytainer.util.FileHelper.scanDirectory;
import static com.example.pytainer.util.PathHelper.getNormalizedPath;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScriptService {
    private final ProcessManager processManager;
    private final ScriptRepository scriptRepository;
    private final RandomHelper randomHelper;
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final Scheduler jdbcScheduler = Schedulers.boundedElastic();
    private final String SCRIPT_UPLOAD_DIR = "script_upload/";
    private final String SCRIPT_LOG_DIR = "script_log/";

    private Script findScript(String processKey) {
        return scriptRepository.findByProcessKey(processKey)
                .orElseThrow(() -> new ResourceNotFoundException("Couldn't find script with processKey: " + processKey));
    }

    private Path getLogPath(Script script) {
        String fileName = script.getName().replaceAll("\\..*$", "") + ".log";
        return getNormalizedPath(SCRIPT_LOG_DIR + script.getProcessKey() + "/" + fileName);
    }

    public Script getScriptData(String processKey) {
        return findScript(processKey);
    }

    public List<Script> getAllScriptData() {
        return scriptRepository.findAll();
    }

    public Script uploadScript(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();

        if (fileName == null)
            throw new IllegalArgumentException("File name is null");

        String processKey = randomHelper.generateUniqueString(fileName);
        Path filePath = getNormalizedPath(SCRIPT_UPLOAD_DIR + processKey + "/" + fileName);

        if (!Files.exists(filePath))
            Files.createDirectories(filePath);

        Script script = new Script(
                processKey,
                fileName,
                filePath.toString(),
                ScriptStatus.NOT_STARTED,
                null);

        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        return scriptRepository.save(script);
    }

    public Script runScript(String processKey) throws IOException, ExecutionException, InterruptedException {
        // Check if the script is already running
        if (scriptRepository.findByProcessKey(processKey).stream().anyMatch(script -> script.getStatus() == ScriptStatus.RUNNING))
            throw new IllegalStateException("Script with processKey: " + processKey + " is already running");

        Script script = findScript(processKey);

        Future<Script> future = executorService.submit(() -> {
            try {
                log.info("Starting script: {}", script.getName());

                /* INSTALLING REQUIREMENTS */
                ProcessBuilder pipreqs = new ProcessBuilder("pipreqs", "--force", SCRIPT_UPLOAD_DIR)
                        .redirectErrorStream(true);
//                        .inheritIO();
                Process pipreqsProcess = pipreqs.start();
                pipreqsProcess.waitFor();

                ProcessBuilder pipInstall = new ProcessBuilder("python", "-u", "-m", "pip", "install", "-r", SCRIPT_UPLOAD_DIR + "requirements.txt")
                        .redirectErrorStream(true);
//                        .inheritIO();
                Process pipProcess = pipInstall.start();
                pipProcess.waitFor();

                /* RUNNING SCRIPT */
                Path logDir = getNormalizedPath(SCRIPT_LOG_DIR + script.getProcessKey() + "/");

                if (!Files.exists(logDir))
                    Files.createDirectories(logDir);

                File logFile = getLogPath(script).toFile();

                ProcessBuilder processBuilder = new ProcessBuilder("python", "-u", script.getFilePath())
                        .redirectErrorStream(true)
                        .redirectOutput(logFile);

                Process process = processBuilder.start();
                processManager.addProcess(script.getProcessKey(), process);

                script.setStatus(ScriptStatus.RUNNING);
                script.setLastExecutionTime(LocalDateTime.now());
                log.info("Script started: {}", script.getName());
                return scriptRepository.save(script);

            } catch (Exception exception) {
                script.setStatus(ScriptStatus.FAILED);
                scriptRepository.save(script);
                throw new ScriptMasterException("Script execution failed: " + exception.getMessage());
            }
        });
        return future.get();
    }

    public Script stopScript(String processKey) {
        Process process = processManager.getProcess(processKey);

        if (process == null)
            throw new ResourceNotFoundException("Couldn't find process with processKey: " + processKey);

        processManager.killProcess(processKey);

        Script script = findScript(processKey);

        script.setStatus(ScriptStatus.STOPPED);
        return scriptRepository.save(script);
    }

    public List<String> getScriptLog(String processKey, int maxLines) throws IOException {
        Script script = findScript(processKey);

        Path logPath = getLogPath(script);

        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(logPath)) {
            String line;
            while ((line = reader.readLine()) != null && lines.size() < maxLines) {
                lines.add(line);
            }
        }

        return lines;
    }

    public Flux<ServerSentEvent<?>> streamScriptLog(String processKey) {
        return Mono.fromCallable(() -> findScript(processKey))
                .subscribeOn(jdbcScheduler)
                .flatMapMany(script -> {
                    Path logPath = getLogPath(script);
                    AtomicLong lastPosition = new AtomicLong(0);

                    return Flux.interval(Duration.ofSeconds(1))
                            .flatMap(_ -> Mono.fromCallable(() -> {
                                if (Files.exists(logPath)) {
                                    try (RandomAccessFile reader = new RandomAccessFile(logPath.toFile(), "r")) {
                                        long fileLength = reader.length();

                                        if (fileLength > lastPosition.get()) {
                                            reader.seek(lastPosition.get());

                                            List<String> newLines = new ArrayList<>();
                                            String line;

                                            while ((line = reader.readLine()) != null)
                                                newLines.add(line);

                                            lastPosition.set(fileLength);
                                            return newLines.stream();
                                        }
                                    }
                                }
                                return Stream.empty();
                            }).subscribeOn(Schedulers.boundedElastic()).flatMapMany(Flux::fromStream))
                            .map(line -> ServerSentEvent.builder(line).build());
                });
    }

    public ScriptStatus getScriptStatus(String processKey) {
        return scriptRepository.findByProcessKey(processKey)
                .orElseThrow(() -> new ResourceNotFoundException("Couldn't find script with processKey: " + processKey))
                .getStatus();
    }

    public FileNode getScriptFileTree(String processKey) throws IOException {
        Script script = findScript(processKey);

        Path scriptOutputDir = getNormalizedPath(SCRIPT_UPLOAD_DIR + script.getProcessKey() + "/");

        if (!Files.exists(scriptOutputDir))
            throw new ResourceNotFoundException("Output directory not found for script: " + processKey);


        return scanDirectory(scriptOutputDir);
    }

    public ResponseEntity<InputStreamResource> downloadFile(String processKey, String filePath) throws IOException {
        Script script = findScript(processKey);

        Path fullPath = getNormalizedPath(SCRIPT_UPLOAD_DIR + filePath);

        if (!fullPath.startsWith(getNormalizedPath(SCRIPT_UPLOAD_DIR + script.getProcessKey())))
            throw new SecurityException("Access to file is not allowed"); // TODO: Handle in GlobalExceptionHandler

        if (!Files.exists(fullPath) || Files.isDirectory(fullPath))
            throw new ResourceNotFoundException("File not found: " + filePath);

        InputStreamResource inputStreamResource = new InputStreamResource(Files.newInputStream(fullPath));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fullPath.getFileName().toString() + "\"")
                .contentLength(Files.size(fullPath))
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(inputStreamResource);
    }

    public Script restartScript(String processKey) throws IOException, ExecutionException, InterruptedException {
        if (getScriptData(processKey).getStatus() == ScriptStatus.RUNNING) {
            log.info("Stopping script: {}", processKey);
            stopScript(processKey);
        }

        return runScript(processKey);
    }

    private void rmrfDirectory(String uploadDirectory, String processKey) throws IOException {
        Path scriptDir = getNormalizedPath(uploadDirectory + processKey);
        if (Files.exists(scriptDir)) {
            Files.walk(scriptDir).sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    log.info("Deleting file: {}", path);
                    Files.delete(path);
                } catch (IOException e) {
                    throw new ScriptMasterException("Failed to delete file: " + path);
                }
            });
        }
    }

    public Script removeScript(String processKey) throws IOException {
        Script script = findScript(processKey);

        if (script.getStatus() == ScriptStatus.RUNNING) {
            log.info("Stopping script: {}", processKey);
            script = stopScript(processKey);
        }

        // Delete script directory
        rmrfDirectory(SCRIPT_UPLOAD_DIR, processKey);

        // Delete log directory
        rmrfDirectory(SCRIPT_LOG_DIR, processKey);

        // Delete script data
        scriptRepository.delete(script);
        return script;
    }
}
