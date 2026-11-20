package co.edu.uptc.persistence;

import co.edu.uptc.model.CodeFile;
import co.edu.uptc.model.CodeLine;
import co.edu.uptc.model.User;
import co.edu.uptc.network.MessageType;
import co.edu.uptc.network.NetworkMessage;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

public class MessageSerializer {
    private static final String FIELD_SEPARATOR = "|";
    private static final String FIELD_SPLIT_REGEX = "\\|";
    private static final int EXPECTED_PARTS = 5;

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
            if (parts.length == EXPECTED_PARTS) {
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

    public String formatUsersPayload(List<User> users) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            if (i > 0) {
                builder.append("\n");
            }
            builder.append(user.getUsername())
                    .append(";")
                    .append(user.getColorId())
                    .append(";")
                    .append(user.getRole().name());
        }
        return builder.toString();
    }

    public String formatFileListPayload(List<CodeFile> files) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < files.size(); i++) {
            if (i > 0) {
                builder.append("\n");
            }
            builder.append(files.get(i).getFileName());
        }
        return builder.toString();
    }

    public String formatFileLinesPayload(CodeFile file) {
        List<CodeLine> lines = file.getOrderedLines();
        Base64.Encoder encoder = Base64.getEncoder();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < lines.size(); i++) {
            CodeLine line = lines.get(i);
            if (i > 0) {
                builder.append("\n");
            }
            String owner = line.getActiveUsername() != null ? line.getActiveUsername() : "";
            String encodedContent = encode(encoder, line.getContent());
            builder.append(line.getLineNumber())
                    .append(FIELD_SEPARATOR)
                    .append(owner)
                    .append(FIELD_SEPARATOR)
                    .append(line.getActiveColorId())
                    .append(FIELD_SEPARATOR)
                    .append(encodedContent);
        }
        return builder.toString();
    }

    private String encode(Base64.Encoder encoder, String value) {
        return encoder.encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private String decode(Base64.Decoder decoder, String encoded) {
        return new String(decoder.decode(encoded), StandardCharsets.UTF_8);
    }
}
