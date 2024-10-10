package com.example.scriptmaster.model;

public enum ScriptStatus {
    NOT_STARTED,
    INITIALIZING,
    RUNNING,
    PAUSED,
    RESUMING,
    COMPLETED,
    FAILED,
    TIMEOUT,
    INTERRUPTED,
    WAITING_FOR_INPUT,
    CLEANING_UP,
    STOPPED
}
