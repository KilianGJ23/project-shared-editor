package co.edu.uptc.presenter;

import co.edu.uptc.model.CodeFile;
import co.edu.uptc.model.CodeLine;
import co.edu.uptc.model.LockReference;
import co.edu.uptc.model.User;
import co.edu.uptc.model.Workspace;
import co.edu.uptc.network.ClientHandler;
import co.edu.uptc.network.CollaborativeServer;
import co.edu.uptc.network.MessageType;
import co.edu.uptc.network.NetworkMessage;
import co.edu.uptc.network.ServerMessageListener;
import co.edu.uptc.persistence.MessageSerializer;
import co.edu.uptc.persistence.WorkspaceFilePersistence;
import java.util.List;
import java.util.Map;

public class ServerPresenter implements ServerMessageListener {
    private static final int DEFAULT_PORT = 1372;
    private static final String DEFAULT_WORKSPACE_DIR = "server_workspace";

    private Workspace workspace;
    private MessageSerializer serializer;
    private WorkspaceFilePersistence filePersistence;
    private CollaborativeServer server;

    public ServerPresenter() {
        this(DEFAULT_PORT);
    }

    public ServerPresenter(int port) {
        this.workspace = new Workspace();
        this.serializer = new MessageSerializer();
        this.filePersistence = new WorkspaceFilePersistence(DEFAULT_WORKSPACE_DIR);
        this.server = new CollaborativeServer(port, this, this.serializer);
        loadPersistedWorkspaceFiles();
    }

    private void loadPersistedWorkspaceFiles() {
        Map<String, String> storedFiles = this.filePersistence.loadAllFiles();
        for (Map.Entry<String, String> entry : storedFiles.entrySet()) {
            this.workspace.loadExistingFile(entry.getKey(), entry.getValue());
        }
    }

    public void startServer() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::stopServer));
        this.server.start();
    }

    public void stopServer() {
        this.server.stop();
    }

    @Override
    public void onClientMessage(ClientHandler handler, NetworkMessage message) {
        MessageType type = message.getType();
        if (type == MessageType.REGISTER_REQUEST) {
            handleRegister(handler, message.getSender());
        } else if (handler.getUsername() != null) {
            dispatchAuthenticatedMessage(handler, message);
        }
    }

    private void dispatchAuthenticatedMessage(ClientHandler handler, NetworkMessage message) {
        MessageType type = message.getType();
        if (type == MessageType.CREATE_FILE) {
            handleCreateFile(handler, message.getFileName(), message.getPayload());
        } else if (type == MessageType.SELECT_FILE) {
            handleSelectFile(handler, message.getFileName());
        } else if (type == MessageType.LOCK_LINE) {
            handleLockLine(handler, message.getFileName(), message.getLineNumber());
        } else if (type == MessageType.EDIT_LINE) {
            handleEditLine(handler, message.getFileName(), message.getPayload());
        } else if (type == MessageType.INSERT_LINE) {
            handleInsertLine(handler, message.getFileName(), message.getPayload());
        } else if (type == MessageType.DELETE_LINE) {
            handleDeleteLine(handler, message.getFileName());
        } else if (type == MessageType.UNLOCK_LINE) {
            handleUnlockLine(handler, message.getFileName());
        }
    }

    @Override
    public void onClientDisconnected(ClientHandler handler) {
        String username = handler.getUsername();
        if (username != null) {
            System.out.println("[DESCONEXION] Cliente desconectado: " + username
                    + " (IP: " + handler.getClientAddress() + ")");
            List<LockReference> releasedLocks = this.workspace.disconnectUser(username);
            broadcastUnlockedLocks(releasedLocks);
            notifyAdminReassignment();
            broadcastUserList();
        }
    }

    private void notifyAdminReassignment() {
        User newAdmin = this.workspace.reassignAdminRole();
        if (newAdmin != null) {
            NetworkMessage roleMsg = new NetworkMessage(
                    MessageType.ROLE_UPDATED, "", "", 0, newAdmin.getRole().name());
            this.server.sendToClient(newAdmin.getUsername(), roleMsg);
        }
    }

    @Override
    public void onInactivityCheck(long maxInactivityMillis) {
        List<LockReference> expiredLocks = this.workspace.releaseExpiredLocks(maxInactivityMillis);
        broadcastUnlockedLocks(expiredLocks);
    }

    private void handleRegister(ClientHandler handler, String username) {
        if (handler.getUsername() == null) {
            User registered = this.workspace.registerUser(username);
            if (registered != null) {
                handler.setUsername(registered.getUsername());
                System.out.println("[CONEXION] Cliente conectado: " + registered.getUsername()
                        + " (IP: " + handler.getClientAddress() + ")");
                String payload = registered.getColorId() + ";" + registered.getRole().name();
                NetworkMessage okMsg = new NetworkMessage(
                        MessageType.REGISTER_OK, registered.getUsername(), "", 0, payload);
                handler.sendMessage(okMsg);
                broadcastUserList();
                broadcastFileList();
            } else {
                sendRegisterError(handler);
            }
        }
    }

    private void sendRegisterError(ClientHandler handler) {
        String reason = this.workspace.hasAvailableColorSlots()
                ? "Nombre de usuario invalido o ya conectado."
                : "La sesion alcanzo el limite maximo de usuarios.";
        NetworkMessage errMsg = new NetworkMessage(MessageType.REGISTER_ERROR, "", "", 0, reason);
        handler.sendMessage(errMsg);
    }

    private void handleCreateFile(ClientHandler handler, String fileName, String content) {
        CodeFile created = this.workspace.createCodeFile(handler.getUsername(), fileName, content);
        if (created != null) {
            this.filePersistence.saveFile(created.getFileName(), created.buildFullSourceCode());
            broadcastFileList();
            broadcastFileContent(created.getFileName());
        } else {
            NetworkMessage errMsg = new NetworkMessage(
                    MessageType.ERROR_MESSAGE, "", fileName, 0,
                    "No se pudo crear el archivo. Verifique permisos de administrador o nombre .java unico.");
            handler.sendMessage(errMsg);
        }
    }

    private void handleSelectFile(ClientHandler handler, String fileName) {
        CodeFile file = this.workspace.findFileByName(fileName);
        if (file != null) {
            List<LockReference> released = this.workspace.unlockAllUserLines(handler.getUsername());
            broadcastUnlockedLocks(released);
            String formatted = this.serializer.formatFileLinesPayload(file);
            NetworkMessage contentMsg = new NetworkMessage(
                    MessageType.FILE_CONTENT, "", file.getFileName(), 0, formatted);
            handler.sendMessage(contentMsg);
        } else {
            NetworkMessage errMsg = new NetworkMessage(
                    MessageType.ERROR_MESSAGE, "", fileName, 0, "El archivo solicitado no existe.");
            handler.sendMessage(errMsg);
        }
    }

    private void handleLockLine(ClientHandler handler, String fileName, int lineNumber) {
        String username = handler.getUsername();
        if (this.workspace.canLockLine(fileName, lineNumber, username)) {
            List<LockReference> previousLocks = this.workspace.unlockAllUserLines(username);
            broadcastUnlockedLocks(previousLocks);
            boolean locked = this.workspace.lockLine(fileName, lineNumber, username);
            if (locked) {
                User owner = this.workspace.findUserByName(username);
                NetworkMessage lockMsg = new NetworkMessage(
                        MessageType.LINE_LOCKED, username, fileName, lineNumber,
                        String.valueOf(owner.getColorId()));
                this.server.broadcast(lockMsg);
            }
        } else {
            NetworkMessage rejectMsg = new NetworkMessage(
                    MessageType.LOCK_REJECTED, username, fileName, lineNumber,
                    "La linea esta siendo editada por otro usuario.");
            handler.sendMessage(rejectMsg);
        }
    }

    private void handleEditLine(ClientHandler handler, String fileName, String newText) {
        String username = handler.getUsername();
        boolean edited = this.workspace.editLine(fileName, newText, username);
        if (edited) {
            CodeFile file = this.workspace.findFileByName(fileName);
            CodeLine activeLine = file.findLineLockedBy(username);
            if (activeLine != null) {
                this.filePersistence.saveFile(file.getFileName(), file.buildFullSourceCode());
                NetworkMessage editMsg = new NetworkMessage(
                        MessageType.EDIT_LINE, username, fileName, activeLine.getLineNumber(), newText);
                this.server.broadcast(editMsg);
            }
        }
    }

    private void handleInsertLine(ClientHandler handler, String fileName, String text) {
        String username = handler.getUsername();
        boolean inserted = this.workspace.insertLine(fileName, text, username);
        if (inserted) {
            persistAndBroadcastFile(fileName);
        }
    }

    private void handleDeleteLine(ClientHandler handler, String fileName) {
        String username = handler.getUsername();
        boolean deleted = this.workspace.deleteLine(fileName, username);
        if (deleted) {
            persistAndBroadcastFile(fileName);
        }
    }

    private void handleUnlockLine(ClientHandler handler, String fileName) {
        LockReference released = this.workspace.unlockLine(fileName, handler.getUsername());
        if (released != null) {
            NetworkMessage unlockMsg = new NetworkMessage(
                    MessageType.LINE_UNLOCKED, released.getUsername(),
                    released.getFileName(), released.getLineNumber(), "");
            this.server.broadcast(unlockMsg);
        }
    }

    private void persistAndBroadcastFile(String fileName) {
        CodeFile file = this.workspace.findFileByName(fileName);
        if (file != null) {
            this.filePersistence.saveFile(file.getFileName(), file.buildFullSourceCode());
            broadcastFileContent(fileName);
        }
    }

    private void broadcastUnlockedLocks(List<LockReference> releasedLocks) {
        for (int i = 0; i < releasedLocks.size(); i++) {
            LockReference ref = releasedLocks.get(i);
            NetworkMessage unlockMsg = new NetworkMessage(
                    MessageType.LINE_UNLOCKED, ref.getUsername(),
                    ref.getFileName(), ref.getLineNumber(), "");
            this.server.broadcast(unlockMsg);
        }
    }

    private void broadcastUserList() {
        String payload = this.serializer.formatUsersPayload(this.workspace.getConnectedUsers());
        NetworkMessage usersMsg = new NetworkMessage(MessageType.USER_LIST_UPDATE, "", "", 0, payload);
        this.server.broadcast(usersMsg);
    }

    private void broadcastFileList() {
        String payload = this.serializer.formatFileListPayload(this.workspace.getFilesInOrder());
        NetworkMessage filesMsg = new NetworkMessage(MessageType.FILE_LIST_UPDATE, "", "", 0, payload);
        this.server.broadcast(filesMsg);
    }

    private void broadcastFileContent(String fileName) {
        CodeFile file = this.workspace.findFileByName(fileName);
        if (file != null) {
            String payload = this.serializer.formatFileLinesPayload(file);
            NetworkMessage contentMsg = new NetworkMessage(
                    MessageType.FILE_CONTENT, "", file.getFileName(), 0, payload);
            this.server.broadcast(contentMsg);
        }
    }
}
