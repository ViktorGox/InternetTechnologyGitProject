package Messages.PrivateMessage;

import Messages.JsonMessage;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PrivateReceiveMessage extends JsonMessage {
    @JsonProperty
    private String username;
    @JsonProperty
    private String message;

    public PrivateReceiveMessage(@JsonProperty String username, @JsonProperty String message) {
        this.username = username;
        this.message = message;
    }
}
