package Shared;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

public class EncryptionUtils {
    public static PublicKey stringByteArrayToPublicKey(String byteArray) {
        try {
            byte[] keyBytes = stringByteArrayToByteArray(byteArray);

            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");

            return keyFactory.generatePublic(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] stringByteArrayToByteArray(String byteArray) {
        String[] byteStrings = byteArray.replaceAll("\\[|\\]", "").split(", ");

        byte[] keyBytes = new byte[byteStrings.length];
        for (int i = 0; i < byteStrings.length; i++) {
            keyBytes[i] = Byte.parseByte(byteStrings[i]);
        }

        return keyBytes;
    }
}
