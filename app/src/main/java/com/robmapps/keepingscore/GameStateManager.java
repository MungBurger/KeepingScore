package com.robmapps.keepingscore;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.robmapps.keepingscore.database.AppDatabase;
import com.robmapps.keepingscore.database.entities.GameStats;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Manages game state persistence and restoration
 */
public class GameStateManager {
    private static final String PREFS_NAME = "MySharedPref";
    private final Context context;
    private final SharedViewModel viewModel;
    private SharedPreferences sharedPreferences;
    
    public GameStateManager(Context context, SharedViewModel viewModel) {
        this.context = context;
        this.viewModel = viewModel;
        this.sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    
    /**
     * Saves the current game state
     */
    public void saveGameState(GameState gameState) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        
        // Save scores and statistics
        saveScoreStats(editor, gameState);
        
        // Save team information
        saveTeamInfo(editor, gameState);
        
        // Save game progress
        saveGameProgress(editor);
        
        editor.apply();
        
        // Save to database
        saveGameStats(gameState);
    }
    
    /**
     * Saves score statistics to SharedPreferences
     */
    private void saveScoreStats(SharedPreferences.Editor editor, GameState gameState) {
        editor.putInt("iScore1", gameState.score1);
        editor.putInt("iScore2", gameState.score2);
        editor.putInt("iGS1", gameState.gs1);
        editor.putInt("iGA1", gameState.ga1);
        editor.putInt("iGS1M", gameState.gs1m);
        editor.putInt("iGA1M", gameState.ga1m);
        editor.putInt("iGS2", gameState.gs2);
        editor.putInt("iGA2", gameState.ga2);
        editor.putInt("iGS2M", gameState.gs2m);
        editor.putInt("iGA2M", gameState.ga2m);
    }
    
    /**
     * Saves team information to SharedPreferences
     */
    private void saveTeamInfo(SharedPreferences.Editor editor, GameState gameState) {
        editor.putString("tvTeam1", gameState.team1Name);
        editor.putString("etTeam2", gameState.team2Name);
        editor.putString("sGSPlayer", gameState.gsPlayer);
        editor.putString("sGAPlayer", gameState.gaPlayer);
        
        String gameLogForPrefs = viewModel.getCurrentActionsLogString();
        editor.putString("GameLog", gameLogForPrefs);
    }
    
    /**
     * Saves game progress information to SharedPreferences
     */
    private void saveGameProgress(SharedPreferences.Editor editor) {
        editor.putBoolean("GameInProgress", viewModel.getGameInProgress().getValue());
        editor.putString("GameMode", viewModel.getGameMode().getValue());
        editor.putString("CurrPeriod", String.valueOf(viewModel.getCurrentPeriod().getValue()));
    }
    
    /**
     * Saves the current game statistics to the database
     */
    private void saveGameStats(GameState gameState) {
        String gameLogForPrefs = viewModel.getCurrentActionsLogString();
        
        GameStats stats = new GameStats(
                new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date()),
                gameState.team1Name,
                gameState.team2Name,
                gameState.score1,
                gameState.score2,
                gameLogForPrefs
        );

        new Thread(() -> {
            AppDatabase db = MyApplication.getDatabase();
            db.gameStatsDao().insertGameStats(stats);
        }).start();
    }
    
    /**
     * Restores the game state
     */
    public GameState restoreGameState() {
        GameState gameState = new GameState();
        
        // Restore scores and statistics
        gameState.gs1 = sharedPreferences.getInt("iGS1", 0);
        gameState.ga1 = sharedPreferences.getInt("iGA1", 0);
        gameState.gs1m = sharedPreferences.getInt("iGS1M", 0);
        gameState.ga1m = sharedPreferences.getInt("iGA1M", 0);
        gameState.gs2 = sharedPreferences.getInt("iGS2", 0);
        gameState.ga2 = sharedPreferences.getInt("iGA2", 0);
        gameState.gs2m = sharedPreferences.getInt("iGS2M", 0);
        gameState.ga2m = sharedPreferences.getInt("iGA2M", 0);
        gameState.team1Name = sharedPreferences.getString("tvTeam1", "");
        gameState.team2Name = sharedPreferences.getString("etTeam2", "");
        gameState.gsPlayer = sharedPreferences.getString("sGSPlayer", null);
        gameState.gaPlayer = sharedPreferences.getString("sGAPlayer", null);
        
        // Restore game progress
        viewModel.setGameInProgress(sharedPreferences.getBoolean("GameInProgress", false));
        viewModel.setGameMode(sharedPreferences.getString("GameMode", "15m,4Q"));
        viewModel.setCurrentPeriod(sharedPreferences.getInt("CurrPeriod", 1));
        
        // Restore scoring attempts
        restoreScoringAttempts();
        
        return gameState;
    }
    
    /**
     * Restores scoring attempts from SharedPreferences
     */
    private void restoreScoringAttempts() {
        String actionsLogFromPrefs = sharedPreferences.getString("GameLog", null);
        if ((viewModel.currentActions.getValue() == null || viewModel.currentActions.getValue().isEmpty()) &&
                actionsLogFromPrefs != null && !actionsLogFromPrefs.isEmpty()) {

            List<ScoringAttempt> restoredAttempts = new ArrayList<>();
            String[] lines = actionsLogFromPrefs.split("\n");
            
            for (String line : lines) {
                if (line.trim().isEmpty()) continue;
                
                try {
                    parseScoringAttempt(line, restoredAttempts);
                } catch (Exception e) {
                    Log.e("GameStateManager", "Error parsing action line: " + line, e);
                }
            }
            
            viewModel.updateAllActions(restoredAttempts);
        }
    }
    
    /**
     * Parses a single scoring attempt from a string
     */
    private void parseScoringAttempt(String line, List<ScoringAttempt> attempts) {
        String[] parts = line.split(",", -1); // Split by comma, -1 to keep trailing empty strings
        
        if (parts.length == 4) {
            boolean isSuccessful = parts[2].trim().equalsIgnoreCase("Goal");
            attempts.add(new ScoringAttempt(
                parts[0].trim(),  // playerName
                parts[1].trim(),  // playerPosition
                isSuccessful,     // isSuccessful
                parts[3].trim()   // timestamp
            ));
        } else {
            Log.w("GameStateManager", "Could not parse action line: " + line);
        }
    }
    
    /**
     * Class to hold game state data
     */
    public static class GameState {
        public int score1, score2;
        public int gs1, ga1, gs1m, ga1m, gs2, ga2, gs2m, ga2m;
        public String team1Name, team2Name, gsPlayer, gaPlayer;
    }
}