package Messages;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MessageLogin extends JsonMessage{
    @JsonProperty
    private String username;

    public MessageLogin(@JsonProperty("username") String username) {
        this.username = username;
    }


}
