package Messages.Broadcast;

import Messages.JsonMessage;
import com.fasterxml.jackson.annotation.JsonProperty;

public class MessageBroadcast extends JsonMessage {
    @JsonProperty
    private String message;
    @JsonProperty
    private String username;
    public MessageBroadcast(@JsonProperty String username, @JsonProperty String message) {
        this.message = message;
    }
}