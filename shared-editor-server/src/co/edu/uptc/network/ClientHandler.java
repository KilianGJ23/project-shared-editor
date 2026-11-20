package co.edu.uptc.network;

import co.edu.uptc.persistence.MessageSerializer;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ClientHandler implements Runnable {
    private String clientAddress;
    private long handlerId;
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private String username;
    private boolean connected;
    private CollaborativeServer server;
    private ServerMessageListener listener;
    private MessageSerializer serializer;

    public ClientHandler(long handlerId, Socket socket, CollaborativeServer server,
            ServerMessageListener listener, MessageSerializer serializer) {
        this.handlerId = handlerId;
        this.socket = socket;
        this.server = server;
        this.listener = listener;
        this.serializer = serializer;
        this.username = null;
        this.clientAddress = resolveClientAddress(socket);
        this.connected = initStreams();
    }

    private boolean initStreams() {
        boolean ready = false;
        try {
            this.reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream(), StandardCharsets.UTF_8));
            this.writer = new PrintWriter(new OutputStreamWriter(this.socket.getOutputStream(), StandardCharsets.UTF_8), true);
            ready = true;
        } catch (IOException ex) {
            ready = false;
        }
        return ready;
    }

    @Override
    public void run() {
        try {
            processIncomingLines();
        } catch (IOException ex) {
            this.connected = false;
        } finally {
            cleanupSession();
        }
    }

    private void processIncomingLines() throws IOException {
        while (this.connected) {
            String rawLine = this.reader.readLine();
            if (rawLine == null) {
                this.connected = false;
            } else {
                dispatchRawLine(rawLine);
            }
        }
    }

    private void dispatchRawLine(String rawLine) {
        NetworkMessage message = this.serializer.deserialize(rawLine);
        if (message != null) {
            if (message.getType() == MessageType.DISCONNECT) {
                this.connected = false;
            } else if (this.listener != null) {
                this.listener.onClientMessage(this, message);
            }
        }
    }

    private void cleanupSession() {
        closeConnection();
        if (this.server != null) {
            this.server.removeClientHandler(this);
        }
        if (this.listener != null) {
            this.listener.onClientDisconnected(this);
        }
    }

    public synchronized void sendMessage(NetworkMessage message) {
        if (this.connected && this.writer != null && message != null) {
            this.writer.println(this.serializer.serialize(message));
        }
    }

    public synchronized void closeConnection() {
        this.connected = false;
        closeReader();
        if (this.writer != null) {
            this.writer.close();
        }
        closeSocket();
    }

    private void closeReader() {
        try {
            if (this.reader != null) {
                this.reader.close();
            }
        } catch (IOException ex) {
            this.reader = null;
        }
    }

    private void closeSocket() {
        try {
            if (this.socket != null && !this.socket.isClosed()) {
                this.socket.close();
            }
        } catch (IOException ex) {
            this.socket = null;
        }
    }

    private String resolveClientAddress(Socket clientSocket) {
        String address = "desconocida";
        if (clientSocket != null && clientSocket.getInetAddress() != null) {
            address = clientSocket.getInetAddress().getHostAddress();
        }
        return address;
    }

    public String getClientAddress() {
        return this.clientAddress;
    }
    
    public long getHandlerId() {
        return this.handlerId;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isConnected() {
        return this.connected;
    }
}
