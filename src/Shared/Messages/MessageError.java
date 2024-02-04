package Shared.Messages;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MessageError extends JsonMessage{
    @JsonProperty
    private String code;
    @JsonProperty
    private String status = "ERROR";
    public MessageError(@JsonProperty("code") String code){
        this.code = code;
    }
    public String getCode() {
        return code;
    }

    public String getStatus() {
        return status;
    }

    @Override
    public String toString() {
        if(code.equals("5000")){
            return "Username is in use";
        } else if (code.equals("5001")) {
            return "Username has an invalid format or length";
        }else if (code.equals("5002")) {
            return "User has already logged in ";
        }else if (code.equals("6000")) {
            return "User is not logged in";
        }else if (code.equals("7000")) {
            return "Pong timeout";
        }else if (code.equals("8000")) {
            return "Pong without ping ";
        }else if (code.equals("8001")) {
            return "Room already created ";
        }else if (code.equals("8010")) {
            return "User is not logged in ";
        }else if (code.equals("8002")){
            return "A room has not been created";
        } else if (code.equals("8003")) {
            return "You have already joined a game";
        }else if (code.equals("8004")) {
            return "Not enough users to start game";
        }else if (code.equals("8005")) {
            return "Guess is not an integer";
        }else if (code.equals("8006")) {
            return "Guess is not between 1 and 50";
        }else if (code.equals("8008")) {
            return "A game has not been started";
        }else if (code.equals("8007")) {
            return "You are not in a game";
        }else if (code.equals("1000")) {
            return "User not logged in";
        }else if (code.equals("1001")) {
            return "Sent empty message";
        }else if (code.equals("1002")) {
            return "Receiver doesn't exist";
        }else if (code.equals("1003")) {
            return "Can't send message to yourself";
        }else if (code.equals("3000")) {
            return "User not logged in";
        }else if (code.equals("3001")) {
            return "Receiver doesn't exist";
        }else if (code.equals("3003")){
            return "Sender cannot be receiver.";
        }else if (code.equals("3002")){
            return "Sender cannot be receiver.";
        }else if (code.equals("0")){
            return "Failed to parse the given data to json.";
        }else if (code.equals("1")){
            return "Failed to find a command with the given header.";
        }
        return "";
    }
}