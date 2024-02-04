package Shared.Messages;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MessageStatuslessError extends JsonMessage{
    @JsonProperty
    private String code;

    public MessageStatuslessError(@JsonProperty("code") String code){
        this.code = code;
    }
    public String getCode() {
        return code;
    }
}