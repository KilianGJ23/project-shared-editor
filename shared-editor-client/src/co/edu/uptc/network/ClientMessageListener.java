package co.edu.uptc.network;

public interface ClientMessageListener {
    void onServerMessage(NetworkMessage message);

    void onConnectionLost(String reason);
}
