package Shared.Messages;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class LeaderboardMessage extends JsonMessage {
    @JsonProperty
    private Map<String, Long> leaderboard;

    public LeaderboardMessage(@JsonProperty("leaderboard") Map<String, Long> leaderboard) {
        this.leaderboard = leaderboard;
    }

    public Map<String, Long> getLeaderboard() {
        return leaderboard;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("LEADERBOARD\n");
        int index = 1;
        for (Map.Entry<String, Long> entry : leaderboard.entrySet()) {
            sb.append(index).append(". ")
                    .append(entry.getKey()).append("-").append(entry.getValue()).append(" ms\n");
            index++;
        }
        return sb.toString();
    }


}