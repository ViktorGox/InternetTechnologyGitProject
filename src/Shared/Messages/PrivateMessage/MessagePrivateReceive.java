package Shared.Messages.PrivateMessage;

import Shared.Messages.JsonMessage;
import com.fasterxml.jackson.annotation.JsonProperty;

public class MessagePrivateReceive extends JsonMessage {
    @JsonProperty
    private String username;
    @JsonProperty
    private String message;

    public MessagePrivateReceive(@JsonProperty String username, @JsonProperty String message) {
        this.username = username;
        this.message = message;
    }
}