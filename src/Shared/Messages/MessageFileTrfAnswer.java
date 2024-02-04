package Shared.Messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public class MessageFileTrfAnswer extends JsonMessage {
    @JsonProperty
    private String username;
    @JsonProperty
    private String answer;
    @JsonProperty
    private UUID uuid;
    @JsonCreator

    public MessageFileTrfAnswer(@JsonProperty("username") String username,
                                @JsonProperty("answer") String answer, @JsonProperty("uuid") UUID uuid) {
        this.username = username;
        this.answer = answer;
        this.uuid = uuid;
    }

    public String getUsername() {
        return username;
    }

    public String getAnswer() {
        return answer;
    }

    public UUID getUuid() {
        return uuid;
    }
}