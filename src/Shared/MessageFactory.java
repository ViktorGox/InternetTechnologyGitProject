package Shared;

import Shared.ClientCommand;
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

public class MessageFactory {
    private static <T> JsonMessage convertToBasicVersion(String messageAsJson, Class<T> messageType) {
        ObjectMapper mapper = new ObjectMapper();
        System.out.println("MessageFactory / convertToBasicVersion: Received message: " + messageAsJson +
                ". Converting to class: " + messageType);
        try {
            return (JsonMessage) mapper.readValue(messageAsJson, messageType);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return null;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static JsonMessage convertToMessageClass(ClientCommand received) {
        System.out.println("(convertToMessageClass) received this: " + received);
        if (received.getMessage() == null) {
            System.out.println("(convertToMessageClass) Message is: " + received.getMessage());
            return null;
        }
        Map<String, String> message = JsonMessageExtractor.extractInformation(received.getMessage());
        if (message.get("status") != null) { //TODO: or error. PongError
            return handleOther(received);
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
            case "GG_CREATE", "PING", "PONG", "BYE", "GG_LEAVE", "USER_LIST" -> {
                return null;
            }
        }
        System.out.println("MessageFactory failed.");
        //TODO: Handle Invalid header + body
        throw new RuntimeException("Failed to find header in the MessageFactory. Header: " + received.getCommandAsEnum());
    }

    private static JsonMessage handleOther(ClientCommand clientCommand) {
        System.out.println("(MessageFactory) Handling other. " + clientCommand);

        switch (clientCommand.getCommandAsEnum().name()) {
            case "SESSION_KEY_CREATE_RESP" -> {
                return convertToBasicVersion(clientCommand.getMessage(), MessageSessionKeyCreateResp.class);
            }
        }

        Map<String, String> mapped = JsonMessageExtractor.extractInformation(clientCommand.getMessage());
        if(mapped.get("status") != null && mapped.get("code") == null) {
            return new MessageGoodStatus();
        }
        if(mapped.get("status") != null && mapped.get("code") != null) {
            return new MessageError(mapped.get("code"));
        }

        System.out.println("MessageFactory failed while handling other.");
        throw new RuntimeException("Failed to find header in the MessageFactory. Header: " + clientCommand.getCommandAsEnum());
    }
}