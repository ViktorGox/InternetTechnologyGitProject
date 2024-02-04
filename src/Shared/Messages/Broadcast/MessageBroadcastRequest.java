package Shared.Messages.Broadcast;

import Shared.Messages.JsonMessage;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class MessageBroadcastRequest extends JsonMessage {
    @JsonProperty
    private String message;

    @JsonCreator
    public MessageBroadcastRequest(@JsonProperty("message") String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}