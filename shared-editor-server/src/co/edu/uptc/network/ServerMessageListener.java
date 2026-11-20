package co.edu.uptc.network;

public interface ServerMessageListener {
    void onClientMessage(ClientHandler handler, NetworkMessage message);

    void onClientDisconnected(ClientHandler handler);

    void onInactivityCheck(long maxInactivityMillis);
}
