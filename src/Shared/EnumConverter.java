package Shared;

import Shared.Headers.*;

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

        public Class<? extends Enum<?>> getEnumClass() {
            return enumClass;
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

    public static void processEnum(Enum<?> enumValue) {
        System.out.println("Processing: " + enumValue);
    }

    //TODO: Remove this.
//    public static void main(String[] args) {
//        // Examples of calling the method with different enums
//        processEnum(UserListHeader.USER_LIST);
//
//        // Example of converting a string to an enum within a group
//        Enum<?> convertedEnum = GroupedEnum.fromString("LOGIN");
//        if (convertedEnum != null) {
//            System.out.println("Converted to Enum: " + convertedEnum);
//        } else {
//            System.out.println("No matching enum found for input: Red");
//        }
//    }
}