package com.example.pytainer.model;

import lombok.Data;

import java.util.List;

@Data
public class FileNode {
    private String name;
    private boolean isDirectory;
    private List<FileNode> children;
}
