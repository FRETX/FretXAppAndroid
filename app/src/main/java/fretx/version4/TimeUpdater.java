package fretx.version4;

import android.os.Handler;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by pandor on 4/5/17.
 */

public class TimeUpdater {

    private Timer timer;
    private final TextView timeText;
    private final Handler handler = new Handler();
    private int second;
    private int minute;

    public TimeUpdater(TextView timeText) {
        this.timeText = timeText;
        timeText.setText("00:00");
    }

    public void resetTimer() {
        //zero time
        second = 0;
        minute = 0;
    }

    public void pauseTimer() {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    public void resumeTimer() {
        //set a new Timer
        timer = new Timer();

        //initialize the TimerTask's job
        TimerTask timerTask = new TimerTask() {
            public void run() {
                //use a handler to run a toast that shows the current timestamp
                handler.post(new Runnable() {
                    public void run() {
                        //update time
                        ++second;
                        if (second == 60) {
                            ++minute;
                            second = 0;
                        }
                        if (minute == 60) {
                            minute = 0;
                        }
                        //update textview
                        timeText.setText(String.format("%1$02d:%2$02d", minute, second));
                    }
                });
            }
        };

        //schedule the timer
        timer.schedule(timerTask, 1000, 1000); //
    }
}
