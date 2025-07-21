package com.robmapps.keepingscore;

public class ScoringAttempt {
    private final String playerPosition; // e.g., "GS1" or "GA2"
    private final String playerName;
    private final boolean isSuccessful; // true for a goal, false for a miss
    private final String timestamp; // Time of the action
    private String teamName; // Team name derived from position

    public ScoringAttempt(String playerName, String playerPosition, boolean isSuccessful, String timestamp) {
        this.playerName = playerName;
        this.playerPosition = playerPosition;
        this.isSuccessful = isSuccessful;
        this.timestamp = timestamp;
        
        // Default team name will be set via setTeamNames method
        this.teamName = "Unknown Team";
    }

    public String getPlayerName() {
        return playerName;
    }
    
    public String getPlayerPosition() {
        return playerPosition;
    }
    
    public String getTeamName() {
        return teamName;
    }
    
    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }
    
    /**
     * Sets the team name based on position and actual team names
     * 
     * @param clubTeamName The name of the club team (position ending with "1")
     * @param oppositionTeamName The name of the opposition team (position ending with "2")
     */
    public void setTeamNames(String clubTeamName, String oppositionTeamName) {
        if (playerPosition != null && !playerPosition.isEmpty()) {
            if (playerPosition.endsWith("1")) {
                this.teamName = clubTeamName != null && !clubTeamName.isEmpty() ? 
                        clubTeamName : "Club Team";
            } else if (playerPosition.endsWith("2")) {
                this.teamName = oppositionTeamName != null && !oppositionTeamName.isEmpty() ? 
                        oppositionTeamName : "Opposition Team";
            } else {
                // For positions without a team indicator, try to determine from context
                // Default to club team if we can't determine
                this.teamName = clubTeamName != null && !clubTeamName.isEmpty() ? 
                        clubTeamName : "Club Team";
            }
        } else {
            // If no position is available, use club team as default
            this.teamName = clubTeamName != null && !clubTeamName.isEmpty() ? 
                    clubTeamName : "Club Team";
        }
    }

    public boolean isSuccessful() {
        return isSuccessful;
    }

    public String getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return teamName + ", " + playerPosition + ", " + (isSuccessful ? "Goal" : "Miss") + ", " + playerName;
    }
}
