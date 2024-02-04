package Shared.Messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class MessageInvite extends JsonMessage{
    @JsonProperty
    private String username;
    @JsonCreator
    public MessageInvite(@JsonProperty("username") String username) {
        this.username = username;
    }
}