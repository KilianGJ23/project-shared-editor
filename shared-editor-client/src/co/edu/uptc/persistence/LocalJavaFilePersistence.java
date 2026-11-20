package co.edu.uptc.persistence;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class LocalJavaFilePersistence {
    private static final String ALLOWED_EXTENSION = ".java";

    public boolean isValidJavaFile(String filePath) {
        boolean valid = false;
        if (filePath != null) {
            String trimmed = filePath.trim();
            valid = trimmed.endsWith(ALLOWED_EXTENSION) && trimmed.length() > ALLOWED_EXTENSION.length();
        }
        return valid;
    }

    public String extractFileName(String filePath) {
        String fileName = "";
        if (filePath != null && !filePath.trim().isEmpty()) {
            Path path = Paths.get(filePath.trim());
            if (path.getFileName() != null) {
                fileName = path.getFileName().toString();
            }
        }
        return fileName;
    }

    public String readJavaFile(String filePath) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(filePath));
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public void writeJavaFile(String filePath, String sourceCode) throws IOException {
        String normalizedPath = ensureJavaExtension(filePath);
        String content = sourceCode != null ? sourceCode : "";
        Files.write(Paths.get(normalizedPath), content.getBytes(StandardCharsets.UTF_8));
    }

    private String ensureJavaExtension(String filePath) {
        String result = filePath.trim();
        if (!result.endsWith(ALLOWED_EXTENSION)) {
            result = result + ALLOWED_EXTENSION;
        }
        return result;
    }
}
