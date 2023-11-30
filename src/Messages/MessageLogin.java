package Messages;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MessageLogin {
    @JsonProperty
    private String username;

    public MessageLogin(@JsonProperty("username") String username) {
        this.username = username;
    }

    public String mapToJson() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(this);
    }
}
