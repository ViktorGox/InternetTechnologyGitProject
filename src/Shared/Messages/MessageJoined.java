package Shared.Messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class MessageJoined extends JsonMessage {
    @JsonProperty
    private String username;
    @JsonCreator
    public MessageJoined(@JsonProperty("username") String username) {
        this.username = username;
    }
}