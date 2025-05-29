package com.robmapps.keepingscore;

public class ScoringAttempt {
    private final String playerPosition; // e.g., "GS1" or "GA2"
    private final String playerName;
    private final boolean isSuccessful; // true for a goal, false for a miss
    private final String timestamp; // Time of the action

    public ScoringAttempt(String playerName,String playerPosition, boolean isSuccessful, String timestamp) {
        this.playerName = playerName;
        this.playerPosition = playerPosition;
        this.isSuccessful = isSuccessful;
        this.timestamp = timestamp;
    }

    public String getPlayerPosition() {
        return playerPosition;
    }

    public boolean isSuccessful() {
        return isSuccessful;
    }

    public String getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return playerName + ", " + playerPosition + ", " + (isSuccessful ? "Goal" : "Miss") + ", " + timestamp;
    }
}
