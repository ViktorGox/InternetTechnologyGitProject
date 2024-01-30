package Messages;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class LeaderboardMessage extends JsonMessage {
    @JsonProperty
    private Map<String, Long> leaderbaord;

    public LeaderboardMessage(@JsonProperty("leaderboard") Map<String, Long> leaderbaord) {
        this.leaderbaord = leaderbaord;
    }
}
