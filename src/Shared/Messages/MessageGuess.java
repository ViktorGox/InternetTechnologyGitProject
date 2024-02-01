package Shared.Messages;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MessageGuess extends JsonMessage{
    @JsonProperty
    String guess;
    public MessageGuess(@JsonProperty("guess") String guess) {
        this.guess = guess;
    }
}