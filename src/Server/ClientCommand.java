package Server;

public class ClientCommand {
    private final String command;
    private final String message;

    public ClientCommand(String command, String message) {
        this.command = command;
        this.message = message;
    }

    public String getCommand() {
        return command;
    }

    public String getMessage() {
        return message;
    }
}
