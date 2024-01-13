package Messages;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MessageFileTransfer extends JsonMessage{
    @JsonProperty private String username;
    @JsonProperty private String fileName;
    public MessageFileTransfer(@JsonProperty("username") String username,
                               @JsonProperty("fileName") String fileName) {
        this.username = username;
        this.fileName = fileName;
    }
}
