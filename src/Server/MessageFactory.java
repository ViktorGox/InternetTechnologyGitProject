package Server;

import Shared.ClientCommand;
import Shared.Messages.JsonMessage;
import Shared.Messages.JsonMessageExtractor;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

public class MessageFactory {
    public static <T extends JsonMessage> T convertToMessageClass(ClientCommand received, Class<T> messageType) {
        Map<String, String> message = null;
        if (received.getMessage() == null) {
            return null;
        }
        message = JsonMessageExtractor.extractInformation(received.getMessage());
        if (message.get("status") != null) {
            return handleOther(message);
        }
        // convert to class
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.convertValue(message, messageType);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static <T extends JsonMessage> T handleOther(Map<String, String> stringedJson) {
        return null;
    }
}
