package co.edu.uptc.model;

import co.edu.uptc.structures.MyBinaryTreeAVL;
import co.edu.uptc.structures.MyDoubleList;
import co.edu.uptc.structures.MySimpleList;
import co.edu.uptc.structures.MyStack;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Workspace {
    private static final int MAX_USERS = 10;
    private static final String JAVA_EXTENSION = ".java";

    private MyBinaryTreeAVL<User> connectedUsers;
    private MySimpleList<String> activeUsernames;
    private MyBinaryTreeAVL<CodeFile> files;
    private MyDoubleList<String> registeredFileNames;
    private MyStack<Integer> availableColorIds;
    private long nextJoinOrder;

    public Workspace() {
        this.connectedUsers = new MyBinaryTreeAVL<>(Comparator.comparingLong(User::getJoinOrder));
        this.activeUsernames = new MySimpleList<>();
        this.files = new MyBinaryTreeAVL<>(Comparator.comparing(CodeFile::getFileName));
        this.registeredFileNames = new MyDoubleList<>();
        this.availableColorIds = new MyStack<>();
        this.nextJoinOrder = 1L;
        initAvailableColorIds();
    }

    private void initAvailableColorIds() {
        for (int colorId = MAX_USERS - 1; colorId >= 0; colorId--) {
            this.availableColorIds.push(colorId);
        }
    }

    public synchronized User registerUser(String username) {
        User registeredUser = null;
        if (username != null) {
            String trimmed = username.trim();
            if (!trimmed.isEmpty() && isUsernameAvailable(trimmed) && hasAvailableColorSlots()) {
                UserRole role = this.connectedUsers.isEmpty() ? UserRole.ADMIN : UserRole.COLLABORATOR;
                int colorId = this.availableColorIds.pop();
                registeredUser = new User(trimmed, colorId, role, this.nextJoinOrder);
                this.nextJoinOrder++;
                this.connectedUsers.add(registeredUser);
                this.activeUsernames.insert(trimmed.toLowerCase());
            }
        }
        return registeredUser;
    }

    public synchronized List<LockReference> disconnectUser(String username) {
        List<LockReference> releasedLocks = new ArrayList<>();
        User user = findUserByName(username);
        if (user != null) {
            releasedLocks = unlockAllUserLines(user.getUsername());
            this.connectedUsers.remove(user);
            this.activeUsernames.remove(user.getUsername().toLowerCase());
            this.availableColorIds.push(user.getColorId());
        }
        return releasedLocks;
    }

    public synchronized User findUserByName(String username) {
        User found = null;
        if (username != null) {
            List<User> orderedUsers = this.connectedUsers.inOrder();
            int index = 0;
            while (index < orderedUsers.size() && found == null) {
                User candidate = orderedUsers.get(index);
                if (candidate.getUsername().equalsIgnoreCase(username)) {
                    found = candidate;
                }
                index++;
            }
        }
        return found;
    }

    public synchronized User reassignAdminRole() {
        User newAdmin = null;
        List<User> orderedUsers = this.connectedUsers.inOrder();
        boolean hasAdmin = false;
        for (int i = 0; i < orderedUsers.size(); i++) {
            if (orderedUsers.get(i).isAdmin()) {
                hasAdmin = true;
            }
        }
        if (!hasAdmin && !orderedUsers.isEmpty()) {
            newAdmin = orderedUsers.get(0);
            newAdmin.setRole(UserRole.ADMIN);
        }
        return newAdmin;
    }

    public synchronized boolean isUsernameAvailable(String username) {
        boolean available = false;
        if (username != null && !username.trim().isEmpty()) {
            available = !this.activeUsernames.exits(username.trim().toLowerCase());
        }
        return available;
    }

    public synchronized boolean hasAvailableColorSlots() {
        return !this.availableColorIds.isEmpty();
    }

    public synchronized CodeFile createCodeFile(String requesterName, String fileName, String initialContent) {
        CodeFile created = null;
        User requester = findUserByName(requesterName);
        if (requester != null && requester.isAdmin() && isValidNewFileName(fileName)) {
            String cleanName = fileName.trim();
            created = new CodeFile(cleanName, initialContent);
            this.files.add(created);
            this.registeredFileNames.addFinal(cleanName);
        }
        return created;
    }

    public synchronized boolean loadExistingFile(String fileName, String initialContent) {
        boolean loaded = false;
        if (isValidNewFileName(fileName)) {
            String cleanName = fileName.trim();
            CodeFile loadedFile = new CodeFile(cleanName, initialContent);
            this.files.add(loadedFile);
            this.registeredFileNames.addFinal(cleanName);
            loaded = true;
        }
        return loaded;
    }

    private boolean isValidNewFileName(String fileName) {
        boolean valid = false;
        if (fileName != null) {
            String cleanName = fileName.trim();
            boolean validExtension = cleanName.endsWith(JAVA_EXTENSION)
                    && cleanName.length() > JAVA_EXTENSION.length();
            boolean notDuplicate = !this.registeredFileNames.contains(cleanName);
            valid = validExtension && notDuplicate;
        }
        return valid;
    }

    public synchronized CodeFile findFileByName(String fileName) {
        CodeFile found = null;
        if (fileName != null && !fileName.trim().isEmpty()) {
            found = this.files.find(new CodeFile(fileName.trim(), ""));
        }
        return found;
    }

    public synchronized boolean canLockLine(String fileName, int lineNumber, String username) {
        boolean canLock = false;
        User user = findUserByName(username);
        CodeFile file = findFileByName(fileName);
        if (user != null && file != null) {
            canLock = file.canLockLine(lineNumber, user.getUsername());
        }
        return canLock;
    }

    public synchronized boolean lockLine(String fileName, int lineNumber, String username) {
        boolean locked = false;
        User user = findUserByName(username);
        CodeFile file = findFileByName(fileName);
        if (user != null && file != null && file.canLockLine(lineNumber, user.getUsername())) {
            locked = file.lockLine(lineNumber, user.getUsername(), user.getColorId());
        }
        return locked;
    }

    public synchronized boolean editLine(String fileName, String newContent, String username) {
        boolean edited = false;
        CodeFile file = findFileByName(fileName);
        if (file != null) {
            edited = file.updateLineText(newContent, username);
        }
        return edited;
    }

    public synchronized boolean insertLine(String fileName, String text, String username) {
        boolean inserted = false;
        CodeFile file = findFileByName(fileName);
        if (file != null) {
            inserted = file.insertNewLine(text, username);
        }
        return inserted;
    }

    public synchronized boolean deleteLine(String fileName, String username) {
        boolean deleted = false;
        CodeFile file = findFileByName(fileName);
        if (file != null) {
            deleted = file.deleteLine(username);
        }
        return deleted;
    }

    public synchronized LockReference unlockLine(String fileName, String username) {
        LockReference released = null;
        CodeFile file = findFileByName(fileName);
        if (file != null) {
            released = file.unlockLine(username);
        }
        return released;
    }

    public synchronized List<LockReference> unlockAllUserLines(String username) {
        List<LockReference> releasedLocks = new ArrayList<>();
        List<CodeFile> allFiles = this.files.inOrder();
        for (int i = 0; i < allFiles.size(); i++) {
            LockReference released = allFiles.get(i).unlockLine(username);
            if (released != null) {
                releasedLocks.add(released);
            }
        }
        return releasedLocks;
    }

    public synchronized List<LockReference> releaseExpiredLocks(long maxInactivityMillis) {
        List<LockReference> releasedLocks = new ArrayList<>();
        List<CodeFile> allFiles = this.files.inOrder();
        for (int i = 0; i < allFiles.size(); i++) {
            releasedLocks.addAll(allFiles.get(i).releaseInactiveLines(maxInactivityMillis));
        }
        return releasedLocks;
    }

    public synchronized List<User> getConnectedUsers() {
        return new ArrayList<>(this.connectedUsers.inOrder());
    }

    public synchronized List<CodeFile> getFilesInOrder() {
        return new ArrayList<>(this.files.inOrder());
    }

    public synchronized int getFileCount() {
        return this.registeredFileNames.size();
    }
}
