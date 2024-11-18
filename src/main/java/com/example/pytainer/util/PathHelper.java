package com.example.pytainer.util;

import java.nio.file.Path;
import java.nio.file.Paths;

public final class PathHelper {
    public static Path getNormalizedPath(String path) {
        return Paths.get(path).normalize();
    }

    private PathHelper() {
        throw new UnsupportedOperationException("This is an utility class and cannot be instantiated");
    }
}
