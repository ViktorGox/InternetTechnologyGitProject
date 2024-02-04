package Shared.Messages;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MessageError extends JsonMessage{
    @JsonProperty
    private String code;
    @JsonProperty
    private String status = "ERROR";
    public MessageError(@JsonProperty("code") String code){
        this.code = code;
    }
}