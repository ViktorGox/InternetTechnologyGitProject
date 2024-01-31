package Shared.Messages.Encryption;

import Shared.Messages.JsonMessage;
import com.fasterxml.jackson.annotation.JsonProperty;

public class MessageReqPublicKey extends JsonMessage {
    @JsonProperty
    String username;
    public MessageReqPublicKey(@JsonProperty("username") String username) {
        this.username = username;
    }
}