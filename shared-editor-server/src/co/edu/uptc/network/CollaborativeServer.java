package co.edu.uptc.network;

import co.edu.uptc.persistence.MessageSerializer;
import co.edu.uptc.structures.MyBinaryTreeAVL;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class CollaborativeServer {
    private static final long CHECK_INTERVAL_MILLIS = 5000L;
    private static final long MAX_INACTIVITY_MILLIS = 30000L;

    private int port;
    private ServerSocket serverSocket;
    private boolean running;
    private MyBinaryTreeAVL<ClientHandler> clients;
    private AtomicLong handlerSequence;
    private ServerMessageListener messageListener;
    private MessageSerializer serializer;
    private InactivityMonitor inactivityMonitor;

    public CollaborativeServer(int port, ServerMessageListener listener, MessageSerializer serializer) {
        this.port = port;
        this.messageListener = listener;
        this.serializer = serializer;
        this.clients = new MyBinaryTreeAVL<>(Comparator.comparingLong(ClientHandler::getHandlerId));
        this.handlerSequence = new AtomicLong(1L);
        this.running = false;
        this.serverSocket = null;
        this.inactivityMonitor = null;
    }

    public void start() {
        try {
            this.serverSocket = new ServerSocket(this.port);
            this.running = true;
            System.out.println("[SERVIDOR] Encendido y escuchando en el puerto " + this.port);
            startInactivityMonitor();
            acceptClientsLoop();
        } catch (IOException ex) {
            this.running = false;
            System.out.println("[SERVIDOR] Error al iniciar en el puerto " + this.port + ": " + ex.getMessage());
        }
    }

    private void startInactivityMonitor() {
        this.inactivityMonitor = new InactivityMonitor(
                CHECK_INTERVAL_MILLIS, MAX_INACTIVITY_MILLIS, this.messageListener);
        Thread monitorThread = new Thread(this.inactivityMonitor, "InactivityMonitor");
        monitorThread.setDaemon(true);
        monitorThread.start();
    }

    private void acceptClientsLoop() {
        while (this.running) {
            try {
                Socket clientSocket = this.serverSocket.accept();
                long id = this.handlerSequence.getAndIncrement();
                ClientHandler handler = new ClientHandler(
                        id, clientSocket, this, this.messageListener, this.serializer);
                if (handler.isConnected()) {
                    registerClientHandler(handler);
                    Thread clientThread = new Thread(handler, "ClientHandler-" + id);
                    clientThread.start();
                }
            } catch (IOException ex) {
                if (this.serverSocket.isClosed()) {
                    this.running = false;
                }
            }
        }
    }

    public synchronized void stop() {
        if (this.running) {
            this.running = false;
            if (this.inactivityMonitor != null) {
                this.inactivityMonitor.stop();
            }
            closeAllClients();
            closeServerSocket();
            System.out.println("[SERVIDOR] Servidor finalizado correctamente.");
        }
    }

    private void closeAllClients() {
        synchronized (this.clients) {
            List<ClientHandler> activeClients = this.clients.inOrder();
            for (int i = 0; i < activeClients.size(); i++) {
                activeClients.get(i).closeConnection();
            }
            this.clients = new MyBinaryTreeAVL<>(Comparator.comparingLong(ClientHandler::getHandlerId));
        }
    }

    private void closeServerSocket() {
        try {
            if (this.serverSocket != null && !this.serverSocket.isClosed()) {
                this.serverSocket.close();
            }
        } catch (IOException ex) {
            this.serverSocket = null;
        }
    }

    public void broadcast(NetworkMessage message) {
        synchronized (this.clients) {
            List<ClientHandler> activeClients = this.clients.inOrder();
            for (int i = 0; i < activeClients.size(); i++) {
                ClientHandler client = activeClients.get(i);
                if (client.getUsername() != null && client.isConnected()) {
                    client.sendMessage(message);
                }
            }
        }
    }

    public void sendToClient(String username, NetworkMessage message) {
        if (username != null) {
            synchronized (this.clients) {
                List<ClientHandler> activeClients = this.clients.inOrder();
                boolean sent = false;
                int index = 0;
                while (index < activeClients.size() && !sent) {
                    ClientHandler client = activeClients.get(index);
                    if (username.equals(client.getUsername()) && client.isConnected()) {
                        client.sendMessage(message);
                        sent = true;
                    }
                    index++;
                }
            }
        }
    }

    public void registerClientHandler(ClientHandler handler) {
        synchronized (this.clients) {
            this.clients.add(handler);
        }
    }

    public void removeClientHandler(ClientHandler handler) {
        synchronized (this.clients) {
            this.clients.remove(handler);
        }
    }

    public boolean isRunning() {
        return this.running;
    }
}
