package Shared;

public class ClientCommand {
    @SuppressWarnings("rawtypes")
    private final Enum header;
    private final String message;

    public ClientCommand(String unsplitRawMessage) {
        int indexOfJSON = unsplitRawMessage.indexOf("{");
        if(indexOfJSON != -1) {
            header = EnumConverter.GroupedEnum.fromString(unsplitRawMessage.substring(0, indexOfJSON).trim());
            message = unsplitRawMessage.substring(indexOfJSON).trim();
            return;
        }
        header = EnumConverter.GroupedEnum.fromString(unsplitRawMessage.trim());
        message = null;
    }

    public String getCommand() {
        //TODO: FIx
        return header.toString();
    }


    public Enum getCommandAsEnum() {
        return header;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return header + " " + (message == null ? "" : message);
    }
}