package com.robmapps.keepingscore;

import android.app.Service;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

public class TimerService extends Service {
    private CountDownTimer countDownTimer;
    private long timeRemaining;
    private int currentPeriod = 1;
    private int periodDuration; // In minutes
    private int totalPeriods;
    private boolean isRunning = false;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("TimerService", "TimerService started");

        // Get data from intent
        periodDuration = intent.getIntExtra("PERIOD_DURATION", 10); // Default to 10 minutes
        totalPeriods = intent.getIntExtra("TOTAL_PERIODS", 4); // Default to 4 quarters
        currentPeriod = intent.getIntExtra("CURRENT_PERIOD", 1); // Default to period 1

        // Start the timer
        startTimer(periodDuration * 60 * 1000L); // Convert minutes to milliseconds
        return START_STICKY;
    }


    private void startTimer(long duration) {
        countDownTimer = new CountDownTimer(duration, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // Update remaining time
                timeRemaining = millisUntilFinished;
                sendBroadcast(); // Update UI via broadcast
            }

            @Override
            public void onFinish() {
                // Timer stops automatically
                isRunning = false;

                // Trigger additional actions
                triggerEndOfPeriodActions();

                if (currentPeriod < totalPeriods) {
                    currentPeriod++;
                    //startTimer(periodDuration * 60 * 1000L); // Start next period
                } else {
                    stopSelf(); // Stop the service entirely when all periods are done
                }
            }
        };
        isRunning = true;
        countDownTimer.start();
    }
    private void triggerEndOfPeriodActions() {
        Intent intent = new Intent("END_OF_PERIOD_ACTION");
        intent.putExtra("CURRENT_PERIOD", currentPeriod);
        intent.putExtra("TOTAL_PERIODS", totalPeriods);

        sendBroadcast(intent);

        // Example: Play a sound or vibration
        // MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.buzzer_sound);
        // mediaPlayer.start();

        Log.d("TimerService", "End of period actions triggered.");
    }

    private void sendBroadcast() {
        Intent intent = new Intent("GAME_TIMER_UPDATE");
        intent.putExtra("TIME_REMAINING", timeRemaining);
        intent.putExtra("CURRENT_PERIOD", currentPeriod);

        sendBroadcast(intent);
        Log.d("TimerService", "Broadcast sent: TIME_REMAINING=" + timeRemaining + ", CURRENT_PERIOD=" + currentPeriod);
    }


    @Override
    public void onDestroy() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        isRunning = false;
        super.onDestroy();
    }
}
