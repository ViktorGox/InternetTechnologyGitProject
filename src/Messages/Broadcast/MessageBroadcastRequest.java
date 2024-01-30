package Messages.Broadcast;

import Messages.JsonMessage;
import com.fasterxml.jackson.annotation.JsonProperty;

public class MessageBroadcastRequest extends JsonMessage {
    @JsonProperty
    private String message;
    public MessageBroadcastRequest(@JsonProperty String message) {
        this.message = message;
    }
}
