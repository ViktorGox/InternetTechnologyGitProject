package Client;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.util.*;

public class EncryptionHandler {

    private final Map<String, byte[]> sessionKeys = new HashMap<>();
    private final Map<String, String> awaitingList = new HashMap<>();
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

    public byte[] encryptWithPublicKey(byte[] data, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(data);
    }

    public byte[] decryptWithPrivateKey(byte[] encryptedData, PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return cipher.doFinal(encryptedData);
    }

    public byte[] generateRandomKey() {
        byte[] key = new byte[16]; // 128 bits for AES-128
        new SecureRandom().nextBytes(key);
        return key;
    }

    public PublicKey getPublicKey() {
        return keyPair.getPublic();
    }

    public PrivateKey getPrivateKey() {
        return keyPair.getPrivate();
    }

    public String findWaitingUser(String username) {
        return awaitingList.get(username);
    }

    public void addWaitingUser(String username, String message) {
        awaitingList.put(username, message);
    }

    public byte[] getSessionKey(String username) {
        return sessionKeys.get(username);
    }
    public void addSessionKey(String username, byte[] sessionKey) {
        System.out.println("Adding a key for: " + username + ". Key is: " + sessionKey);
        sessionKeys.put(username, sessionKey);
    }

    public String encryptWithSessionKey(String plainText, byte[] sessionKey) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            SecretKey secretKey = new SecretKeySpec(sessionKey, 0, sessionKey.length, "AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes());
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String decryptWithSessionKey(String encryptedText, byte[] sessionKey) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            SecretKey secretKey = new SecretKeySpec(sessionKey, 0, sessionKey.length, "AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedText);
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            return new String(decryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
