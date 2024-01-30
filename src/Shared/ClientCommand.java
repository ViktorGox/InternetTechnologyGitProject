package Shared;

public class ClientCommand {
    private final String command;
    private final String message;

    public ClientCommand(String unsplitRawMessage) {
        // Handle no space present.
        int indexOfJSON = unsplitRawMessage.indexOf("{");
        if(indexOfJSON != -1) {
            command = unsplitRawMessage.substring(0, indexOfJSON).trim();
            message = unsplitRawMessage.substring(indexOfJSON).trim();
            return;
        }
        command = unsplitRawMessage.trim();
        message = null;
    }

    public String getCommand() {
        return command;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return command + " " + (message == null ? "" : message);
    }
}
