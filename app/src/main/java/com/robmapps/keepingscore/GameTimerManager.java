package com.robmapps.keepingscore;

import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.Toast;

import java.util.Locale;

/**
 * Manages game timer functionality including period timers and end-of-period handling
 */
public class GameTimerManager {
    private final Context context;
    private CountDownTimer endOfPeriodTimer;
    private boolean debugMode = false;
    private long timeMultiplier;
    private String timeFormatted;
    private TimerListener listener;
    
    /**
     * Interface for timer event callbacks
     */
    public interface TimerListener {
        void onTimerTick(String formattedTime);
        void onPeriodEnd(int currentPeriod, int totalPeriods);
    }
    
    public GameTimerManager(Context context) {
        this.context = context;
    }
    
    public void setListener(TimerListener listener) {
        this.listener = listener;
    }
    
    /**
     * Starts the game timer service
     */
    public void startGameTimer(int periodDuration, int totalPeriods, int currentPeriod) {
        Intent intent = new Intent(context, TimerService.class);
        intent.putExtra("PERIOD_DURATION", periodDuration);
        intent.putExtra("TOTAL_PERIODS", totalPeriods);
        intent.putExtra("CURRENT_PERIOD", currentPeriod);
        context.startService(intent);
        
        Toast.makeText(context, "Game Timer Started!", Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Stops the game timer service
     */
    public void stopGameTimer() {
        Intent stopIntent = new Intent(context, TimerService.class);
        context.stopService(stopIntent);
    }
    
    /**
     * Starts the end-of-period timer
     */
    public void startEndOfPeriodTimer(boolean isDebugMode, int currentPeriod, int totalPeriods) {
        debugMode = isDebugMode;
        timeMultiplier = debugMode ? 10000 : 60000;
        
        if (endOfPeriodTimer != null) {
            endOfPeriodTimer.cancel();
        }
        
        endOfPeriodTimer = new CountDownTimer(timeMultiplier / 3, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                updateTimerDisplay(millisUntilFinished);
            }
            
            @Override
            public void onFinish() {
                if (listener != null) {
                    listener.onPeriodEnd(currentPeriod, totalPeriods);
                }
            }
        }.start();
    }
    
    /**
     * Updates the timer display during countdown
     */
    private void updateTimerDisplay(long millisUntilFinished) {
        long minutes = ((millisUntilFinished / 1000) % 3600) / 60;
        long seconds = (millisUntilFinished / 1000 % 60);
        timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        
        if (listener != null) {
            listener.onTimerTick(timeFormatted);
        }
    }
    
    /**
     * Cancels any active timers
     */
    public void cancelTimers() {
        if (endOfPeriodTimer != null) {
            endOfPeriodTimer.cancel();
            endOfPeriodTimer = null;
        }
    }
    
    /**
     * Gets the current formatted time
     */
    public String getFormattedTime() {
        return timeFormatted;
    }
}