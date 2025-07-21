package com.robmapps.keepingscore;

import com.robmapps.keepingscore.database.entities.GameAction;
import com.robmapps.keepingscore.database.entities.GameStats;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

/**
 * Utility class for analyzing team performance against specific opposition teams
 */
public class TeamStatsAnalyzer {

    /**
     * Calculate summary statistics for games against a specific opposition team
     * @param games List of games against the opposition team
     * @param actions List of actions from those games
     * @return Map containing summary statistics
     */
    public static Map<String, Object> analyzeTeamPerformance(List<GameStats> games, List<List<GameAction>> actions) {
        Map<String, Object> stats = new HashMap<>();
        
        if (games == null || games.isEmpty()) {
            return stats;
        }
        
        // Initialize counters
        int totalGames = games.size();
        int wins = 0;
        int losses = 0;
        int draws = 0;
        int totalGoalsScored = 0;
        int totalGoalsConceded = 0;
        
        // Calculate win/loss record
        for (GameStats game : games) {
            if (game.team1Score > game.team2Score) {
                wins++;
            } else if (game.team1Score < game.team2Score) {
                losses++;
            } else {
                draws++;
            }
            
            totalGoalsScored += game.team1Score;
            totalGoalsConceded += game.team2Score;
        }
        
        // Calculate player performance if actions are provided
        Map<String, PlayerPerformance> playerStats = new HashMap<>();
        if (actions != null) {
            for (int i = 0; i < Math.min(games.size(), actions.size()); i++) {
                List<GameAction> gameActions = actions.get(i);
                if (gameActions != null) {
                    for (GameAction action : gameActions) {
                        // Only process actions for the club team (team1)
                        if (action.teamName.equals(games.get(i).team1Name)) {
                            String playerName = action.playerName;
                            
                            // Skip if no player name
                            if (playerName == null || playerName.isEmpty()) {
                                continue;
                            }
                            
                            // Get or create player stats
                            PlayerPerformance performance = playerStats.getOrDefault(
                                playerName, new PlayerPerformance(playerName));
                            
                            // Update stats based on action
                            if ("Goal".equals(action.actionType)) {
                                performance.goals++;
                            } else if ("Miss".equals(action.actionType)) {
                                performance.misses++;
                            }
                            
                            // Update the map
                            playerStats.put(playerName, performance);
                        }
                    }
                }
            }
        }
        
        // Add all stats to the result map
        stats.put("totalGames", totalGames);
        stats.put("wins", wins);
        stats.put("losses", losses);
        stats.put("draws", draws);
        stats.put("goalsScored", totalGoalsScored);
        stats.put("goalsConceded", totalGoalsConceded);
        stats.put("playerStats", playerStats);
        
        return stats;
    }
    
    /**
     * Inner class to track individual player performance
     */
    public static class PlayerPerformance {
        public String playerName;
        public int goals;
        public int misses;
        public double accuracy;
        
        public PlayerPerformance(String playerName) {
            this.playerName = playerName;
            this.goals = 0;
            this.misses = 0;
            this.accuracy = 0.0;
        }
        
        public void calculateAccuracy() {
            int totalShots = goals + misses;
            if (totalShots > 0) {
                accuracy = (double) goals / totalShots * 100.0;
            }
        }
    }
}