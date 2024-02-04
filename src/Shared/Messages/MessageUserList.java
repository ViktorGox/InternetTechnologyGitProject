package Shared.Messages;

import Server.ServerSideClient;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Set;

public class MessageUserList extends JsonMessage{
    @JsonProperty
    List<String> users;

    @JsonCreator
    public MessageUserList(@JsonProperty("users") List<String> serverSideClients) {
        this.users = serverSideClients;
    }
}
