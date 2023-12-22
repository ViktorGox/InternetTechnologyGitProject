package Messages;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MessageGuess extends JsonMessage{
    @JsonProperty
    int guess;
    public MessageGuess(@JsonProperty("message") int guess) {
        this.guess = guess;
    }
}
