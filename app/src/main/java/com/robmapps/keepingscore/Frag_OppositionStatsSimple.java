package com.robmapps.keepingscore;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
import java.util.Map;
import java.util.stream.Collectors;

public class Frag_OppositionStatsSimple extends Fragment {
    
    private SharedViewModel viewModel;
    private TextView statsTextView;
    private Spinner oppositionTeamSpinner;
    private Button backButton;
    private List<OppositionTeam> oppositionTeams = new ArrayList<>();
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_opposition_stats_simple, container, false);
        
        // Initialize ViewModel
        viewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        
        // Initialize views
        statsTextView = view.findViewById(R.id.opposition_stats_text);
        oppositionTeamSpinner = view.findViewById(R.id.opposition_team_spinner);
        backButton = view.findViewById(R.id.back_button);
        
        // Set up opposition team spinner
        setupOppositionTeamSpinner();
        
        // Set up back button
        backButton.setOnClickListener(v -> {
            // Go back to previous fragment
            requireActivity().getSupportFragmentManager().popBackStack();
        });
        
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
            
            // Display basic stats without complex analysis
            displayBasicStats(oppositionTeamName, games);
        });
    }
    
    private void displayBasicStats(String oppositionTeamName, List<GameStats> games) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("Stats against ").append(oppositionTeamName).append("\n\n");
        
        // Games summary
        int totalGames = games.size();
        int wins = 0;
        int losses = 0;
        int draws = 0;
        int goalsScored = 0;
        int goalsConceded = 0;
        
        for (GameStats game : games) {
            if (game.team1Score > game.team2Score) {
                wins++;
            } else if (game.team1Score < game.team2Score) {
                losses++;
            } else {
                draws++;
            }
            
            goalsScored += game.team1Score;
            goalsConceded += game.team2Score;
        }
        
        sb.append("Games played: ").append(totalGames).append("\n");
        sb.append("Record: ").append(wins).append("W - ").append(losses).append("L - ").append(draws).append("D\n");
        sb.append("Goals scored: ").append(goalsScored).append("\n");
        sb.append("Goals conceded: ").append(goalsConceded).append("\n");
        sb.append("Goal difference: ").append(goalsScored - goalsConceded).append("\n\n");
        
        // List individual games
        sb.append("Game History:\n");
        for (GameStats game : games) {
            sb.append(game.gameDate).append(": ")
              .append(game.team1Name).append(" ")
              .append(game.team1Score).append(" - ")
              .append(game.team2Score).append(" ")
              .append(game.team2Name).append("\n");
        }
        
        statsTextView.setText(sb.toString());
    }
}