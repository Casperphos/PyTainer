package com.example.scriptmaster.util;

import com.example.scriptmaster.model.FileNode;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public final class FileHelper {
    private FileHelper() {
        throw new UnsupportedOperationException("This is an utility class and cannot be instantiated");
    }

    public static FileNode scanDirectory(Path dir) throws IOException {
        FileNode node = new FileNode();
        node.setName(dir.getFileName().toString());
        node.setDirectory(true);

        List<FileNode> children;
        try (Stream<Path> pathStream = Files.list(dir)) {
            children = pathStream
                    .map(path -> {
                        try {
                            if (Files.isDirectory(path)) {
                                return scanDirectory(path);
                            } else {
                                FileNode fileNode = new FileNode();
                                fileNode.setName(path.getFileName().toString());
                                fileNode.setDirectory(false);
                                return fileNode;
                            }
                        } catch (IOException e) {
                            log.error("Error scanning directory: {}", path, e);
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }

        node.setChildren(children);
        return node;
    }
}
