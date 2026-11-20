package co.edu.uptc.persistence;

import co.edu.uptc.network.MessageType;
import co.edu.uptc.network.NetworkMessage;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class MessageSerializer {
    private static final String FIELD_SEPARATOR = "|";
    private static final String FIELD_SPLIT_REGEX = "\\|";
    private static final int EXPECTED_MESSAGE_PARTS = 5;
    private static final int EXPECTED_LINE_PARTS = 4;

    public String serialize(NetworkMessage message) {
        Base64.Encoder encoder = Base64.getEncoder();
        String encodedSender = encode(encoder, message.getSender());
        String encodedFile = encode(encoder, message.getFileName());
        String encodedPayload = encode(encoder, message.getPayload());
        return message.getType().name()
                + FIELD_SEPARATOR + encodedSender
                + FIELD_SEPARATOR + encodedFile
                + FIELD_SEPARATOR + message.getLineNumber()
                + FIELD_SEPARATOR + encodedPayload;
    }

    public NetworkMessage deserialize(String rawMessage) {
        NetworkMessage message = null;
        if (rawMessage != null && !rawMessage.isEmpty()) {
            String[] parts = rawMessage.split(FIELD_SPLIT_REGEX, -1);
            if (parts.length == EXPECTED_MESSAGE_PARTS) {
                message = buildMessageFromParts(parts);
            }
        }
        return message;
    }

    private NetworkMessage buildMessageFromParts(String[] parts) {
        NetworkMessage message = null;
        try {
            Base64.Decoder decoder = Base64.getDecoder();
            MessageType type = MessageType.valueOf(parts[0]);
            String sender = decode(decoder, parts[1]);
            String fileName = decode(decoder, parts[2]);
            int lineNumber = Integer.parseInt(parts[3]);
            String payload = decode(decoder, parts[4]);
            message = new NetworkMessage(type, sender, fileName, lineNumber, payload);
        } catch (IllegalArgumentException ex) {
            message = null;
        }
        return message;
    }

    public List<String[]> parseUsersPayload(String payload) {
        List<String[]> users = new ArrayList<>();
        if (payload != null && !payload.isEmpty()) {
            String[] rows = payload.split("\\r?\\n");
            for (int i = 0; i < rows.length; i++) {
                String[] parts = rows[i].split(";", -1);
                if (parts.length == 3) {
                    users.add(parts);
                }
            }
        }
        return users;
    }

    public List<String> parseFileListPayload(String payload) {
        List<String> fileNames = new ArrayList<>();
        if (payload != null && !payload.isEmpty()) {
            String[] rows = payload.split("\\r?\\n");
            for (int i = 0; i < rows.length; i++) {
                if (!rows[i].trim().isEmpty()) {
                    fileNames.add(rows[i].trim());
                }
            }
        }
        return fileNames;
    }

    public List<String[]> parseFileLinesPayload(String payload) {
        List<String[]> decodedLines = new ArrayList<>();
        if (payload != null && !payload.isEmpty()) {
            Base64.Decoder decoder = Base64.getDecoder();
            String[] rows = payload.split("\\r?\\n", -1);
            for (int i = 0; i < rows.length; i++) {
                String[] parts = rows[i].split(FIELD_SPLIT_REGEX, -1);
                if (parts.length == EXPECTED_LINE_PARTS) {
                    String content = decode(decoder, parts[3]);
                    decodedLines.add(new String[] {parts[0], parts[1], parts[2], content});
                }
            }
        }
        return decodedLines;
    }

    private String encode(Base64.Encoder encoder, String value) {
        return encoder.encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private String decode(Base64.Decoder decoder, String encoded) {
        return new String(decoder.decode(encoded), StandardCharsets.UTF_8);
    }
}
