package com.example.pytainer.component;

import com.example.pytainer.model.Script;
import com.example.pytainer.model.ScriptStatus;
import com.example.pytainer.repository.ScriptRepository;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class ProcessManager {
    private final ScriptRepository scriptRepository;
    private final ConcurrentHashMap<String, Process> runningProcesses = new ConcurrentHashMap<>();

    public Process getProcess(String processKey) {
        return runningProcesses.get(processKey);
    }

    public void addProcess(String processKey, Process process) {
        runningProcesses.put(processKey, process);
    }

    public void killProcess(String processKey) {
        runningProcesses.get(processKey).destroyForcibly();
        runningProcesses.remove(processKey);
    }

    @PreDestroy
    public void shutdownAllProcesses() {
        for (Process process : runningProcesses.values()) {
            if (process.isAlive())
                process.destroyForcibly();
        }
        runningProcesses.clear();

        List<Script> allScripts = scriptRepository.findAll();
        for (Script script : allScripts) {
            script.setStatus(ScriptStatus.STOPPED);
            scriptRepository.save(script);
        }
    }
}
