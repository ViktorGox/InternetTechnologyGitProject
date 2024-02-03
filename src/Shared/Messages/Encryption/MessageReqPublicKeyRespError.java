package Shared.Messages.Encryption;

import Shared.Messages.JsonMessage;
import com.fasterxml.jackson.annotation.JsonProperty;

public class MessageReqPublicKeyRespError extends JsonMessage {
    @JsonProperty
    String status = "Error";
    @JsonProperty
    String code;
    @JsonProperty
    String username;

    public MessageReqPublicKeyRespError(@JsonProperty("code") String code, @JsonProperty("username") String username) {
        this.code = code;
        this.username = username;
    }
}
