package Client;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.*;
import java.util.HashMap;
import java.util.Map;

public class EncryptionHandler {

    private final Map<String, String> sessionKeys = new HashMap<>();

    private KeyPair keyPair;

    public EncryptionHandler() {
        try {
            keyPair = generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        return keyPairGenerator.generateKeyPair();
    }

    // Utility method to encrypt a message using the public key
    private byte[] encrypt(String message, PublicKey publicKey)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
            IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(message.getBytes());
    }

    // Utility method to decrypt a message using the private key
    private String decrypt(byte[] encryptedMessage, PrivateKey privateKey)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
            IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] decryptedBytes = cipher.doFinal(encryptedMessage);
        return new String(decryptedBytes);
    }

    private static byte[] generateRandomKey() {
        byte[] key = new byte[16]; // 128 bits for AES-128
        new SecureRandom().nextBytes(key);
        return key;
    }

    public PublicKey getPublicKey() {
        return keyPair.getPublic();
    }

    public String getSessionKey(String username) {
        return sessionKeys.get(username);
    }
}
