package co.edu.uptc.network;

public class NetworkMessage {
    private MessageType type;
    private String sender;
    private String fileName;
    private int lineNumber;
    private String payload;

    public NetworkMessage(MessageType type, String sender, String fileName, int lineNumber, String payload) {
        this.type = type;
        this.sender = sender != null ? sender : "";
        this.fileName = fileName != null ? fileName : "";
        this.lineNumber = lineNumber;
        this.payload = payload != null ? payload : "";
    }

    public MessageType getType() {
        return this.type;
    }

    public String getSender() {
        return this.sender;
    }

    public String getFileName() {
        return this.fileName;
    }

    public int getLineNumber() {
        return this.lineNumber;
    }

    public String getPayload() {
        return this.payload;
    }
}
