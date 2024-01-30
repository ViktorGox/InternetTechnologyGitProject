package Messages;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MessageInvite extends JsonMessage{
    @JsonProperty
    private String invite = "Invited";
    public MessageInvite() {}
}

