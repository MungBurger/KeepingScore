package com.robmapps.keepingscore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
            String outcomeLower = shotOutcome.toLowerCase();

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
/*            if (targetPositions.contains(playerName)) {
                // Get or create the stats object for this player position
                // Java 8 and later:
                PlayerShotStats stats = playerStats.computeIfAbsent(playerName, k -> new PlayerShotStats(k));

                // For older Java versions:
                // PlayerShotStats stats = playerStats.get(playerName);
                // if (stats == null) {
                //     stats = new PlayerShotStats(playerName);
                //     playerStats.put(playerName, stats);
                // }

                String outcomeLower = shotOutcome.toLowerCase(); // Use lowercase for case-insensitive comparison
                if ("goal".equals(outcomeLower)) {
                    stats.incrementGoals();
                } else if ("miss".equals(outcomeLower)) {
                    stats.incrementMisses();
                }
            }*/
        }
        return playerStats;
    }
}
