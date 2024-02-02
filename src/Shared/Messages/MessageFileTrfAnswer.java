package Shared.Messages;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public class MessageFileTrfAnswer extends JsonMessage {
    @JsonProperty
    private String username;
    @JsonProperty
    private String answer;
    @JsonProperty
    private UUID uuid;

    public MessageFileTrfAnswer(@JsonProperty("username") String username,
                                @JsonProperty("answer") String answer, @JsonProperty("uuid") UUID uuid) {
        this.username = username;
        this.answer = answer;
        this.uuid = uuid;
    }
}