package co.edu.uptc.model;

public class CodeLine {
    public static final int NO_COLOR_ID = -1;

    private int lineNumber;
    private String content;
    private String activeUsername;
    private int activeColorId;
    private long lastEditTimestamp;

    public CodeLine(int lineNumber, String content) {
        this.lineNumber = lineNumber;
        this.content = content != null ? content : "";
        this.activeUsername = null;
        this.activeColorId = NO_COLOR_ID;
        this.lastEditTimestamp = 0L;
    }

    public boolean lock(String username, int colorId) {
        boolean locked = false;
        if (username != null && (!isLocked() || isOwnedBy(username))) {
            this.activeUsername = username;
            this.activeColorId = colorId;
            this.lastEditTimestamp = System.currentTimeMillis();
            locked = true;
        }
        return locked;
    }

    public void unlock() {
        this.activeUsername = null;
        this.activeColorId = NO_COLOR_ID;
        this.lastEditTimestamp = 0L;
    }

    public void updateContent(String newContent) {
        this.content = newContent != null ? newContent : "";
        this.lastEditTimestamp = System.currentTimeMillis();
    }

    public boolean isLocked() {
        return this.activeUsername != null;
    }

    public boolean isOwnedBy(String username) {
        return this.activeUsername != null && this.activeUsername.equals(username);
    }

    public boolean isInactive(long maxInactivityMillis) {
        boolean inactive = false;
        if (isLocked()) {
            long elapsed = System.currentTimeMillis() - this.lastEditTimestamp;
            inactive = elapsed >= maxInactivityMillis;
        }
        return inactive;
    }

    public int getLineNumber() {
        return this.lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public String getContent() {
        return this.content;
    }

    public String getActiveUsername() {
        return this.activeUsername;
    }

    public int getActiveColorId() {
        return this.activeColorId;
    }

    public long getLastEditTimestamp() {
        return this.lastEditTimestamp;
    }
}
