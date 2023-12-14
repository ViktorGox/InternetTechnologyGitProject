package Messages;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MessageError {
    @JsonProperty
    private String code;

    public MessageError(@JsonProperty("code") String code){
        this.code = code;
    }

}
