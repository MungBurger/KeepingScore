package com.robmapps.keepingscore;
import java.util.HashMap;
import java.util.Map;
public class ShotAnalyser {
    public static Map<String, PlayerShotStats> analyzeShotData(String data) {
        Map<String, PlayerShotStats> playerStats = new HashMap<>();

        if (data == null || data.trim().isEmpty()) {
            return playerStats; // Return empty map if data is null or empty
        }
        String[] lines = data.trim().split("\n");
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
            String playerName = columns[0];
            String shotOutcome = columns[2];    // Third column (index 2)
            String outcomeLower = shotOutcome.toLowerCase(java.util.Locale.ROOT);
            // Check if this playerName is one we want to track
            if ("goal".equals(outcomeLower) || "miss".equals(outcomeLower)) {
                PlayerShotStats stats = playerStats.computeIfAbsent(playerName, k -> new PlayerShotStats(k));
                // Now, update the individual stats for THIS player
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
