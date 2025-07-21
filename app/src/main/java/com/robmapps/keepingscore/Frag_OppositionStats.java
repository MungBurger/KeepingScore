package com.robmapps.keepingscore;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.robmapps.keepingscore.database.entities.GameAction;
import com.robmapps.keepingscore.database.entities.GameStats;
import com.robmapps.keepingscore.database.entities.OppositionTeam;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class Frag_OppositionStats extends Fragment {
    
    private SharedViewModel viewModel;
    private TextView statsTextView;
    private Spinner oppositionTeamSpinner;
    private List<OppositionTeam> oppositionTeams = new ArrayList<>();
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_opposition_stats, container, false);
        
        // Initialize ViewModel
        viewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        
        // Initialize views
        statsTextView = view.findViewById(R.id.opposition_stats_text);
        oppositionTeamSpinner = view.findViewById(R.id.opposition_team_spinner);
        
        // Set up opposition team spinner
        setupOppositionTeamSpinner();
        
        return view;
    }
    
    private void setupOppositionTeamSpinner() {
        // Create adapter for the spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), 
                android.R.layout.simple_spinner_item, new ArrayList<>());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        oppositionTeamSpinner.setAdapter(adapter);
        
        // Observe opposition teams
        viewModel.getAllOppositionTeams().observe(getViewLifecycleOwner(), teams -> {
            if (teams != null && !teams.isEmpty()) {
                oppositionTeams = teams;
                List<String> teamNames = teams.stream()
                        .map(team -> team.teamName)
                        .collect(Collectors.toList());
                
                adapter.clear();
                adapter.addAll(teamNames);
                adapter.notifyDataSetChanged();
            }
        });
        
        // Handle selection
        oppositionTeamSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < oppositionTeams.size()) {
                    String oppositionTeamName = oppositionTeams.get(position).teamName;
                    String clubTeamName = viewModel.getActiveTeamName().getValue();
                    
                    if (clubTeamName != null && !clubTeamName.isEmpty()) {
                        loadStatsForOppositionTeam(clubTeamName, oppositionTeamName);
                    } else {
                        statsTextView.setText("Please select a club team first");
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }
    
    private void loadStatsForOppositionTeam(String clubTeamName, String oppositionTeamName) {
        // Show loading state
        statsTextView.setText("Loading stats...");
        
        // Get games against this opposition team
        viewModel.getGamesByOppositionTeam(clubTeamName, oppositionTeamName).observe(getViewLifecycleOwner(), games -> {
            if (games == null || games.isEmpty()) {
                statsTextView.setText("No games found against " + oppositionTeamName);
                return;
            }
            
            // Get game IDs to fetch actions
            List<Integer> gameIds = games.stream()
                    .map(game -> game.id)
                    .collect(Collectors.toList());
            
            // Get actions for these games
            LiveData<List<GameAction>> actionsLiveData = viewModel.getActionsForGames(gameIds);
            actionsLiveData.observe(getViewLifecycleOwner(), new Observer<List<GameAction>>() {
                @Override
                public void onChanged(List<GameAction> allActions) {
                    // Group actions by game ID
                    Map<Integer, List<GameAction>> actionsByGame = allActions.stream()
                            .collect(Collectors.groupingBy(action -> action.gameId));
                    
                    // Create list of action lists in the same order as games
                    List<List<GameAction>> orderedActions = new ArrayList<>();
                    for (GameStats game : games) {
                        orderedActions.add(actionsByGame.getOrDefault(game.id, new ArrayList<>()));
                    }
                    
                    // Analyze the stats
                    Map<String, Object> stats = TeamStatsAnalyzer.analyzeTeamPerformance(games, orderedActions);
                    
                    // Display the stats
                    displayStats(oppositionTeamName, stats);
                    
                    // Remove observer after processing
                    actionsLiveData.removeObserver(this);
                }
            });
        });
    }
    
    private void displayStats(String oppositionTeamName, Map<String, Object> stats) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("Stats against ").append(oppositionTeamName).append("\n\n");
        
        // Games summary
        int totalGames = (int) stats.getOrDefault("totalGames", 0);
        int wins = (int) stats.getOrDefault("wins", 0);
        int losses = (int) stats.getOrDefault("losses", 0);
        int draws = (int) stats.getOrDefault("draws", 0);
        
        sb.append("Games played: ").append(totalGames).append("\n");
        sb.append("Record: ").append(wins).append("W - ").append(losses).append("L - ").append(draws).append("D\n");
        
        // Goals
        int goalsScored = (int) stats.getOrDefault("goalsScored", 0);
        int goalsConceded = (int) stats.getOrDefault("goalsConceded", 0);
        
        sb.append("Goals scored: ").append(goalsScored).append("\n");
        sb.append("Goals conceded: ").append(goalsConceded).append("\n");
        sb.append("Goal difference: ").append(goalsScored - goalsConceded).append("\n\n");
        
        // Player stats
        Map<String, TeamStatsAnalyzer.PlayerPerformance> playerStats = 
                (Map<String, TeamStatsAnalyzer.PlayerPerformance>) stats.getOrDefault("playerStats", new HashMap<>());
        
        if (!playerStats.isEmpty()) {
            sb.append("Player Performance:\n");
            
            for (TeamStatsAnalyzer.PlayerPerformance player : playerStats.values()) {
                player.calculateAccuracy();
                sb.append(player.playerName)
                  .append(": ")
                  .append(player.goals)
                  .append(" goals, ")
                  .append(player.misses)
                  .append(" misses (")
                  .append(String.format("%.1f", player.accuracy))
                  .append("% accuracy)\n");
            }
        }
        
        statsTextView.setText(sb.toString());
    }
}