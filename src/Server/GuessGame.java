package Server;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class GuessGame implements Runnable {
    private Timer timer;

    @Override
    public void run() {
        startTimer();

    }

    private void startTimer() {
        timer = new Timer();

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handleTimerCompletion();
                stopTimer();
            }
        }, 30000);
    }

    private void handleTimerCompletion() {


    }

    private void stopTimer() {
        if (timer != null) {
            timer.cancel();
        }
    }
}
