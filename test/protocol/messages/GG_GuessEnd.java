package protocol.messages;

import java.util.Map;

public record GG_GuessEnd(Map<String, Long> leaderboard) {
}
