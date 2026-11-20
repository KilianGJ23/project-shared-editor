package co.edu.uptc.model;

import co.edu.uptc.structures.MyBinaryTreeAVL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CodeFile {
    private String fileName;
    private MyBinaryTreeAVL<CodeLine> lines;

    public CodeFile(String fileName, String rawContent) {
        this.fileName = fileName;
        this.lines = createLineTree();
        loadLinesFromText(rawContent);
    }

    private MyBinaryTreeAVL<CodeLine> createLineTree() {
        return new MyBinaryTreeAVL<>(Comparator.comparingInt(CodeLine::getLineNumber));
    }

    public void loadLinesFromText(String rawContent) {
        this.lines = createLineTree();
        if (rawContent == null || rawContent.isEmpty()) {
            this.lines.add(new CodeLine(1, ""));
        } else {
            String[] splitLines = rawContent.split("\\r?\\n", -1);
            for (int i = 0; i < splitLines.length; i++) {
                this.lines.add(new CodeLine(i + 1, splitLines[i]));
            }
        }
    }

    public CodeLine findLine(int lineNumber) {
        CodeLine found = null;
        if (lineNumber >= 1) {
            found = this.lines.find(new CodeLine(lineNumber, ""));
        }
        return found;
    }

    public CodeLine findLineLockedBy(String username) {
        CodeLine found = null;
        List<CodeLine> orderedLines = this.lines.inOrder();
        int index = 0;
        while (index < orderedLines.size() && found == null) {
            CodeLine candidate = orderedLines.get(index);
            if (candidate.isOwnedBy(username)) {
                found = candidate;
            }
            index++;
        }
        return found;
    }

    public boolean canLockLine(int lineNumber, String username) {
        CodeLine target = findLine(lineNumber);
        return target != null && (!target.isLocked() || target.isOwnedBy(username));
    }

    public boolean lockLine(int lineNumber, String username, int colorId) {
        boolean locked = false;
        CodeLine target = findLine(lineNumber);
        if (target != null && (!target.isLocked() || target.isOwnedBy(username))) {
            CodeLine previous = findLineLockedBy(username);
            if (previous != null && previous != target) {
                previous.unlock();
            }
            locked = target.lock(username, colorId);
        }
        return locked;
    }

    public LockReference unlockLine(String username) {
        LockReference released = null;
        CodeLine lockedLine = findLineLockedBy(username);
        if (lockedLine != null) {
            released = new LockReference(this.fileName, lockedLine.getLineNumber(), username);
            lockedLine.unlock();
        }
        return released;
    }

    public boolean updateLineText(String newText, String username) {
        boolean updated = false;
        CodeLine lockedLine = findLineLockedBy(username);
        if (lockedLine != null) {
            lockedLine.updateContent(newText);
            updated = true;
        }
        return updated;
    }

    public boolean insertNewLine(String text, String username) {
        boolean inserted = false;
        CodeLine lockedLine = findLineLockedBy(username);
        if (lockedLine != null) {
            int ownerColorId = lockedLine.getActiveColorId();
            int insertIndex = lockedLine.getLineNumber();
            List<CodeLine> updatedList = new ArrayList<>(this.lines.inOrder());
            CodeLine newLine = new CodeLine(insertIndex + 1, text);
            lockedLine.unlock();
            newLine.lock(username, ownerColorId);
            updatedList.add(insertIndex, newLine);
            rebuildTree(updatedList);
            inserted = true;
        }
        return inserted;
    }

    public boolean deleteLine(String username) {
        boolean deleted = false;
        CodeLine lockedLine = findLineLockedBy(username);
        if (lockedLine != null) {
            List<CodeLine> updatedList = new ArrayList<>(this.lines.inOrder());
            if (updatedList.size() > 1) {
                updatedList.remove(lockedLine.getLineNumber() - 1);
                rebuildTree(updatedList);
            } else {
                lockedLine.updateContent("");
            }
            deleted = true;
        }
        return deleted;
    }

    private void rebuildTree(List<CodeLine> orderedLines) {
        MyBinaryTreeAVL<CodeLine> rebuilt = createLineTree();
        for (int i = 0; i < orderedLines.size(); i++) {
            CodeLine current = orderedLines.get(i);
            current.setLineNumber(i + 1);
            rebuilt.add(current);
        }
        this.lines = rebuilt;
    }

    public List<LockReference> releaseInactiveLines(long maxInactivityMillis) {
        List<LockReference> releasedLocks = new ArrayList<>();
        List<CodeLine> orderedLines = this.lines.inOrder();
        for (int i = 0; i < orderedLines.size(); i++) {
            CodeLine line = orderedLines.get(i);
            if (line.isInactive(maxInactivityMillis)) {
                releasedLocks.add(new LockReference(this.fileName, line.getLineNumber(), line.getActiveUsername()));
                line.unlock();
            }
        }
        return releasedLocks;
    }

    public String buildFullSourceCode() {
        List<CodeLine> orderedLines = this.lines.inOrder();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < orderedLines.size(); i++) {
            if (i > 0) {
                builder.append("\n");
            }
            builder.append(orderedLines.get(i).getContent());
        }
        return builder.toString();
    }

    public String getFileName() {
        return this.fileName;
    }

    public List<CodeLine> getOrderedLines() {
        return this.lines.inOrder();
    }
}
