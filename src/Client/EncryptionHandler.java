package Client;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.*;

public class EncryptionHandler {
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

    // Utility method to convert byte array to hexadecimal string for better representation
    private String bytesToHex(byte[] bytes) {
        StringBuilder hexStringBuilder = new StringBuilder(2 * bytes.length);
        for (byte b : bytes) {
            hexStringBuilder.append(String.format("%02x", b));
        }
        return hexStringBuilder.toString();
    }

    public PublicKey getPublicKey() {
        return keyPair.getPublic();
    }
}



//    public KeyPair generateKeys() {
//        try {
//            // Assume you have the public and private keys generated previously
//            KeyPair keyPair = generateKeyPair();
//
//            // Message to be encrypted
//            String originalMessage = "Hello, World!";
//
//            // Encrypt the message using the public key
//            byte[] encryptedMessage = encrypt(originalMessage, keyPair.getPublic());
//
//            // Decrypt the message using the private key
//            String decryptedMessage = decrypt(encryptedMessage, keyPair.getPrivate());
//
//            // Print the results
//            System.out.println("Original Message: " + originalMessage);
//            System.out.println("Encrypted Message: " + bytesToHex(encryptedMessage));
//            System.out.println("Decrypted Message: " + decryptedMessage);
//
//        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException |
//                 IllegalBlockSizeException | BadPaddingException e) {
//            e.printStackTrace();
//        }
//    }
