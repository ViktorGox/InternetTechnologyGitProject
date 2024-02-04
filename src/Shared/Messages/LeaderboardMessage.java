package Shared.Messages;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class LeaderboardMessage extends JsonMessage {
    @JsonProperty
    private Map<String, Long> leaderboard;

    public LeaderboardMessage(@JsonProperty("leaderboard") Map<String, Long> leaderboard) {
        this.leaderboard = leaderboard;
    }
}