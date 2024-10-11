package com.example.scriptmaster.component;

import com.example.scriptmaster.model.ProcessKey;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ProcessManager {
    private final ConcurrentHashMap<ProcessKey, Process> runningProcesses = new ConcurrentHashMap<>();

    public void addProcess(ProcessKey processKey, Process process) {
        runningProcesses.put(processKey, process);
    }

    public void removeProcess(ProcessKey processKey) {
        runningProcesses.remove(processKey);
    }

    @PreDestroy
    public void shutdownAllProcesses() {
        for (Process process : runningProcesses.values()) {
            if (process.isAlive())
                process.destroyForcibly();
        }
        runningProcesses.clear();
    }
}
