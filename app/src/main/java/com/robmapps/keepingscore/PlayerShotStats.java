package com.robmapps.keepingscore;

public class PlayerShotStats {
    public String playerName;
    public String playerPosition;
    public String teamName;
    private int goals;
    private int misses;

    public PlayerShotStats(String playerName) {
        this.playerName = playerName;
        this.playerPosition = "";
        this.teamName = "Unknown Team";
        this.goals = 0;
        this.misses = 0;
    }

    public int getGoals() {
        return goals;
    }

    public void incrementGoals() {
        this.goals++;
    }

    public int getMisses() {
        return misses;
    }

    public void incrementMisses() {
        this.misses++;
    }
}