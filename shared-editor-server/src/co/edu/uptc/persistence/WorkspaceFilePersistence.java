package co.edu.uptc.persistence;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

public class WorkspaceFilePersistence {
    private static final String JAVA_EXTENSION = ".java";
    private Path storageDirectory;

    public WorkspaceFilePersistence(String directoryPath) {
        this.storageDirectory = Paths.get(directoryPath);
        ensureDirectoryExists();
    }

    private void ensureDirectoryExists() {
        try {
            if (!Files.exists(this.storageDirectory)) {
                Files.createDirectories(this.storageDirectory);
            }
        } catch (IOException ex) {
            this.storageDirectory = Paths.get(".").toAbsolutePath();
        }
    }

    public synchronized boolean saveFile(String fileName, String sourceCode) {
        boolean saved = false;
        if (isSafeJavaFileName(fileName)) {
            Path target = this.storageDirectory.resolve(fileName);
            try {
                String content = sourceCode != null ? sourceCode : "";
                Files.write(target, content.getBytes(StandardCharsets.UTF_8));
                saved = true;
            } catch (IOException ex) {
                saved = false;
            }
        }
        return saved;
    }

    public synchronized Map<String, String> loadAllFiles() {
        Map<String, String> loadedFiles = new LinkedHashMap<>();
        if (Files.isDirectory(this.storageDirectory)) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(this.storageDirectory, "*" + JAVA_EXTENSION)) {
                for (Path entry : stream) {
                    if (Files.isRegularFile(entry)) {
                        String name = entry.getFileName().toString();
                        String content = new String(Files.readAllBytes(entry), StandardCharsets.UTF_8);
                        loadedFiles.put(name, content);
                    }
                }
            } catch (IOException ex) {
                loadedFiles.clear();
            }
        }
        return loadedFiles;
    }

    private boolean isSafeJavaFileName(String fileName) {
        boolean safe = false;
        if (fileName != null) {
            boolean validExt = fileName.endsWith(JAVA_EXTENSION) && fileName.length() > JAVA_EXTENSION.length();
            boolean noPathTraversal = !fileName.contains("/") && !fileName.contains("\\") && !fileName.contains("..");
            safe = validExt && noPathTraversal;
        }
        return safe;
    }
}
