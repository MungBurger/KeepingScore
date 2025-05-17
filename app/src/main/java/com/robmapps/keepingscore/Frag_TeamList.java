package com.robmapps.keepingscore;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import androidx.room.Room;
import android.content.IntentFilter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.robmapps.keepingscore.database.entities.Team;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Frag_TeamList # newInstance} factory method to
 * create an instance of this fragment.
 */

public class Frag_TeamList extends Fragment {

    private SharedViewModel viewModel; // Declare viewModel at the class level
    private EditText etTeamName, etPlayerName;
    private RecyclerView rvPlayerNames;
    private Button btnAddPlayer, btnSaveTeam, btnDeleteCurrentTeam, btnEditTeam,btnAddNewTeam,btnDeleteAllTeams;
    private Spinner spTeamList; // Declare the Spinner for team selection
    private boolean hasUnsavedChanges = false; // Flag for tracking unsaved changes
    private ArrayList<Player> playerNames = new ArrayList<>();
    /*private TeamAdapter teamAdapter;*/
    private PlayerAdapter playerAdapter; // Declare at the class level

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_team_list, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        etTeamName = view.findViewById(R.id.etTeamName);
        etPlayerName = view.findViewById(R.id.etPlayerName);
        btnAddPlayer = view.findViewById(R.id.btnAddPlayer);
        btnSaveTeam = view.findViewById(R.id.btnSaveTeam);
        btnDeleteCurrentTeam = view.findViewById(R.id.btnDeleteCurrentTeam);
        btnDeleteAllTeams=view.findViewById(R.id.btnDeleteAllTeams);
        btnAddNewTeam=view.findViewById(R.id.btnAddNewTeam);
        spTeamList = view.findViewById(R.id.spTeamList); // Reference the Spinner from the XML layout
        rvPlayerNames = view.findViewById(R.id.rvPlayerNames);

        etTeamName.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) { // When the user finishes editing (loses focus)
                String teamName = etTeamName.getText().toString().trim();
                if (!teamName.isEmpty()) {
                    viewModel.setActiveTeamName(teamName); // Update the active team in the ViewModel
                } else {
                    Toast.makeText(requireContext(), "Team name cannot be empty", Toast.LENGTH_SHORT).show();
                }
            }
        });

        etTeamName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                hasUnsavedChanges = true; // Mark changes as unsaved
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
        etPlayerName.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {

                addPlayer(); // Call the addPlayer method
                etPlayerName.requestFocus(); // Ensure focus stays on the input field
                return true; // Consume the action to prevent focus changes
            }
            return false; // Allow default behavior otherwise
        });

// Initialize RecyclerView and PlayerAdapter
        playerAdapter = new PlayerAdapter(playerNames, new PlayerAdapter.OnPlayerActionListener() {
            @Override
            public void onPlayerDeleted(Player player) {
                playerNames.remove(player);
                playerAdapter.notifyDataSetChanged(); // No more error
            }

            @Override
            public void onPositionChanged(Player player) {
                // Handle position change (if needed)
            }
        });
        rvPlayerNames.setAdapter(playerAdapter);
        rvPlayerNames.setLayoutManager(new LinearLayoutManager(requireContext()));

        btnAddPlayer.setOnClickListener(v -> addPlayer());

        // Existing button logic (e.g., save, choose, edit teams)
        btnSaveTeam.setOnClickListener(v -> saveTeam());
        btnAddNewTeam.setOnClickListener(v ->addNewTeam());
        btnDeleteCurrentTeam.setOnClickListener(v -> deleteCurrentTeam());
        btnDeleteAllTeams.setOnClickListener(v ->deleteAllTeams());
        // Set up the dropdown for team selection
        setupTeamDropdown();

        viewModel.getActiveTeam().observe(getViewLifecycleOwner(), activeTeam -> {
            Log.d("ActiveTeamObserver", "Active team LiveData updated: " + (activeTeam != null ? activeTeam.getTeamName() : "null"));
            // This observer is triggered when the active team changes

            // Update the team name EditText
            if (activeTeam != null && !activeTeam.getTeamName().equals(etTeamName.getText().toString())) {
                etTeamName.setText(activeTeam.getTeamName());
                updatePlayerControlsState(true);
            } else if (activeTeam == null) {
                // Clear the team name if no active team
                updatePlayerControlsState(false);
                etTeamName.setText("");
            }

            // Update the player list
            playerNames.clear();
            if (activeTeam != null && activeTeam.getTeamName() != null) {
                playerNames.addAll(activeTeam.getPlayers());
            }
            playerAdapter.notifyDataSetChanged();
        });

        viewModel.getTeamsLive().observe(getViewLifecycleOwner(), teams -> {
            List<String> teamNames;
            if (teams != null && !teams.isEmpty()) {
                teamNames = teams.stream().map(team -> team.getTeamName()).collect(Collectors.toList());
            } else {
                teamNames = new ArrayList<>();
                teamNames.add("");
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                    requireContext(), android.R.layout.simple_spinner_dropdown_item, teamNames);
                    spTeamList.setAdapter(adapter);
        });
        return view;
    }
// In Frag_TeamList.java

    private void saveTeam() {
        String teamName = etTeamName.getText().toString().trim();
        if (teamName.isEmpty()) {
            Toast.makeText(getContext(), "Please enter a team name", Toast.LENGTH_SHORT).show();
            return;
        }

        // Access the current active team from the ViewModel's LiveData
        // This value is automatically updated by the ViewModel's activeTeam observer.
        Team currentActiveTeam = viewModel.getActiveTeam().getValue();

        if (currentActiveTeam != null && currentActiveTeam.getId() != 0) {
            // Case 1: We are saving an EXISTING team (it has a non-zero ID)
            Log.d("SaveTeamDebug", "Saving existing team: " + teamName + " with ID: " + currentActiveTeam.getId());
            // Update the properties of the existing Team object
            currentActiveTeam.setTeamName(teamName);
            currentActiveTeam.setPlayers(new ArrayList<>(playerNames));

            viewModel.updateTeam(currentActiveTeam);
            Log.d("SaveTeamDebug", "Requested update for existing team: " + teamName);

            Toast.makeText(getContext(), "Team updated!", Toast.LENGTH_SHORT).show();

        } else {
            // Case 2: We are saving a NEW team (activeTeam is null or has a zero ID)
            Log.d("SaveTeamDebug", "Saving new team: " + teamName);
            Team newTeam = new Team(teamName); // Create a new Team object
            newTeam.setPlayers(new ArrayList<>(playerNames));
            newTeam.setScore(0); // Assuming a default score for a new team

            viewModel.insertTeam(newTeam);
            Log.d("SaveTeamDebug", "Requested insert for new team: " + teamName);
            //viewModel.setActiveTeamName(teamName);
            Toast.makeText(getContext(), "New team saved!", Toast.LENGTH_SHORT).show();
        }
        hasUnsavedChanges = false; // Reset the unsaved changes flag
        updatePlayerControlsState(true);
    }
    private void deleteCurrentTeam() {
        String teamName = etTeamName.getText().toString().trim();

        if (teamName.isEmpty()) {
            Toast.makeText(getContext(), "No team selected to delete.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show a confirmation dialog
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Team")
                .setMessage("Are you sure you want to delete the team \"" + teamName + "\"? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    SharedViewModel viewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
                    viewModel.deleteTeam(teamName); // Remove the team from the ViewModel

                    // Refresh UI
                    etTeamName.setText("");
                    playerNames.clear();
                    playerAdapter.notifyDataSetChanged();
                    viewModel.setActiveTeamName("");

                    // **Update spinner after deletion**
                    updateSpinner();

                    Toast.makeText(getContext(), "Team \"" + teamName + "\" deleted.", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void addNewTeam() {
        // Clear the team name and player list
        etTeamName.setText(""); // Clear the team name EditText
        playerNames.clear(); // Clear the player list
        playerAdapter.notifyDataSetChanged(); // Refresh the RecyclerView

        // Reset the active team in the ViewModel
        SharedViewModel viewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        viewModel.setActiveTeamName(""); // Set active team name to an empty string
        Log.d("NewTeamDebug", "Active team reset to: " + viewModel.getActiveTeamName().getValue());
        updatePlayerControlsState(true);
        // Set focus to the team name field
        etTeamName.requestFocus(); // Move cursor to the EditText

        Toast.makeText(getContext(), "Create a new team!", Toast.LENGTH_SHORT).show();
    }
    private void deleteAllTeams() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete ALL Teams")
                .setMessage("Are you sure you want to delete ALL teams? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    // Obtain the SharedViewModel reference.
                    SharedViewModel viewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

                    // Delete persisted teams from the database.
                    viewModel.deleteAllTeams();
                    playerNames.clear();
                    // Clear the in-memory teams map if your UI depends on it.
                    viewModel.setTeams(new HashMap<>());

                    // Optionally refresh the UI (e.g., update the spinner).
                    updateSpinner();

                    Toast.makeText(getContext(), "All Teams deleted.", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    dialog.dismiss(); // Do nothing, user canceled.
                })
                .show();
    }
    private void addPlayer() {
        String playerName = etPlayerName.getText().toString().trim();
        if (!playerName.isEmpty()) {
            // Determine position for the new player
            List<String> occupiedPositions = new ArrayList<>();
            for (Player player : playerNames) {
                occupiedPositions.add(player.getPosition());
            }

            String position = "Off";
            for (String pos : Player.POSITIONS) {
                if (!occupiedPositions.contains(pos)) {
                    position = pos;
                    break;
                }
            }

            // Add the new player
            Player newPlayer = new Player(playerName, position);
            playerNames.add(newPlayer);

            // Notify the adapter of the change
            playerAdapter.notifyDataSetChanged();

            // Reset the input field
            etPlayerName.setText("");
            etPlayerName.requestFocus();
            hasUnsavedChanges = true;

            //Toast.makeText(getContext(), "Player added: " + playerName + " as " + position, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Enter a player name!", Toast.LENGTH_SHORT).show();
            etPlayerName.requestFocus();
        }
    }

    public interface OnTeamActionListener {
        void onTeamSaved(String teamName, ArrayList<String> playerNames);
        void onTeamChosen();
        void onTeamEdited(String teamName, ArrayList<String> playerNames);
    }

    private void chooseTeam(String teamName) {
        SharedViewModel viewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        viewModel.setActiveTeamName(teamName);

        // Display a Toast message in the Fragment
        Toast.makeText(requireContext(), "Active team set to: " + teamName, Toast.LENGTH_SHORT).show();
    }

    private void setupTeamDropdown() {
        SharedViewModel viewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        viewModel.getTeams().observe(getViewLifecycleOwner(), teams -> {
            if (teams != null && !teams.isEmpty()) {
                // Convert team keys to a list for the adapter
                List<String> teamNames = new ArrayList<>(teams.keySet());

                // Initialize the ArrayAdapter
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        requireContext(), // Use requireContext() for a valid context
                        android.R.layout.simple_spinner_dropdown_item, // Default dropdown layout
                        teamNames
                );
                // Set the adapter to the Spinner
                spTeamList.setAdapter(adapter);
                // Manually set the selection to match the active team
                String activeTeam = viewModel.getActiveTeamName().getValue();
                if (activeTeam != null) {
                    int position = adapter.getPosition(activeTeam);
                    spTeamList.setSelection(position);
                }
            } else {
                spTeamList.setAdapter(null); // Clear the dropdown if no teams exist
                //Toast.makeText(requireContext(), "No teams available", Toast.LENGTH_SHORT).show();
            }
        });

        // Observe the active team LiveData to update the UI when it changes
        viewModel.getActiveTeam().observe(getViewLifecycleOwner(), activeTeam -> {
            Log.d("TeamSelectionDebug", "Active team LiveData updated: " +
                    (activeTeam != null ? activeTeam.getTeamName() : "null"));

            //playerNames.clear();
            if (activeTeam != null && activeTeam.getPlayers() != null) {
                playerNames.addAll(activeTeam.getPlayers());
            }
            playerAdapter.notifyDataSetChanged();

            // Update the EditText with the active team name
            if (activeTeam != null && !activeTeam.getTeamName().equals(etTeamName.getText().toString())) {
                etTeamName.setText(activeTeam.getTeamName());
            }
        });

        spTeamList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedTeamName = parent.getItemAtPosition(position).toString();
                Log.d("TeamSelectionDebug", "Spinner selected team: " + selectedTeamName);
                // Setting the active team name will trigger the activeTeam LiveData observer
                viewModel.setActiveTeamName(selectedTeamName);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Optional: handle case where nothing is selected (might not be needed for Spinner)
            }
        });
    }
    private void updateSpinner() {
        //SharedViewModel viewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        HashMap<String, ArrayList<Player>> teams = viewModel.getTeams().getValue();
        if (teams != null) {
            List<String> teamNames = new ArrayList<>(teams.keySet());

            ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                    getContext(),
                    android.R.layout.simple_spinner_item,
                    teamNames
            );
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spTeamList.setAdapter(spinnerAdapter);

            String activeTeamName = viewModel.getActiveTeamName().getValue();
            if (activeTeamName != null) {
                int position = teamNames.indexOf(activeTeamName);
                if (position >= 0) {
                    spTeamList.setSelection(position); // Automatically select the active team
                }
            }
        }
    }
    private void updatePlayerControlsState(boolean enabled) {
        if (etPlayerName != null) {
            etPlayerName.setEnabled(enabled);
        }
        if (btnAddPlayer != null) {
            btnAddPlayer.setEnabled(enabled);
        }
        // Optionally change appearance, e.g., alpha
        // float alpha = enabled ? 1.0f : 0.5f;
        // if (etPlayerName != null) etPlayerName.setAlpha(alpha);
        // if (btnAddPlayer != null) btnAddPlayer.setAlpha(alpha);
    }
}