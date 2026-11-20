package co.edu.uptc.presenter;

import co.edu.uptc.network.ClientConnection;
import co.edu.uptc.network.ClientMessageListener;
import co.edu.uptc.network.MessageType;
import co.edu.uptc.network.NetworkMessage;
import co.edu.uptc.view.ConnectionFrame;
import co.edu.uptc.view.MainEditorFrame;
import co.edu.uptc.persistence.LocalJavaFilePersistence;
import co.edu.uptc.persistence.MessageSerializer;
import java.io.IOException;
import java.util.List;

public class ClientPresenter implements IClientPresenter, ClientMessageListener {
    private static final String ADMIN_ROLE = "ADMIN";

    private IConnectionView connectionView;
    private IEditorView editorView;
    private MessageSerializer serializer;
    private LocalJavaFilePersistence filePersistence;
    private ClientConnection connection;
    private String currentUsername;
    private int assignedColorId;
    private String currentFileName;
    private boolean admin;
    private boolean registered;

    public ClientPresenter(IConnectionView connectionView, IEditorView editorView) {
        this.connectionView = connectionView;
        this.editorView = editorView;
        this.serializer = new MessageSerializer();
        this.filePersistence = new LocalJavaFilePersistence();
        this.connection = new ClientConnection(this, this.serializer);
        this.currentUsername = "";
        this.assignedColorId = 0;
        this.currentFileName = "";
        this.admin = false;
        this.registered = false;
        this.connectionView.setPresenter(this);
        this.editorView.setPresenter(this);
    }

    public ClientPresenter() {
        this(new ConnectionFrame(), new MainEditorFrame());
    }

    public void start() {
        this.connectionView.showConnectionWindow();
    }

    @Override
    public void connectToServer(String host, int port, String username) {
        String cleanUser = username != null ? username.trim() : "";
        if (cleanUser.isEmpty()) {
            this.connectionView.showConnectionError("Debe ingresar un nombre de usuario.");
        } else {
            attemptSocketConnection(host, port, cleanUser);
        }
    }

    private void attemptSocketConnection(String host, int port, String username) {
        this.connectionView.setConnectEnabled(false);
        Thread connectThread = new Thread(() -> executeConnect(host, port, username), "ClientConnector");
        connectThread.setDaemon(true);
        connectThread.start();
    }

    private void executeConnect(String host, int port, String username) {
        this.connection = new ClientConnection(this, this.serializer);
        if (this.connection.connect(host, port)) {
            NetworkMessage registerMsg = new NetworkMessage(
                    MessageType.REGISTER_REQUEST, username, "", 0, "");
            this.connection.sendMessage(registerMsg);
        } else {
            this.connectionView.setConnectEnabled(true);
            this.connectionView.showConnectionError("No fue posible conectar con el servidor en " + host + ":" + port);
        }
    }

    @Override
    public void disconnectFromServer() {
        this.registered = false;
        this.connection.disconnect();
    }

    @Override
    public void createNewFile(String fileName) {
        String cleanName = normalizeJavaFileName(fileName);
        if (!cleanName.isEmpty()) {
            String initialTemplate = buildDefaultJavaTemplate(cleanName);
            NetworkMessage msg = new NetworkMessage(
                    MessageType.CREATE_FILE, this.currentUsername, cleanName, 0, initialTemplate);
            this.connection.sendMessage(msg);
        } else {
            this.editorView.showErrorDialog("Ingrese un nombre valido para el archivo .java.");
        }
    }

    private String normalizeJavaFileName(String rawName) {
        String result = "";
        if (rawName != null && !rawName.trim().isEmpty()) {
            String trimmed = rawName.trim();
            result = trimmed.endsWith(".java") ? trimmed : trimmed + ".java";
        }
        return result;
    }

    private String buildDefaultJavaTemplate(String fileName) {
        String className = fileName.substring(0, fileName.length() - 5);
        return "public class " + className + " {\n    public static void main(String[] args) {\n        \n    }\n}";
    }

    @Override
    public void importJavaFile(String filePath) {
        if (this.filePersistence.isValidJavaFile(filePath)) {
            try {
                String fileName = this.filePersistence.extractFileName(filePath);
                String content = this.filePersistence.readJavaFile(filePath);
                NetworkMessage msg = new NetworkMessage(
                        MessageType.CREATE_FILE, this.currentUsername, fileName, 0, content);
                this.connection.sendMessage(msg);
            } catch (IOException ex) {
                this.editorView.showErrorDialog("Error al leer el archivo: " + ex.getMessage());
            }
        } else {
            this.editorView.showErrorDialog("Solo se permiten archivos con extension .java.");
        }
    }

    @Override
    public void exportCurrentFile(String filePath) {
        if (this.currentFileName.isEmpty()) {
            this.editorView.showErrorDialog("No hay ningun archivo abierto para exportar.");
        } else {
            try {
                String fullCode = this.editorView.getFullDisplayedCode();
                this.filePersistence.writeJavaFile(filePath, fullCode);
                this.editorView.showStatusMessage("Archivo exportado correctamente: " + filePath);
            } catch (IOException ex) {
                this.editorView.showErrorDialog("Error al guardar el archivo: " + ex.getMessage());
            }
        }
    }

    @Override
    public void selectFile(String fileName) {
        if (fileName != null && !fileName.isEmpty()) {
            this.currentFileName = fileName;
            NetworkMessage msg = new NetworkMessage(
                    MessageType.SELECT_FILE, this.currentUsername, fileName, 0, "");
            this.connection.sendMessage(msg);
        }
    }

    @Override
    public void requestLockLine(int lineNumber) {
        if (!this.currentFileName.isEmpty() && lineNumber > 0) {
            NetworkMessage msg = new NetworkMessage(
                    MessageType.LOCK_LINE, this.currentUsername, this.currentFileName, lineNumber, "");
            this.connection.sendMessage(msg);
        }
    }

    @Override
    public void requestEditLine(String newContent) {
        if (!this.currentFileName.isEmpty()) {
            NetworkMessage msg = new NetworkMessage(
                    MessageType.EDIT_LINE, this.currentUsername, this.currentFileName, 0, newContent);
            this.connection.sendMessage(msg);
        }
    }

    @Override
    public void requestInsertLine(String text) {
        if (!this.currentFileName.isEmpty()) {
            NetworkMessage msg = new NetworkMessage(
                    MessageType.INSERT_LINE, this.currentUsername, this.currentFileName, 0, text);
            this.connection.sendMessage(msg);
        }
    }

    @Override
    public void requestDeleteLine() {
        if (!this.currentFileName.isEmpty()) {
            NetworkMessage msg = new NetworkMessage(
                    MessageType.DELETE_LINE, this.currentUsername, this.currentFileName, 0, "");
            this.connection.sendMessage(msg);
        }
    }

    @Override
    public void requestUnlockLine() {
        if (!this.currentFileName.isEmpty()) {
            NetworkMessage msg = new NetworkMessage(
                    MessageType.UNLOCK_LINE, this.currentUsername, this.currentFileName, 0, "");
            this.connection.sendMessage(msg);
        }
    }

    @Override
    public void onServerMessage(NetworkMessage message) {
        MessageType type = message.getType();
        if (type == MessageType.REGISTER_OK) {
            handleRegisterOk(message);
        } else if (type == MessageType.REGISTER_ERROR) {
            handleRegisterError(message);
        } else if (type == MessageType.USER_LIST_UPDATE) {
            List<String[]> users = this.serializer.parseUsersPayload(message.getPayload());
            this.editorView.updateConnectedUsers(users);
        } else if (type == MessageType.ROLE_UPDATED) {
            handleRoleUpdated(message);
        } else if (type == MessageType.FILE_LIST_UPDATE) {
            handleFileListUpdate(message);
        } else {
            dispatchEditorMessage(message);
        }
    }

    private void dispatchEditorMessage(NetworkMessage message) {
        MessageType type = message.getType();
        if (type == MessageType.FILE_CONTENT) {
            handleFileContent(message);
        } else if (type == MessageType.LINE_LOCKED) {
            handleLineLocked(message);
        } else if (type == MessageType.LOCK_REJECTED) {
            this.editorView.showStatusMessage(message.getPayload());
        } else if (type == MessageType.EDIT_LINE) {
            handleRemoteEditLine(message);
        } else if (type == MessageType.LINE_UNLOCKED) {
            handleLineUnlocked(message);
        } else if (type == MessageType.ERROR_MESSAGE) {
            this.editorView.showErrorDialog(message.getPayload());
        }
    }

    private void handleRegisterOk(NetworkMessage message) {
        String[] parts = message.getPayload().split(";", -1);
        this.currentUsername = message.getSender();
        this.assignedColorId = parseColorId(parts.length > 0 ? parts[0] : "0");
        this.admin = parts.length > 1 && ADMIN_ROLE.equals(parts[1]);
        this.registered = true;
        this.connectionView.closeConnectionWindow();
        this.editorView.showEditorWindow(this.currentUsername, this.assignedColorId, this.admin);
        this.editorView.showStatusMessage("Conectado como " + this.currentUsername);
    }

    private void handleRegisterError(NetworkMessage message) {
        this.connection.disconnect();
        this.connectionView.setConnectEnabled(true);
        this.connectionView.showConnectionError(message.getPayload());
    }

    private void handleRoleUpdated(NetworkMessage message) {
        this.admin = ADMIN_ROLE.equals(message.getPayload());
        this.editorView.setAdminPermissions(this.admin);
        if (this.admin) {
            this.editorView.showStatusMessage("Ahora eres el Administrador de la sesion.");
        }
    }

    private void handleFileListUpdate(NetworkMessage message) {
        List<String> files = this.serializer.parseFileListPayload(message.getPayload());
        this.editorView.updateFileList(files);
        if (this.currentFileName.isEmpty() && !files.isEmpty()) {
            selectFile(files.get(0));
        }
    }

    private void handleFileContent(NetworkMessage message) {
        if (this.currentFileName.isEmpty() || this.currentFileName.equals(message.getFileName())) {
            this.currentFileName = message.getFileName();
            List<String[]> lines = this.serializer.parseFileLinesPayload(message.getPayload());
            this.editorView.displayFileLines(this.currentFileName, lines, this.currentUsername);
        }
    }

    private void handleLineLocked(NetworkMessage message) {
        if (this.currentFileName.equals(message.getFileName())) {
            int colorId = parseColorId(message.getPayload());
            boolean ownedByLocal = this.currentUsername.equals(message.getSender());
            this.editorView.highlightLockedLine(
                    message.getLineNumber(), message.getSender(), colorId, ownedByLocal);
            this.editorView.showStatusMessage(
                    message.getSender() + " esta editando la linea " + message.getLineNumber());
        }
    }

    private void handleRemoteEditLine(NetworkMessage message) {
        boolean sameFile = this.currentFileName.equals(message.getFileName());
        boolean remoteSender = !this.currentUsername.equals(message.getSender());
        if (sameFile && remoteSender) {
            this.editorView.updateLineContent(message.getLineNumber(), message.getPayload());
        }
    }

    private void handleLineUnlocked(NetworkMessage message) {
        if (this.currentFileName.equals(message.getFileName())) {
            this.editorView.clearLineLock(message.getLineNumber());
            this.editorView.showStatusMessage("Linea " + message.getLineNumber() + " liberada.");
        }
    }

    @Override
    public void onConnectionLost(String reason) {
        if (this.registered) {
            this.registered = false;
            this.editorView.showErrorDialog(reason);
            this.editorView.closeEditorWindow();
            this.connectionView.setConnectEnabled(true);
            this.connectionView.showConnectionWindow();
        } else {
            this.connectionView.setConnectEnabled(true);
            this.connectionView.showConnectionError(reason);
        }
    }

    private int parseColorId(String rawId) {
        int colorId = 0;
        try {
            colorId = Integer.parseInt(rawId.trim());
        } catch (NumberFormatException ex) {
            colorId = 0;
        }
        return colorId;
    }
}
