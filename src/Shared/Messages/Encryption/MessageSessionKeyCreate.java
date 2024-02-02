package Shared.Messages.Encryption;

import Shared.EncryptionUtils;
import Shared.Messages.JsonMessage;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;

public class MessageSessionKeyCreate extends JsonMessage {
    @JsonProperty
    byte[] sessionKey;
    @JsonProperty
    String username;

    public MessageSessionKeyCreate(@JsonProperty("session_key") byte[] sessionKey, @JsonProperty("username") String username) {
        this.sessionKey = sessionKey;
        this.username = username;
    }

    public MessageSessionKeyCreate(@JsonProperty("session_key") String sessionKey, @JsonProperty("username") String username) {
        this.sessionKey = EncryptionUtils.stringByteArrayToByteArray(sessionKey);
        this.username = username;
    }

    @Override
    public String mapToJson() {
        return "{\"session_key\":\"" + Arrays.toString(sessionKey) + "\", \"username\":\"" + username + "\"}";
    }
}