package Shared;

import Shared.Messages.Broadcast.MessageBroadcast;
import Shared.Messages.Broadcast.MessageBroadcastRequest;
import Shared.Messages.Bye.MessageLeft;
import Shared.Messages.Encryption.*;
import Shared.Messages.*;
import Shared.Messages.PrivateMessage.MessagePrivateReceive;
import Shared.Messages.PrivateMessage.MessagePrivateSend;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

import static Client.Client.DISPLAY_RAW_DEBUG;

public class MessageFactory {
    private static <T> JsonMessage convertToBasicVersion(String messageAsJson, Class<T> messageType) {
        if(messageAsJson == null || messageAsJson.isBlank()) {
            if (DISPLAY_RAW_DEBUG) System.out.println("Json parsing failed.");
            return new MessageError("0");
        }
        ObjectMapper mapper = new ObjectMapper();
        System.out.println("MessageFactory / convertToBasicVersion: Received message: " + messageAsJson +
                ". Converting to class: " + messageType);
        try {
            return (JsonMessage) mapper.readValue(messageAsJson, messageType);
        } catch (JsonProcessingException e) {
            if (DISPLAY_RAW_DEBUG) System.out.println("Json parsing failed.");
            return new MessageError("0");
        }
    }

    public static JsonMessage convertToMessageClass(ClientCommand received) {
        if (DISPLAY_RAW_DEBUG) System.out.println("(convertToMessageClass) received this: " + received);

        if (received.getCommandAsEnum() == null) {
            return new MessageError("1");
        }

        Map<String, String> message = null;
        if (received.getMessage() != null) {
            try {
                message = JsonMessageExtractor.extractInformation(received.getMessage());
            } catch(IllegalArgumentException | JsonExtractorError e) {
                return new MessageError("0");
            }
            if (message.get("status") != null) { //TODO: or error. PongError
                return handleOther(received);
            }
        }

        System.out.println("MessageFactory switching enums: " + received.getCommandAsEnum().name());
        //TODO: Gives error when trying to use Enum, why? Convert cases to enum, not string.
        switch (received.getCommandAsEnum().name()) {
            case "LOGIN" -> {
                return convertToBasicVersion(received.getMessage(), MessageLogin.class);
            }
            case "JOINED" -> {
                return convertToBasicVersion(received.getMessage(), MessageJoined.class);
            }
            case "BROADCAST" -> {
                return convertToBasicVersion(received.getMessage(), MessageBroadcast.class);
            }
            case "BROADCAST_REQ" -> {
                return convertToBasicVersion(received.getMessage(), MessageBroadcastRequest.class);
//            case "DSCN" -> convertToBasicVersion(received.getMessage(), MessageBroadcastRequest.class); //TODO: DSCN ping pong missing
            }
            case "LEFT" -> {
                return convertToBasicVersion(received.getMessage(), MessageLeft.class);
            }
            case "GG_INVITATION" -> {
                return convertToBasicVersion(received.getMessage(), MessageInvite.class); //TODO: Bad message, contains english.
            }
            case "GG_GUESS", "GG_GUESS_RESP" -> {
                return convertToBasicVersion(received.getMessage(), MessageGuess.class);
            }
            case "GG_GUESS_END" -> {
                return convertToBasicVersion(received.getMessage(), LeaderboardMessage.class); //TODO: Bad message, contains english.
            }
            case "PRIVATE_SEND" -> {
                return convertToBasicVersion(received.getMessage(), MessagePrivateSend.class);
            }
            case "PRIVATE_RECEIVE" -> {
                return convertToBasicVersion(received.getMessage(), MessagePrivateReceive.class);
            }
            case "USER_LIST_RESP" -> {
                return convertToBasicVersion(received.getMessage(), MessageUserList.class);
            }
            case "FILE_TRF" -> {
                return convertToBasicVersion(received.getMessage(), MessageFileTransfer.class);
            }
            case "FILE_TRF_ANSWER" -> {
                return convertToBasicVersion(received.getMessage(), MessageFileTrfAnswer.class); //TODO: No body in
            }
            case "REQ_PUBLIC_KEY" -> {
                return convertToBasicVersion(received.getMessage(), MessageReqPublicKey.class);
            }
            case "REQ_PUBLIC_KEY_RESP" -> {
                return convertToBasicVersion(received.getMessage(), MessageReqPublicKeyResp.class);
            }
            case "SESSION_KEY_CREATE" -> {
                return convertToBasicVersion(received.getMessage(), MessageSessionKeyCreate.class);
            }
            case "ENC_PRIVATE_SEND", "ENC_PRIVATE_RECEIVE" -> {
                return convertToBasicVersion(received.getMessage(), MessageEncPrivateSend.class);
            }
            case "GG_CREATE", "PING", "PONG", "BYE", "GG_LEAVE", "USER_LIST", "GG_JOIN" -> {
                return null;
            }
        }
        if (DISPLAY_RAW_DEBUG) System.out.println("MessageFactory failed.");
        return new MessageError("1");
    }

    private static JsonMessage handleOther(ClientCommand clientCommand) {
        System.out.println("(MessageFactory) Handling other. " + clientCommand);

        if ("SESSION_KEY_CREATE_RESP".equals(clientCommand.getCommandAsEnum().name())) {
            return convertToBasicVersion(clientCommand.getMessage(), MessageSessionKeyCreateResp.class);
        }

        Map<String, String> mapped;

        try {
            mapped = JsonMessageExtractor.extractInformation(clientCommand.getMessage());
        } catch(IllegalArgumentException | JsonExtractorError e) {
            return new MessageError("0");
        }
        if (mapped.get("status") != null && mapped.get("code") == null) {
            return new MessageGoodStatus();
        }
        if (mapped.get("status") != null && mapped.get("code") != null) {
            return new MessageError(mapped.get("code"));
        }

        if (DISPLAY_RAW_DEBUG) System.out.println("MessageFactory failed while handling other.");
        return new MessageError("1");
    }
}