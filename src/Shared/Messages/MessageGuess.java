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

    public String toString() {
        if (guess.equals("0")) {
            return "Well Done! You have guessed the number, wait for the leaderboard to see where you ranked up";
        } else if (guess.equals("1")) {
            return "Guess too high! Try a lower one";
        } else {
            return "Guess too low! Try a higher one";
        }
    }
}