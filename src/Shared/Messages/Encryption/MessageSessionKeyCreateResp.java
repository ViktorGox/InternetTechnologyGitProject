package Shared.Messages.Encryption;

import Shared.Messages.JsonMessage;
import com.fasterxml.jackson.annotation.JsonProperty;

public class MessageSessionKeyCreateResp extends JsonMessage {
    @JsonProperty
    String status;
    @JsonProperty
    String username;

    public MessageSessionKeyCreateResp(@JsonProperty("status") String status, @JsonProperty("username") String username) {
        this.status = status;
        this.username = username;
    }
}