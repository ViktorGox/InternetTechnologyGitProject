package Shared.Messages.Bye;

import Shared.Messages.JsonMessage;
import com.fasterxml.jackson.annotation.JsonProperty;

public class MessageLeft extends JsonMessage {
    @JsonProperty
    String username;
    public MessageLeft(@JsonProperty("username") String username) {
        this.username = username;
    }
}