package Shared.Messages.Broadcast;

import Shared.Messages.JsonMessage;
import com.fasterxml.jackson.annotation.JsonProperty;

public class MessageBroadcast extends JsonMessage {
    @JsonProperty
    private String username;
    @JsonProperty
    private String message;

    public MessageBroadcast(@JsonProperty String username, @JsonProperty String message) {
        this.username = username;
        this.message = message;
    }
}