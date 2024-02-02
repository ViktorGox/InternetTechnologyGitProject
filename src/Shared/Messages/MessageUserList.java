package Shared.Messages;

import Server.ServerSideClient;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Set;

public class MessageUserList extends JsonMessage{
    @JsonProperty
    Set<ServerSideClient> serverSideClients;

    public MessageUserList(Set<ServerSideClient> serverSideClients) {
        this.serverSideClients = serverSideClients;
    }
}
