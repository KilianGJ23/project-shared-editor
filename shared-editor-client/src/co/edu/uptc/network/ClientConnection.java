package co.edu.uptc.network;

import co.edu.uptc.persistence.MessageSerializer;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ClientConnection implements Runnable {
    private static final int CONNECT_TIMEOUT_MILLIS = 5000;

    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private boolean connected;
    private ClientMessageListener listener;
    private MessageSerializer serializer;

    public ClientConnection(ClientMessageListener listener, MessageSerializer serializer) {
        this.listener = listener;
        this.serializer = serializer;
        this.connected = false;
    }

    public boolean connect(String host, int port) {
        boolean opened = false;
        try {
            this.socket = new Socket();
            this.socket.connect(new InetSocketAddress(host, port), CONNECT_TIMEOUT_MILLIS);
            this.reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream(), StandardCharsets.UTF_8));
            this.writer = new PrintWriter(new OutputStreamWriter(this.socket.getOutputStream(), StandardCharsets.UTF_8), true);
            this.connected = true;
            Thread listenerThread = new Thread(this, "ClientSocketListener");
            listenerThread.setDaemon(true);
            listenerThread.start();
            opened = true;
        } catch (IOException ex) {
            this.connected = false;
            closeResources();
        }
        return opened;
    }

    @Override
    public void run() {
        String lostReason = null;
        try {
            while (this.connected) {
                String rawLine = this.reader.readLine();
                if (rawLine == null) {
                    this.connected = false;
                    lostReason = "El servidor cerro la conexion.";
                } else {
                    NetworkMessage message = this.serializer.deserialize(rawLine);
                    if (message != null && this.listener != null) {
                        this.listener.onServerMessage(message);
                    }
                }
            }
        } catch (IOException ex) {
            if (this.connected) {
                lostReason = "Se perdio la conexion con el servidor.";
            }
            this.connected = false;
        } finally {
            closeResources();
            if (lostReason != null && this.listener != null) {
                this.listener.onConnectionLost(lostReason);
            }
        }
    }

    public synchronized void sendMessage(NetworkMessage message) {
        if (this.connected && this.writer != null && message != null) {
            this.writer.println(this.serializer.serialize(message));
        }
    }

    public synchronized void disconnect() {
        if (this.connected) {
            sendMessage(new NetworkMessage(MessageType.DISCONNECT, "", "", 0, ""));
            this.connected = false;
            closeResources();
        }
    }

    private void closeResources() {
        try {
            if (this.reader != null) {
                this.reader.close();
            }
        } catch (IOException ex) {
            this.reader = null;
        }
        if (this.writer != null) {
            this.writer.close();
        }
        try {
            if (this.socket != null && !this.socket.isClosed()) {
                this.socket.close();
            }
        } catch (IOException ex) {
            this.socket = null;
        }
    }

    public boolean isConnected() {
        return this.connected;
    }
}
