package Shared;

import Shared.Headers.*;

// Reason for this class it, that at first I wanted to put the headers in the Message classes, which works fine
// until you get to the error/ok status messages. So I thought of this. We have the enums and you can call them,
// rather than have to write the strings every time, risking misspelling.
public class EnumConverter {
    public enum GroupedEnum {
        LOGIN(LoginHeader.class),
        PING_PONG(PingPongHeader.class),
        BROADCAST(BroadcastHeader.class),
        ENCRYPTED(EncryptedPrivateHeader.class),
        PRIVATE_MESSAGE(PrivateMessageHeader.class),
        FILE_TRANSFER(FileTransferHeader.class),
        GUESSING_GAME(GuessingGameHeader.class),
        USER_LIST(UserListHeader.class),
        BYE(ByeHeader.class),
        OTHER(OtherHeader.class);

        private final Class<? extends Enum<?>> enumClass;

        GroupedEnum(Class<? extends Enum<?>> enumClass) {
            this.enumClass = enumClass;
        }

        public static Enum<?> fromString(String value) {
            for (GroupedEnum groupedEnum : values()) {
                for (Enum<?> enumValue : groupedEnum.enumClass.getEnumConstants()) {
                    if (enumValue.name().equalsIgnoreCase(value)) {
                        return enumValue;
                    }
                }
            }
            return null;
        }
    }
}