package Shared.Messages.Encryption;

import Shared.Messages.JsonMessage;
import com.fasterxml.jackson.annotation.JsonProperty;

public class MessageReqPublicKeyResp extends JsonMessage {
    @JsonProperty
    String publicKey;
    @JsonProperty
    String username;
    public MessageReqPublicKeyResp(@JsonProperty("public_key") String publicKey, @JsonProperty("username") String username) {
        this.publicKey = publicKey;
        this.username = username;
    }
}