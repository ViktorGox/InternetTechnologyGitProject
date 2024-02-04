package Shared.Messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class MessageGuess extends JsonMessage{
    @JsonProperty
    String guess;
    @JsonCreator
    public MessageGuess(@JsonProperty("guess") String guess) {
        this.guess = guess;
    }
    public String getGuess() {
        return guess;
    }
}