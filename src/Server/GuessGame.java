package Server;

import Messages.JsonMessage;
import Messages.MessageError;
import Messages.MessageGoodStatus;

import java.util.*;

public class GuessGame extends Thread {
    private Set<ServerSideClient> gamers = new HashSet<>();
    private Map<ServerSideClient, Long> gamerTimes = new HashMap<>();

    ServerSideClient creator;
    int numberToGuess = 0;
    private Timer startTimer;
    private Timer gameTimer;
    private long gameStartTime;

    String error = "GG_GUESS_ERROR";
    String start = "GG_GUESS_START";
    String resp = "GG_GUESS_RESP";


    public GuessGame(ServerSideClient creator) {
        this.creator = creator;
        gamers.add(creator);
    }

    @Override
    public void run() {
        startStartTime();
    }

    private void startStartTime() {
        startTimer = new Timer();

        startTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                handleStartCompletion();
                stopTimer(startTimer);
            }
        }, 30000);
    }


    private void handleStartCompletion() {
        JsonMessage jsonMessage;
        if (gamers.size() < 2) {
            jsonMessage = new MessageError("8004");
        } else {
            jsonMessage = new MessageGoodStatus();
            generateRandomNumber();
        }
        Server.getInstance().broadcastTo(start, jsonMessage, gamers);
    }

    private void startGameTimer() {
        gameTimer = new Timer();
        gameStartTime = System.currentTimeMillis();
        gameTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                handleGameCompletion();
                stopTimer(gameTimer);
            }
        }, 120000);
    }

    private void handleGameCompletion() {
        if (gamers.size() != gamerTimes.size()) {
            for (ServerSideClient gamer : gamers) {
                if (!gamerTimes.containsKey(gamer)) {
                    //TODO: MARK AS DNF
                }
            }
        }
            // TODO: SEND THE LEADERBOARD
    }


    private void stopTimer(Timer startTimer) {
        if (startTimer != null) {
            startTimer.cancel();
        }
    }

    synchronized public void addGamer(ServerSideClient gamer) {
        gamers.add(gamer);
    }

    synchronized public int compareNumber(int inputNumber, ServerSideClient gamer) {
        if (inputNumber < numberToGuess) {
            return -1;
        } else if (inputNumber == numberToGuess) {
            processGuess(gamer);
            return 0;
        } else {
            return 1;
        }
    }

    public void generateRandomNumber() {
        Random random = new Random();

        numberToGuess = random.nextInt(51) + 1;
    }

    //TODO: ASK HERE
    synchronized public void processGuess(ServerSideClient gamer) {
        long currentTime = System.currentTimeMillis();
        long timeElapsed = (currentTime - gameStartTime) / 1000;

        gamerTimes.put(gamer, timeElapsed);

        if (gamerTimes.size() == gamers.size()) {
            handleGameCompletion();
            stopTimer(gameTimer);
        }
    }


}
