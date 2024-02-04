import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import protocol.messages.*;
import protocol.utils.Utils;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Properties;

import static java.time.Duration.ofMillis;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

public class GuessGameTests {
    private static Properties props = new Properties();

    private Socket socketUser1, socketUser2;
    private BufferedReader inUser1, inUser2;
    private PrintWriter outUser1, outUser2;

    private final static int max_delta_allowed_ms = 100;

    @BeforeAll
    static void setupAll() throws IOException {
        InputStream in = GuessGameTests.class.getResourceAsStream("testconfig.properties");
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
    void creatingGuessingGameTwiceReturnsError() throws JsonProcessingException {
        receiveLineWithTimeout(inUser1); //welcome message
        receiveLineWithTimeout(inUser2); //welcome message

        // Connect user 1
        outUser1.println(Utils.objectToMessage(new Login("user1")));
        outUser1.flush();
        receiveLineWithTimeout(inUser1); //OK

        // Connect using same username
        outUser2.println(Utils.objectToMessage(new Login("user2")));
        outUser2.flush();
        receiveLineWithTimeout(inUser2); //OK
        receiveLineWithTimeout(inUser1); // User 2 Joined

        //Send message to user2
        outUser1.println(Utils.objectToMessage(new GG_Create()));
        outUser1.flush();

        receiveLineWithTimeout(inUser2); //Invitation
        outUser2.println(Utils.objectToMessage(new GG_Create()));
        String error = receiveLineWithTimeout(inUser2);
        GG_Create_Resp ggCreateResp = Utils.messageToObject(error);
        assertEquals(8001,ggCreateResp.code());
    }

    @Test
    void creatingGuessingGameNotLoggedInReturnsError() throws JsonProcessingException {
        receiveLineWithTimeout(inUser1); //welcome message
        receiveLineWithTimeout(inUser2); //welcome message

        //Send message to user2
        outUser1.println(Utils.objectToMessage(new GG_Create()));
        outUser1.flush();

        String error = receiveLineWithTimeout(inUser1);
        GG_Create_Resp ggCreateResp = Utils.messageToObject(error);
        assertEquals(8010, ggCreateResp.code());
    }

    @Test
    void creatingGuessingGameOtherUserReceivesInvite() throws JsonProcessingException {
        receiveLineWithTimeout(inUser1); //welcome message
        receiveLineWithTimeout(inUser2); //welcome message

        // Connect user 1
        outUser1.println(Utils.objectToMessage(new Login("user1")));
        outUser1.flush();
        receiveLineWithTimeout(inUser1); //OK

        // Connect using same username
        outUser2.println(Utils.objectToMessage(new Login("user2")));
        outUser2.flush();
        receiveLineWithTimeout(inUser2); //OK
        receiveLineWithTimeout(inUser1); // User 2 Joined

        //Send message to user2
        outUser1.println(Utils.objectToMessage(new GG_Create()));
        outUser1.flush();

        String invite = receiveLineWithTimeout(inUser2);
        GG_Invitation ggCreateResp = Utils.messageToObject(invite);
        assertEquals(new GG_Invitation("user1"),ggCreateResp);
    }

    @Test
    void joiningGuessingGameWithoutOneBeingCreatedReturnsError() throws JsonProcessingException {
        receiveLineWithTimeout(inUser1); //welcome message
        receiveLineWithTimeout(inUser2); //welcome message

        // Connect user 1
        outUser1.println(Utils.objectToMessage(new Login("user1")));
        outUser1.flush();
        receiveLineWithTimeout(inUser1); //OK

        // Connect using same username
        outUser2.println(Utils.objectToMessage(new Login("user2")));
        outUser2.flush();
        receiveLineWithTimeout(inUser2); //OK
        receiveLineWithTimeout(inUser1); // User 2 Joined

        //Send message to user2
        outUser1.println(Utils.objectToMessage(new GG_Join()));
        outUser1.flush();

        String joinResp = receiveLineWithTimeout(inUser1);
        GG_Join_Resp ggJoinResp = Utils.messageToObject(joinResp);
        assertEquals(new GG_Join_Resp("ERROR", 8002), ggJoinResp);
    }


    private String receiveLineWithTimeout(BufferedReader reader) {
        return assertTimeoutPreemptively(ofMillis(max_delta_allowed_ms), reader::readLine);
    }
}
