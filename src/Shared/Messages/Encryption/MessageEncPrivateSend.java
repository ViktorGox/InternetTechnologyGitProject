package Shared.Messages.Encryption;

import Shared.Messages.JsonMessage;
import com.fasterxml.jackson.annotation.JsonProperty;

public class MessageEncPrivateSend extends JsonMessage {
    @JsonProperty
    private String username;
    @JsonProperty
    private String message;

    public MessageEncPrivateSend(@JsonProperty String username, @JsonProperty String message) {
        this.username = username;
        this.message = message;
    }
}