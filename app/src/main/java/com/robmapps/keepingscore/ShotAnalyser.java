package com.robmapps.keepingscore;
import java.util.HashMap;
import java.util.Map;

public class ShotAnalyser {
    public static Map<String, PlayerShotStats> analyzeShotData(String data) {
        return analyzeShotData(data, null, null);
    }
    
    public static Map<String, PlayerShotStats> analyzeShotData(String data, String team1Name, String team2Name) {
        Map<String, PlayerShotStats> playerStats = new HashMap<>();

        if (data == null || data.trim().isEmpty()) {
            return playerStats; // Return empty map if data is null or empty
        }
        
        String[] lines = data.trim().split("\\n");
        for (String line : lines) {
            if (line.trim().isEmpty()) {
                continue; // Skip empty lines
            }
            String[] columns = line.split(",");
            for (int i = 0; i < columns.length; i++) {
                columns[i] = columns[i].trim(); // Trim whitespace from each column
            }
            if (columns.length < 3) {
                System.out.println("Skipping malformed line: " + line);
                continue; // Ensure there are enough columns
            }
            // In the new format, columns are: Team Name, Position, Action, Player Name
            String teamName = columns[0];
            String playerPosition = columns[1];
            String shotOutcome = columns[2];    // Third column (index 2) - Action
            String playerName = columns.length > 3 ? columns[3] : "Unknown Player";
            String outcomeLower = shotOutcome.toLowerCase(java.util.Locale.ROOT);
            
            // Check if this is a scoring action we want to track
            if ("goal".equals(outcomeLower) || "miss".equals(outcomeLower)) {
                // Determine the correct team name based on position
                String correctTeamName;
                if (playerPosition != null && !playerPosition.isEmpty()) {
                    if (playerPosition.endsWith("1")) {
                        correctTeamName = team1Name != null && !team1Name.isEmpty() ? team1Name : "Club Team";
                    } else if (playerPosition.endsWith("2")) {
                        correctTeamName = team2Name != null && !team2Name.isEmpty() ? team2Name : "Opposition Team";
                    } else {
                        // If position doesn't have a team indicator, use the team name from the data
                        correctTeamName = teamName;
                    }
                } else {
                    correctTeamName = teamName;
                }
                
                // Create a key that combines team name and player name to avoid mixing stats
                String statsKey = correctTeamName + ": " + playerName;
                
                // Get or create stats for this player
                PlayerShotStats stats = playerStats.computeIfAbsent(statsKey, k -> {
                    PlayerShotStats newStats = new PlayerShotStats(playerName);
                    newStats.playerPosition = playerPosition;
                    newStats.teamName = correctTeamName;
                    return newStats;
                });
                
                if (stats.playerPosition == null || stats.playerPosition.isEmpty()) {
                    stats.playerPosition = playerPosition;
                }
                
                // Update the individual stats for THIS player
                if ("goal".equals(outcomeLower)) {
                    stats.incrementGoals();
                } else { // "miss"
                    stats.incrementMisses();
                }
            }
        }
        return playerStats;
    }
}