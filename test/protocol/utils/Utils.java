package protocol.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import protocol.messages.*;

import java.util.HashMap;
import java.util.Map;

public class Utils {

    private final static ObjectMapper mapper = new ObjectMapper();
    private final static Map<Class<?>, String> objToNameMapping = new HashMap<>();
    static {
        objToNameMapping.put(Login.class, "LOGIN");
        objToNameMapping.put(LOGIN_RESP.class, "LOGIN_RESP");
        objToNameMapping.put(BROADCAST_REQ.class, "BROADCAST_REQ");
        objToNameMapping.put(BROADCAST_RESP.class, "BROADCAST_RESP");
        objToNameMapping.put(BROADCAST.class, "BROADCAST");
        objToNameMapping.put(JOINED.class, "JOINED");
        objToNameMapping.put(PARSE_ERROR.class, "PARSE_ERROR");
        objToNameMapping.put(Pong.class, "PONG");
        objToNameMapping.put(PONG_ERROR.class, "PONG_ERROR");
        objToNameMapping.put(Welcome.class, "WELCOME");
        objToNameMapping.put(Ping.class, "PING");
    }

    public static String objectToMessage(Object object) throws JsonProcessingException {
        Class<?> clazz = object.getClass();
        String header = objToNameMapping.get(clazz);
        if (header == null) {
            throw new RuntimeException("Cannot convert this class to a message");
        }
        String body = mapper.writeValueAsString(object);
        return header + " " + body;
    }

    public static <T> T messageToObject(String message) throws JsonProcessingException {
        String[] parts = message.split(" ", 2);
        if (parts.length > 2 || parts.length == 0) {
            throw new RuntimeException("Invalid message");
        }
        String header = parts[0];
        String body = "{}";
        if (parts.length == 2) {
            body = parts[1];
        }
        Class<?> clazz = getClass(header);
        Object obj = mapper.readValue(body, clazz);
        return (T) clazz.cast(obj);
    }

    private static Class<?> getClass(String header) {
        return objToNameMapping.entrySet().stream()
                .filter(e -> e.getValue().equals(header))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Cannot find class belonging to header " + header));
    }
}
