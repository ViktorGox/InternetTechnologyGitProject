package protocol.messages;

import java.util.List;

public record UserListResp(List<String> users, String status, int code) {
}
