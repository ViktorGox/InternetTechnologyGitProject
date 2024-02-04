package Shared.Messages.Encryption;

import Shared.EncryptionUtils;
import Shared.Messages.JsonMessage;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;

public class MessageSessionKeyCreate extends JsonMessage {
    @JsonProperty
    private byte[] sessionKey;
    @JsonProperty
    private String username;

    public String getSessionKey() {
        return Arrays.toString(sessionKey);
    }

    public String getUsername() {
        return username;
    }

    public MessageSessionKeyCreate(@JsonProperty("session_key") byte[] sessionKey, @JsonProperty("username") String username) {
        this.sessionKey = sessionKey;
        this.username = username;
    }

    @JsonCreator
    public MessageSessionKeyCreate(@JsonProperty("session_key") String sessionKey, @JsonProperty("username") String username) {
        this.sessionKey = EncryptionUtils.stringByteArrayToByteArray(sessionKey);
        this.username = username;
    }

    @Override
    public String mapToJson() {
        return "{\"session_key\":\"" + Arrays.toString(sessionKey) + "\", \"username\":\"" + username + "\"}";
    }

}