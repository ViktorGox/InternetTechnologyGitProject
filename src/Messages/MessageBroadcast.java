package Messages;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MessageBroadcast extends MessageLogin{
    @JsonProperty
    private String message;

    public MessageBroadcast(@JsonProperty String username, @JsonProperty String message) {
        super(username);
        this.message = message;
    }
}
