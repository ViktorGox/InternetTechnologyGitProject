package Shared.Messages;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MessageFileTrfAnswer extends JsonMessage {
    @JsonProperty
    private String username;
    @JsonProperty
    private String answer;

    public MessageFileTrfAnswer(@JsonProperty("username") String username,
                                @JsonProperty("answer") String answer) {
        this.username = username;
        this.answer = answer;
    }
}