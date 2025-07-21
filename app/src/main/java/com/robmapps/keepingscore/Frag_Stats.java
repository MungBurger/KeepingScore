package com.robmapps.keepingscore;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.robmapps.keepingscore.database.entities.GameAction;
import com.robmapps.keepingscore.database.entities.GameStats;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Frag_Stats# newInstance} factory method to
 * create an instance of this fragment.
 */
public class Frag_Stats extends Fragment {

    private GameStatsAdapter adapter;
    private TextView tvGameSummary;
    private SharedViewModel viewModel;
    private TextView tvFullGameStatsString;
    private Button btnSaveStats, btnDeleteStats;
    private String gameStats;

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stats, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        btnSaveStats = view.findViewById(R.id.saveThisPage);
        Button btnOppositionStats = view.findViewById(R.id.btnOppositionStats);

        Spinner dropdown = view.findViewById(R.id.statsDropdown); // Replace with correct ID from fragment_stats.xml

        viewModel.getAllGameStats().observe(getViewLifecycleOwner(), stats -> {
            if (stats != null) {
                // Store the game stats list for reference when an item is selected
                final List<GameStats> gameStatsList = stats;
                
                ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                        android.R.layout.simple_spinner_dropdown_item,
                        stats.stream().map(game -> game.gameDate + ": " + game.team1Name + " vs " + game.team2Name)
                                .collect(Collectors.toList()));
                dropdown.setAdapter(adapter); // Correctly set the adapter for the Spinner
                
                // Set up spinner item selection listener
                dropdown.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                        if (position >= 0 && position < gameStatsList.size()) {
                            // Get the selected game stats
                            GameStats selectedGame = gameStatsList.get(position);
                            // Load game actions for this game
                            loadGameActions(selectedGame);
                        }
                    }

                    @Override
                    public void onNothingSelected(android.widget.AdapterView<?> parent) {
                        // Do nothing
                    }
                });
                
                // If there are games, select the first one by default
                if (!stats.isEmpty()) {
                    loadGameActions(stats.get(0));
                }
            }
        });
        tvFullGameStatsString = view.findViewById(R.id.tvFullGameStatsString); // Initialize the new TextView

        // Only use readGameStats for current game in progress
        if (viewModel.getGameInProgress().getValue() == Boolean.TRUE) {
            gameStats = readGameStats("", new StringBuilder());
            if (gameStats != null) {
                tvFullGameStatsString.setText(gameStats.toString());
            }
        } else {
            tvFullGameStatsString.setText("Select a game from the dropdown to view stats");
        }
        btnSaveStats.setOnClickListener(v -> saveGameStats());
        
        // Set up opposition stats button
        btnOppositionStats.setOnClickListener(v -> {
            // Show the opposition stats dialog
            OppositionStatsDialog dialog = new OppositionStatsDialog();
            dialog.show(requireActivity().getSupportFragmentManager(), "OppositionStatsDialog");
        });
        return view;
    }

    /**
     * Load game actions for a selected game from the database
     * @param gameStats The selected game stats
     */
    private void loadGameActions(GameStats gameStats) {
        // Show loading message
        tvFullGameStatsString.setText("Loading game data...");
        
        // Get actions for this game from the database
        viewModel.getActionsForGame(gameStats.id).observe(getViewLifecycleOwner(), actions -> {
            if (actions == null || actions.isEmpty()) {
                tvFullGameStatsString.setText("No actions found for this game.");
                return;
            }
            
            // Build the game stats display
            StringBuilder displayText = new StringBuilder();
            
            // Game header
            displayText.append(" ").append(gameStats.team1Name).append(" vs ").append(gameStats.team2Name);
            displayText.append("\n\n ").append(gameStats.team1Name).append(" Score: ").append(gameStats.team1Score);
            displayText.append("\n ").append(gameStats.team2Name).append(" Score: ").append(gameStats.team2Score);
            displayText.append("\n");
            
            // Calculate player shooting stats
            Map<String, PlayerShotStats> playerShootingStats = calculatePlayerStats(actions, gameStats.team1Name, gameStats.team2Name);
            
            // Display player shooting stats
            if (!playerShootingStats.isEmpty()) {
                displayText.append("\n--- Player Shooting Stats ---\n");
                for (Map.Entry<String, PlayerShotStats> entry : playerShootingStats.entrySet()) {
                    PlayerShotStats stats = entry.getValue();
                    int totalShots = stats.getGoals() + stats.getMisses();
                    double accuracy = 0.0;
                    
                    if (totalShots > 0) {
                        accuracy = ((double) stats.getGoals() / totalShots) * 100;
                    }
                    displayText.append(String.format(Locale.getDefault(),
                            "%s: %s - %d Goals, %d Misses (Accuracy: %.1f%%)\n",
                            stats.teamName, stats.playerName, 
                            stats.getGoals(), stats.getMisses(), accuracy));
                }
                displayText.append("---------------------------\n");
            } else {
                displayText.append("\nNo shooting data to analyze for player percentages.\n");
            }
            
            // Game Timeline section
            displayText.append("\n--- Game Timeline ---\n");
            displayText.append("Team Name, Position, Action, Player Name\n");
            
            // Sort actions by sequence number
            List<GameAction> sortedActions = new ArrayList<>(actions);
            sortedActions.sort((a1, a2) -> Integer.compare(a1.sequence, a2.sequence));
            
            // Add each action to the timeline
            for (GameAction action : sortedActions) {
                displayText.append(String.format("%s, %s, %s, %s\n", 
                    action.teamName, 
                    action.playerPosition, 
                    action.actionType, 
                    action.playerName));
            }
            
            // Display the complete stats
            tvFullGameStatsString.setText(displayText.toString());
        });
    }
    
    /**
     * Calculate player shooting statistics from game actions
     */
    private Map<String, PlayerShotStats> calculatePlayerStats(List<GameAction> actions, String team1Name, String team2Name) {
        Map<String, PlayerShotStats> playerStats = new HashMap<>();
        
        for (GameAction action : actions) {
            // Skip non-goal/miss actions if any
            if (!"Goal".equals(action.actionType) && !"Miss".equals(action.actionType)) {
                continue;
            }
            
            // Create a unique key for this player
            String key = action.teamName + "-" + action.playerName;
            
            // Get or create player stats
            PlayerShotStats stats = playerStats.get(key);
            if (stats == null) {
                stats = new PlayerShotStats(action.playerName);
                stats.teamName = action.teamName;
                playerStats.put(key, stats);
            }
            
            // Update stats based on action type
            if ("Goal".equals(action.actionType)) {
                stats.incrementGoals();
            } else if ("Miss".equals(action.actionType)) {
                stats.incrementMisses();
            }
        }
        
        return playerStats;
    }
    
    private String readGameStats(String fileName, StringBuilder exportFileContent) {
        String gameLogContent = viewModel.getCurrentActionsLogString();
        //OutputStream fos = null; // Use OutputStream
        //Uri uri = null;
        //StringBuilder exportFileContent = new StringBuilder();

        List<ScoringAttempt> actionsList = viewModel.getAllActions().getValue();
        String clubTeamName = viewModel.getActiveTeamName().getValue();
        String oppositionTeamName = viewModel.getTeam2Name().getValue();
        
        // Ensure team names are not null or empty
        if (clubTeamName == null || clubTeamName.isEmpty()) clubTeamName = "Club Team";
        if (oppositionTeamName == null || oppositionTeamName.isEmpty()) oppositionTeamName = "Opposition Team";
        
        if (actionsList != null && !actionsList.isEmpty()) {
            for (int i = 0; i < actionsList.size(); i++) {
                ScoringAttempt attempt = actionsList.get(i);
                // Set actual team names
                attempt.setTeamNames(clubTeamName, oppositionTeamName);
                exportFileContent.append(attempt.toString()); // Relies on ScoringAttempt.toString()
                if (i < actionsList.size() - 1) {      // Add a newline for all but the last item
                    exportFileContent.append("\n");
                }
            }
        }

        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName); // File name
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "text/plain"); // File MIME type
            fileName = "Netball Score-" + new SimpleDateFormat("yyyy-MM-dd-hh:mm", Locale.getDefault()).format(new Date()) + " " + viewModel.getActiveTeamName().getValue() + " v " + viewModel.getTeam2Name().getValue() + ".txt";

            // For Android Q (API 29) and above, save to the "Downloads" collection
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
                //uri = requireContext().getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues);
            } else {
                // For older versions, save to the public Downloads directory
                // This requires WRITE_EXTERNAL_STORAGE permission for API < 29
                File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                if (!downloadsDir.exists()) {
                    downloadsDir.mkdirs(); // Create the directory if it doesn't exist
                }
                File file = new File(downloadsDir, fileName);
                //uri = Uri.fromFile(file); // Get Uri from file path for older versions
            }

            /*if (uri == null) {
                Toast.makeText(getContext(), "Failed to create file for saving.", Toast.LENGTH_SHORT).show();
                return gameLogContent;
            }*/

            //fos = requireContext().getContentResolver().openOutputStream(uri);
            if (true) {
                StringBuilder allActions = new StringBuilder();
                exportFileContent = new StringBuilder(0);
                exportFileContent.append(" " + viewModel.getActiveTeamName().getValue() + " vs " + viewModel.getTeam2Name().getValue());
                exportFileContent.append("\n\n " + viewModel.getActiveTeamName().getValue() + " Score: " + viewModel.getTeam1Score().getValue());// viewModel.getTeam1Score());
                exportFileContent.append("\n " + viewModel.getTeam2Name().getValue() + " Score: " + viewModel.getTeam2Score().getValue());
                exportFileContent.append("\n");

                // Use the new method with team names
                Map<String, PlayerShotStats> playerShootingStats = ShotAnalyser.analyzeShotData(gameLogContent, clubTeamName, oppositionTeamName);

                if (!playerShootingStats.isEmpty()) {
                    exportFileContent.append("\n--- Player Shooting Stats ---\n");
                    for (Map.Entry<String, PlayerShotStats> entry : playerShootingStats.entrySet()) {
                        String statsKey = entry.getKey();
                        PlayerShotStats stats = entry.getValue();
                        int totalShots = stats.getGoals() + stats.getMisses();
                        double accuracy = 0.0;
                        
                        if (!statsKey.contains("-------========-------")) {
                            if (totalShots > 0) {
                                accuracy = ((double) stats.getGoals() / totalShots) * 100;
                            }
                            exportFileContent.append(String.format(Locale.getDefault(),
                                    "%s: %s - %d Goals, %d Misses (Accuracy: %.1f%%)\n",
                                    stats.teamName, stats.playerName, 
                                    stats.getGoals(), stats.getMisses(), accuracy));
                        }
                    }
                    exportFileContent.append("---------------------------\n");
                } else {
                    exportFileContent.append("\nNo shooting data to analyze for player percentages.\n");
                }
                
                // Add Game Timeline section title
                exportFileContent.append("\n--- Game Timeline ---\n");
                exportFileContent.append("Team Name, Position, Action, Player Name\n");
                
                StringBuilder sb = new StringBuilder();
                String[] lines = gameLogContent.split("\\R");
                for (int i = 0; i < lines.length; i++) {
                    String line = lines[i];
                    if (line.contains("-------========-------")) {
                        sb.append("-------========-------");
                    } else {
                        sb.append(line);
                    }
                    if (i < lines.length - 1) {
                        sb.append("\n");
                    }
                }
                String resultStringLoop = sb.toString();
                exportFileContent.append("\n" + resultStringLoop);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return exportFileContent.toString();
    }

    private void saveGameStats() {
        String gameLogContent = viewModel.getCurrentActionsLogString();
        StringBuilder exportFileContent = new StringBuilder();
        String fileName = new String();
        OutputStream fos = null; // Use OutputStream
        Uri uri = null;
        //StringBuilder exportFileContent = new StringBuilder();
        fileName = "Netball Score-" + new SimpleDateFormat("yyyy-MM-dd-hh:mm", Locale.getDefault()).format(new Date()) + " " + viewModel.getActiveTeamName().getValue() + " v " + viewModel.getTeam2Name().getValue() + ".txt";

        List<ScoringAttempt> actionsList = viewModel.getAllActions().getValue();
        String clubTeamName = viewModel.getActiveTeamName().getValue();
        String oppositionTeamName = viewModel.getTeam2Name().getValue();
        
        // Ensure team names are not null or empty
        if (clubTeamName == null || clubTeamName.isEmpty()) clubTeamName = "Club Team";
        if (oppositionTeamName == null || oppositionTeamName.isEmpty()) oppositionTeamName = "Opposition Team";
        
        if (actionsList != null && !actionsList.isEmpty()) {
            for (int i = 0; i < actionsList.size(); i++) {
                ScoringAttempt attempt = actionsList.get(i);
                // Set actual team names
                attempt.setTeamNames(clubTeamName, oppositionTeamName);
                
                if (!attempt.toString().contains("-------========-------")) {
                    exportFileContent.append(attempt.toString());
                } else {
                    exportFileContent.append("-------========-------");
                }
                if (i < actionsList.size() - 1) {      // Add a newline for all but the last item
                    exportFileContent.append("\n");
                }
            }
        }

        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName); // File name
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "text/plain"); // File MIME type


            // For Android Q (API 29) and above, save to the "Downloads" collection
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
                uri = requireContext().getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues);
            } else {
                // For older versions, save to the public Downloads directory
                // This requires WRITE_EXTERNAL_STORAGE permission for API < 29
                File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                if (!downloadsDir.exists()) {
                    downloadsDir.mkdirs(); // Create the directory if it doesn't exist
                }
                File file = new File(downloadsDir, fileName);
                uri = Uri.fromFile(file); // Get Uri from file path for older versions
            }

            if (uri == null) {
                Toast.makeText(getContext(), "Failed to create file for saving.", Toast.LENGTH_SHORT).show();
            }

            fos = requireContext().getContentResolver().openOutputStream(uri);
            if (fos != null) {
                StringBuilder allActions = new StringBuilder();
                exportFileContent = new StringBuilder(0);
                exportFileContent.append(" " + viewModel.getActiveTeamName().getValue() + " vs " + viewModel.getTeam2Name().getValue());
                exportFileContent.append("\n\n " + viewModel.getActiveTeamName().getValue() + " Score: " + viewModel.getTeam1Score().getValue());// viewModel.getTeam1Score());
                exportFileContent.append("\n " + viewModel.getTeam2Name().getValue() + " Score: " + viewModel.getTeam2Score().getValue());
                exportFileContent.append("\n");

                // Use the new method with team names
                Map<String, PlayerShotStats> playerShootingStats = ShotAnalyser.analyzeShotData(gameLogContent, clubTeamName, oppositionTeamName);

                if (!playerShootingStats.isEmpty()) {
                    exportFileContent.append("\n--- Player Shooting Stats ---\n");
                    for (Map.Entry<String, PlayerShotStats> entry : playerShootingStats.entrySet()) {
                        String statsKey = entry.getKey();
                        PlayerShotStats stats = entry.getValue();
                        int totalShots = stats.getGoals() + stats.getMisses();
                        double accuracy = 0.0;
                        
                        if (!statsKey.contains("-------========-------")) {
                            if (totalShots > 0) {
                                accuracy = ((double) stats.getGoals() / totalShots) * 100;
                            }
                            exportFileContent.append(String.format(Locale.getDefault(),
                                    "%s: %s - %d Goals, %d Misses (Accuracy: %.1f%%)\n",
                                    stats.teamName, stats.playerName, 
                                    stats.getGoals(), stats.getMisses(), accuracy));
                        }
                    }
                    exportFileContent.append("---------------------------\n");
                } else {
                    exportFileContent.append("\nNo shooting data to analyze for player percentages.\n");
                }
                
                // Add Game Timeline section title
                exportFileContent.append("\n--- Game Timeline ---\n");
                exportFileContent.append("Team Name, Position, Action, Player Name\n");

                StringBuilder sb = new StringBuilder();
                String[] lines = gameLogContent.split("\\R");
                for (int i = 0; i < lines.length; i++) {
                    String line = lines[i];
                    if (line.contains("-------========-------")) {
                        sb.append("-------========-------");
                    } else {
                        sb.append(line);
                    }
                    if (i < lines.length - 1) {
                        sb.append("\n");
                    }
                }
                String resultStringLoop = sb.toString();
                exportFileContent.append("\n" + resultStringLoop);

                //sAllActions=String.valueOf(sbExportStats);
                fos.write(exportFileContent.toString().getBytes()); // Write stats content to file
                
                // Save game stats to database
                String gameDate = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
                String team1Name = viewModel.getActiveTeamName().getValue();
                String team2Name = viewModel.getTeam2Name().getValue();
                int team1Score = viewModel.getTeam1Score().getValue();
                int team2Score = viewModel.getTeam2Score().getValue();
                
                // Get game configuration
                String gameMode = viewModel.getGameMode().getValue();
                String gameStartTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
                int periodDuration = 15; // Default to 15 minutes if not available
                
                // Extract period duration from game mode if possible
                if (gameMode != null && gameMode.contains("m,")) {
                    try {
                        String durationStr = gameMode.substring(0, gameMode.indexOf("m,"));
                        periodDuration = Integer.parseInt(durationStr);
                    } catch (Exception e) {
                        // Use default if parsing fails
                    }
                }
                
                // Create GameStats object with the new fields
                GameStats stats = new GameStats(gameDate, gameStartTime, team1Name, team2Name, 
                                              team1Score, team2Score, gameMode, periodDuration);
                
                // Create GameAction objects for each action
                List<GameAction> gameActions = new ArrayList<>();
                
                if (actionsList != null && !actionsList.isEmpty()) {
                    for (int i = 0; i < actionsList.size(); i++) {
                        ScoringAttempt attempt = actionsList.get(i);
                        // Set actual team names
                        attempt.setTeamNames(clubTeamName, oppositionTeamName);
                        
                        // Create a GameAction from the ScoringAttempt
                        GameAction action = new GameAction(
                            0, // This will be updated after GameStats is inserted
                            attempt.getTeamName(),
                            attempt.getPlayerPosition(),
                            attempt.isSuccessful() ? "Goal" : "Miss",
                            attempt.getPlayerName(),
                            attempt.getTimestamp(),
                            i // Sequence number
                        );
                        gameActions.add(action);
                    }
                }
                
                // Save both GameStats and GameActions
                viewModel.insertGameStats(stats, gameActions);
                
                // Mark the game as saved to prevent further edits
                viewModel.setGameSaved(true);
                viewModel.setGameInProgress(false);
                
                Toast.makeText(getContext(), "Game saved. No further edits allowed.", Toast.LENGTH_SHORT).show();
            } else {
                //Toast.makeText(getContext(), "Failed to open output stream.", Toast.LENGTH_SHORT).show();
            }

        } catch (IOException e) {
            Toast.makeText(getContext(), "Failed to save stats: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}