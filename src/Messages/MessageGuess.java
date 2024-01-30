package Messages;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MessageGuess extends JsonMessage{
    @JsonProperty
    int guess;
    public MessageGuess(@JsonProperty("guess") int guess) {
        this.guess = guess;
    }
}
