package Shared.Messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class MessageFileTransfer extends JsonMessage{
    @JsonProperty private String username;
    @JsonProperty private String fileName;
    @JsonCreator

    public MessageFileTransfer(@JsonProperty("username") String username,
                               @JsonProperty("fileName") String fileName) {
        this.username = username;
        this.fileName = fileName;
    }

    public String getUsername() {
        return username;
    }

    public String getFileName() {
        return fileName;
    }
}