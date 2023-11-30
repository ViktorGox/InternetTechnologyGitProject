package Messages;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

public class JsonMessageExtractor {

    public static String extractInformation(String jsonMessage) {
        List<String> values = new ArrayList<>();

        int openingBracketIndex = jsonMessage.indexOf('{');
        int closingBracketIndex = jsonMessage.lastIndexOf('}');

        if (openingBracketIndex != -1 && closingBracketIndex != -1) {
            String jsonString = jsonMessage.substring(openingBracketIndex, closingBracketIndex + 1);

            try {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(jsonString);

                jsonNode.elements().forEachRemaining(value -> {
                    if (value.isValueNode()) {
                        values.add(value.asText());
                    } else {
                        values.add(value.toString());
                    }
                });
            } catch (Exception e) {
                System.out.println("Error parsing JSON: " + e.getMessage());
            }
        } else {
            System.out.println("Invalid JSON message format");
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (String value : values) {
            stringBuilder.append(value).append(" ");
        }
        return stringBuilder.toString();
    }
}
