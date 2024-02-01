package Shared.Messages;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MessageLogin extends JsonMessage{
    @JsonProperty
    private String username;
    public MessageLogin(@JsonProperty("username") String username) {
        this.username = username;
    }
}