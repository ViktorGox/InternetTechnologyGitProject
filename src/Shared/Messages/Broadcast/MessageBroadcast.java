package Shared.Messages.Broadcast;

import Shared.Messages.JsonMessage;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class MessageBroadcast extends JsonMessage {
    @JsonProperty
    private String username;
    @JsonProperty
    private String message;

    @JsonCreator
    public MessageBroadcast(@JsonProperty("username") String username, @JsonProperty("message") String message) {
        this.username = username;
        this.message = message;
    }

    public String getUsername() {
        return username;
    }
    public String getMessage() {
        return message;
    }
}