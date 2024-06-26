import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import protocol.messages.*;
import protocol.utils.Utils;

import java.io.*;
import java.net.Socket;
import java.util.Properties;

import static java.time.Duration.ofMillis;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

public class PrivateMessage {
    private static Properties props = new Properties();

    private Socket socketUser1, socketUser2;
    private BufferedReader inUser1, inUser2;
    private PrintWriter outUser1, outUser2;

    private final static int max_delta_allowed_ms = 100;

    @BeforeAll
    static void setupAll() throws IOException {
        InputStream in = PrivateMessage.class.getResourceAsStream("testconfig.properties");
        props.load(in);
        in.close();
    }

    @BeforeEach
    void setup() throws IOException {
        socketUser1 = new Socket(props.getProperty("host"), Integer.parseInt(props.getProperty("port")));
        inUser1 = new BufferedReader(new InputStreamReader(socketUser1.getInputStream()));
        outUser1 = new PrintWriter(socketUser1.getOutputStream(), true);

        socketUser2 = new Socket(props.getProperty("host"), Integer.parseInt(props.getProperty("port")));
        inUser2 = new BufferedReader(new InputStreamReader(socketUser2.getInputStream()));
        outUser2 = new PrintWriter(socketUser2.getOutputStream(), true);
    }

    @AfterEach
    void cleanup() throws IOException {
        socketUser1.close();
        socketUser2.close();
    }

    @Test
    void broadcastPrivateMessageWithoutLoginReturnsError() throws JsonProcessingException {
        receiveLineWithTimeout(inUser1); //welcome message
        receiveLineWithTimeout(inUser2); //welcome message

        //Send message without login
        outUser1.println(Utils.objectToMessage(new PrivateSend("user1", "bla")));
        outUser1.flush();
        String error = receiveLineWithTimeout(inUser1);

        PRIVATE_RESP privateResp = Utils.messageToObject(error);
        assertEquals(new PRIVATE_RESP("ERROR", 1000), privateResp);
    }

    @Test
    void broadcastPrivateMessageWithEmptyMessageReturnsError() throws JsonProcessingException {
        receiveLineWithTimeout(inUser1); //welcome message
        receiveLineWithTimeout(inUser2); //welcome message

        // Connect user 1
        outUser1.println(Utils.objectToMessage(new Login("user1")));
        outUser1.flush();
        receiveLineWithTimeout(inUser1); //OK

        // Send empty message
        outUser1.println(Utils.objectToMessage(new PrivateSend("user2", "")));
        String error = receiveLineWithTimeout(inUser1);
        PRIVATE_RESP privateResp = Utils.messageToObject(error);
        assertEquals(new PRIVATE_RESP("ERROR", 1001), privateResp);
    }

    @Test
    void broadcastPrivateMessageToNonExistingUserReturnsError() throws JsonProcessingException {
        receiveLineWithTimeout(inUser1); //welcome message
        receiveLineWithTimeout(inUser2); //welcome message

        // Connect user 1
        outUser1.println(Utils.objectToMessage(new Login("user1")));
        outUser1.flush();
        receiveLineWithTimeout(inUser1); //OK

        // Send empty message
        outUser1.println(Utils.objectToMessage(new PrivateSend("user2", "bla")));
        String error = receiveLineWithTimeout(inUser1);
        PRIVATE_RESP privateResp = Utils.messageToObject(error);
        assertEquals(new PRIVATE_RESP("ERROR", 1002), privateResp);
    }

    @Test
    void broadcastPrivateMessageToSenderReturnsError() throws JsonProcessingException {
        receiveLineWithTimeout(inUser1); //welcome message
        receiveLineWithTimeout(inUser2); //welcome message

        // Connect user 1
        outUser1.println(Utils.objectToMessage(new Login("user1")));
        outUser1.flush();
        receiveLineWithTimeout(inUser1); //OK

        // Send empty message
        outUser1.println(Utils.objectToMessage(new PrivateSend("user1", "bla")));
        String error = receiveLineWithTimeout(inUser1);
        PRIVATE_RESP privateResp = Utils.messageToObject(error);
        assertEquals(new PRIVATE_RESP("ERROR", 1003), privateResp);
    }

    @Test
    void broadcastPrivateMessageSendsMessageToReceiver() throws JsonProcessingException {
        receiveLineWithTimeout(inUser1); //welcome message
        receiveLineWithTimeout(inUser2); //welcome message

        // Connect user 1
        outUser1.println(Utils.objectToMessage(new Login("user1")));
        outUser1.flush();
        receiveLineWithTimeout(inUser1); //OK

        // Connect using same username
        outUser2.println(Utils.objectToMessage(new Login("user2")));
        outUser2.flush();
        receiveLineWithTimeout(inUser2);

        //Send message to user2
        outUser1.println(Utils.objectToMessage(new PrivateSend("user2", "bla")));
        outUser1.flush();
        receiveLineWithTimeout(inUser1); //OK

        String resUser2 = receiveLineWithTimeout(inUser2);
        PrivateReceive privateReceive = Utils.messageToObject(resUser2);
        assertEquals(new PrivateReceive("user1", "bla"), privateReceive);
    }

    private String receiveLineWithTimeout(BufferedReader reader) {
        return assertTimeoutPreemptively(ofMillis(max_delta_allowed_ms), reader::readLine);
    }
}
