package Shared.Messages.Encryption;

import Shared.EncryptionUtils;
import Shared.Messages.JsonMessage;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.Arrays;

public class MessageReqPublicKeyResp extends JsonMessage {
    @JsonProperty
    byte[] publicKey;
    @JsonProperty
    String username;

    public MessageReqPublicKeyResp(@JsonProperty("publicKey") byte[] publicKey, @JsonProperty("username") String username) {
        this.publicKey = publicKey;
        this.username = username;
    }

    public MessageReqPublicKeyResp(@JsonProperty("publicKey") String publicKey, @JsonProperty("username") String username) {
        this.publicKey = EncryptionUtils.stringByteArrayToByteArray(publicKey);
        this.username = username;
    }

    @Override
    public String mapToJson() {
        String result = "{\"publicKey\":\"" + Arrays.toString(publicKey) + "\", \"username\":\"" + username + "\"}";
        System.out.println("Manually mapped to Json: " + result);
        return result;
    }
}