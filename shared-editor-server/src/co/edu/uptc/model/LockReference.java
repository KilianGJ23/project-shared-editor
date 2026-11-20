package co.edu.uptc.model;

public class LockReference {
    private String fileName;
    private int lineNumber;
    private String username;

    public LockReference(String fileName, int lineNumber, String username) {
        this.fileName = fileName;
        this.lineNumber = lineNumber;
        this.username = username;
    }

    public String getFileName() {
        return this.fileName;
    }

    public int getLineNumber() {
        return this.lineNumber;
    }

    public String getUsername() {
        return this.username;
    }
}
