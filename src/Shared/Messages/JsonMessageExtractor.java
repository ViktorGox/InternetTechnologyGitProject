package Shared.Messages;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

public class JsonMessageExtractor {

    public static Map<String, String> extractInformation(String jsonMessage) {
        Map<String, String> values = new HashMap<>();

        int openingBracketIndex = jsonMessage.indexOf('{');
        int closingBracketIndex = jsonMessage.lastIndexOf('}');

        if (openingBracketIndex != -1 && closingBracketIndex != -1) {
            String jsonString = jsonMessage.substring(openingBracketIndex, closingBracketIndex + 1);

            try {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(jsonString);

                jsonNode.fields().forEachRemaining(entry -> {
                    String fieldName = entry.getKey();
                    String fieldValue = entry.getValue().textValue();
                    values.put(fieldName, fieldValue);
                });
            } catch (Exception e) {
                throw new JsonExtractorError("Error parsing JSON: " + e.getMessage());
            }
        } else {
            throw new IllegalArgumentException("Invalid JSON format.");
        }
        return values;
    }

    private static String isError(String value){
        if(value == null){
            return null;
        }
        if(value.equals("5000")){
            value = "User already logged in";
        } else if (value.equals("5001")) {
            value = "Username has an invalid format or length";
        } else if (value.equals("5002")){
            value = "User cannot login twice";
        } else if (value.equals("6000")){
            value = "User is not logged in";
        } else if (value.equals("7000")){
            value = "Pong timeout";
        } else if (value.equals("7001")) {
            value = "Unterminated message";
        } else if (value.equals("8000")){
            value = "Pong without ping";
        } else if (value.equals("ERROR")) {
            value = "";
        }
        return value;
    }
}
