package Shared.Messages;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MessageGoodStatus extends JsonMessage{
    @JsonProperty
    String status;
    public MessageGoodStatus() {
        this.status = "OK";
    }
}