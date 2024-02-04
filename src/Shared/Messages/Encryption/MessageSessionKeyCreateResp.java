package Shared.Messages.Encryption;

import Shared.Messages.JsonMessage;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class MessageSessionKeyCreateResp extends JsonMessage {
    @JsonProperty
    String status;
    @JsonProperty
    String username;
    @JsonCreator
    public MessageSessionKeyCreateResp(@JsonProperty("status") String status, @JsonProperty("username") String username) {
        this.status = status;
        this.username = username;
    }

    public String getStatus() {
        return status;
    }

    public String getUsername() {
        return username;
    }
}