package Server;

import Shared.Headers.GuessingGameHeader;
import Shared.Messages.JsonMessage;
import Shared.Messages.LeaderboardMessage;
import Shared.Messages.MessageError;
import Shared.Messages.MessageGoodStatus;

import java.util.*;

public class GuessGame extends Thread {
    private Set<ServerSideClient> gamers = new HashSet<>();
    private Map<ServerSideClient, Long> gamerTimes = new HashMap<>();
    ServerSideClient creator;
    int numberToGuess = 0;
    private Timer startTimer;
    private Timer gameTimer;
    private long gameStartTime;

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
        System.out.println("GAME CREATED");

        startTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                handleStartCompletion();
                stopTimer(startTimer);
            }
        }, 5000);
    }


    private void handleStartCompletion() {
        System.out.println("TIMER ENDED");
        JsonMessage jsonMessage;
        if (gamers.size() < 2) {
            jsonMessage = new MessageError("8004");
            Server.getInstance().setGameCreated(false);
        } else {
            jsonMessage = new MessageGoodStatus();
            generateRandomNumber();
        }
        Server.getInstance().broadcastTo(GuessingGameHeader.GG_GUESS_START, jsonMessage, gamers);
        System.out.println("GAME HAS STARTED");
        startGameTimer();
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
        }, 30000);
    }

    private void handleGameCompletion() {
        if (gamers.size() != gamerTimes.size()) {
            for (ServerSideClient gamer : gamers) {
                if (!gamerTimes.containsKey(gamer)) {
                    gamerTimes.put(gamer, 120L);
                }
            }
        }
        Map<String, Long> leaderboard = convertToUsernameMap(gamerTimes);
        JsonMessage jsonMessage = new LeaderboardMessage(leaderboard);
        Server.getInstance().broadcastTo(GuessingGameHeader.GG_GUESS_END, jsonMessage, gamers);
        Server.getInstance().setGameCreated(false);
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

    synchronized public void processGuess(ServerSideClient gamer) {
        long currentTime = System.currentTimeMillis();
        long timeElapsed = (currentTime - gameStartTime) / 1000;

        gamerTimes.put(gamer, timeElapsed);

        if (gamerTimes.size() == gamers.size()) {
            handleGameCompletion();
            stopTimer(gameTimer);
        }
    }

    private Map<String, Long> convertToUsernameMap(Map<ServerSideClient, Long> originalMap) {
        Map<String, Long> usernameMap = new HashMap<>();

        for (Map.Entry<ServerSideClient, Long> entry : originalMap.entrySet()) {
            ServerSideClient serverSideClient = entry.getKey();
            String username = serverSideClient.getUsername();
            Long time = entry.getValue();

            usernameMap.put(username, time);
        }

        return usernameMap;
    }


}
