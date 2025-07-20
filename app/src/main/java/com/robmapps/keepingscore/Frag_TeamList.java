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
import android.text.InputType;
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
    private Button btnAddPlayer, btnSaveTeam, btnDeleteCurrentTeam, btnEditTeam, btnAddNewTeam, btnDeleteAllTeams;
    private Spinner spTeamList; // Declare the Spinner for team selection
    private boolean hasUnsavedChanges = false; // Flag for tracking unsaved changes
    private boolean isInitiatingNewTeam = false;
    private ArrayList<Player> playerNames = new ArrayList<>();

    private PlayerAdapter playerAdapter; // Declare at the class level

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_team_list, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        etTeamName = view.findViewById(R.id.etTeamName);

        btnSaveTeam = view.findViewById(R.id.btnSaveTeam);
        btnDeleteCurrentTeam = view.findViewById(R.id.btnDeleteCurrentTeam);
        btnDeleteAllTeams = view.findViewById(R.id.btnDeleteAllTeams);
        btnAddNewTeam = view.findViewById(R.id.btnAddNewTeam);
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
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                hasUnsavedChanges = true; // Mark changes as unsaved
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

// Initialize RecyclerView and PlayerAdapter
        playerAdapter = new PlayerAdapter(playerNames, new PlayerAdapter.OnPlayerActionListener() {
            @Override
            public void onPlayerDeleted(Player player) {
                int position = playerNames.indexOf(player);
                if (position != -1) {
                    playerNames.remove(player);
                    playerAdapter.notifyItemRemoved(position);
                    // Notify any subsequent items that their positions changed
                    if (position < playerNames.size()) {
                        playerAdapter.notifyItemRangeChanged(position, playerNames.size() - position);
                    }
                }
            }

            @Override
            public void onPositionChanged(Player player) {
                // Handle position change (if needed)
            }
        });
        rvPlayerNames.setAdapter(playerAdapter);
        rvPlayerNames.setLayoutManager(new LinearLayoutManager(requireContext()));

        //btnAddPlayer.setOnClickListener(v -> addPlayer());

        // Existing button logic (e.g., save, choose, edit teams)
        btnSaveTeam.setOnClickListener(v -> saveTeam());
        btnAddNewTeam.setOnClickListener(v -> addNewTeam());
        btnDeleteCurrentTeam.setOnClickListener(v -> deleteCurrentTeam());
        btnDeleteAllTeams.setOnClickListener(v -> deleteAllTeams());
        // Set up the dropdown for team selection
        setupTeamDropdown();

        viewModel.getActiveTeam().observe(getViewLifecycleOwner(), activeTeam -> {
            if (activeTeam != null && activeTeam.getPlayers() != null && !activeTeam.getPlayers().isEmpty()) {
                // Save the old size for comparison
                int oldSize = playerNames.size();
                playerNames.clear();
                playerNames.addAll(activeTeam.getPlayers());
                
                // Use more specific notify methods
                if (oldSize == 0) {
                    // If the list was empty before, use notifyItemRangeInserted
                    playerAdapter.notifyItemRangeInserted(0, playerNames.size());
                } else if (playerNames.size() == 0) {
                    // If the list is now empty, use notifyItemRangeRemoved
                    playerAdapter.notifyItemRangeRemoved(0, oldSize);
                } else {
                    // Otherwise, use notifyItemRangeChanged for existing items
                    int minSize = Math.min(oldSize, playerNames.size());
                    playerAdapter.notifyItemRangeChanged(0, minSize);
                    
                    // If new list is larger, insert the additional items
                    if (playerNames.size() > oldSize) {
                        playerAdapter.notifyItemRangeInserted(oldSize, playerNames.size() - oldSize);
                    } 
                    // If new list is smaller, remove the extra items
                    else if (oldSize > playerNames.size()) {
                        playerAdapter.notifyItemRangeRemoved(playerNames.size(), oldSize - playerNames.size());
                    }
                }
                Log.d("RecyclerViewDebug", "RecyclerView updated with " + playerNames.size() + " players.");
            } else {
                Log.w("RecyclerViewDebug", "No players found for the active team here either.");
            }
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

    private void saveTeam() {
        String teamName = etTeamName.getText().toString().trim();
        if (teamName.isEmpty()) {
            Toast.makeText(getContext(), "Please enter a team name", Toast.LENGTH_SHORT).show();
            return;
        }

        Team currentActiveTeam = viewModel.getActiveTeam().getValue();
        Team teamToSave;

        if (currentActiveTeam != null && currentActiveTeam.getId() != 0) {
            // Case 1: We are saving an EXISTING team (it has a non-zero ID)
            teamToSave = currentActiveTeam;
            teamToSave.setTeamName(teamName);

            ArrayList<Player> updatedPlayersFromRV = getPlayersFromRecyclerView();
            teamToSave.setPlayers(updatedPlayersFromRV);
            viewModel.updateTeam(teamToSave);
            Log.d("SaveTeamDebug", "Requested update for existing team: " + teamName);
            Toast.makeText(getContext(), "Team updated!", Toast.LENGTH_SHORT).show();

            Log.d("SaveTeamDebug", "Saving existing team: " + teamName + " with ID: " + currentActiveTeam.getId());
            // Update the properties of the existing Team object
        } else {
            Log.d("SaveTeamDebug", "Saving new team: " + teamName);
            if (currentActiveTeam != null && currentActiveTeam.getTeamName().equals(teamName) && currentActiveTeam.getId() == 0) {
                teamToSave = currentActiveTeam; // Re-use the shell created by proceedWithAddNewTeam
            } else {
                teamToSave = new Team(teamName);
            }
            ArrayList<Player> updatedPlayersFromRV = getPlayersFromRecyclerView();
            teamToSave.setPlayers(updatedPlayersFromRV);

            viewModel.insertTeam(teamToSave);
            Log.d("SaveTeamDebug", "Requested insert for new team: " + teamName);
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

                    etTeamName.setText("");
                    int oldSize = playerNames.size();
                    playerNames.clear();
                    // Use notifyItemRangeRemoved instead of notifyDataSetChanged
                    if (oldSize > 0) {
                        playerAdapter.notifyItemRangeRemoved(0, oldSize);
                    }
                    viewModel.setActiveTeamName("");

                    updateSpinner();

                    Toast.makeText(getContext(), "Team \"" + teamName + "\" deleted.", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void addNewTeam() {
        if (hasUnsavedChanges) {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Unsaved Changes")
                    .setMessage("You have unsaved changes. Are you sure you want to discard them and create a new team?")
                    .setPositiveButton("Discard", (dialog, which) -> {
                        // User chose to discard, now show the new team name dialog
                        showEnterTeamNameDialog();
                        hasUnsavedChanges = false; // Reset flag as we are proceeding with a new action
                    })
                    .setNegativeButton("Cancel", null) // User canceled discarding changes
                    .show();
        } else {
            // No unsaved changes, directly show the new team name dialog
            showEnterTeamNameDialog();
        }
    }

    private void showEnterTeamNameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Create New Team");
        builder.setMessage("Please enter the name for the new team:");

        // Set up the input
        final EditText inputTeamName = new EditText(requireContext());
        inputTeamName.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        inputTeamName.setHint("Team Name");
        builder.setView(inputTeamName);

        // Set up the buttons
        builder.setPositiveButton("Create", (dialog, which) -> {
            String teamName = inputTeamName.getText().toString().trim();
            if (teamName.isEmpty()) {
                Toast.makeText(getContext(), "Team name cannot be empty", Toast.LENGTH_SHORT).show();
                // Optionally, you could keep the dialog open or re-show it,
                // but for simplicity, we'll just show a toast and the user can try again.
            } else {
                // We have a valid team name, now proceed with setting it and adding players
                proceedWithAddNewTeam(teamName);
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void proceedWithAddNewTeam(String teamName) {

        viewModel.setActiveTeam(new Team(teamName));
        playerNames.clear();
        Log.d("TeamListDebug", "proceedWithAddNewTeam: playerNames cleared. Size: " + playerNames.size());

        if (Player.POSITIONS != null) {
            for (int i = 0; i < 10; i++) {
                String position;
                String playerName;
                if (i < Player.POSITIONS.length) {
                    position = Player.POSITIONS[i];
                    playerName = Player.POSITIONS[i].toString();
                } else {
                    position = "Sub";
                    playerName = "Sub";
                }
                playerNames.add(new Player(playerName, position));
            }
            Log.d("TeamListDebug", "proceedWithAddNewTeam: 10 players added. Size: " + playerNames.size());
        } else {
            Log.e("TeamListError", "proceedWithAddNewTeam: Player.POSITIONS IS NULL.");
        }
        Team activeTeam = viewModel.getActiveTeam().getValue();
        activeTeam.setPlayers(new ArrayList<>(playerNames));

        if (playerAdapter != null) {
            // Use notifyItemRangeInserted instead of notifyDataSetChanged
            playerAdapter.notifyItemRangeInserted(0, playerNames.size());
            Log.d("TeamListDebug", "proceedWithAddNewTeam: playerAdapter.notifyItemRangeInserted called. playerNames size: " + playerNames.size());
        } else {
            Log.e("TeamListError", "proceedWithAddNewTeam: playerAdapter IS NULL.");
        }

        isInitiatingNewTeam = true;
        viewModel.setActiveTeam(new Team(teamName)); // Or: new Team(teamName) if your ViewModel needs it immediately
        // but null is fine if the save operation creates the full Team object.
        Log.d("TeamListDebug", "proceedWithAddNewTeam: " + viewModel.getActiveTeam());

        updatePlayerNameHint();
        updatePlayerControlsState(false); // Or true, if having a team name means some controls are enabled
        hasUnsavedChanges = true; // New team with players is an unsaved change

        Log.d("TeamList", "New team '" + teamName + "' initiated with 10 default players. Final playerNames size: " + playerNames.size());
        etTeamName.setText(teamName); // Set the team name from the dialog to the EditText
        saveTeam();
        hasUnsavedChanges = false; // New team with players is an unsaved change
    }

    private String getNextAvailablePosition() {
        List<String> occupiedPositions = new ArrayList<>();
        int subCount = 0;
        if (playerNames != null) {
            for (Player player : playerNames) {
                if (player.getPosition() != null) {
                    occupiedPositions.add(player.getPosition());
                    if (player.getPosition().equalsIgnoreCase("Sub")) { // Or "Bench", etc.
                        subCount++;
                    }
                }
            }
        }

        if (Player.POSITIONS == null) {
            return "Player Name";
        }

        // Check standard positions first
        for (String pos : Player.POSITIONS) {
            if (!occupiedPositions.contains(pos)) {
                return pos;
            }
        }
        return "Sub"; // Or "Reserve", "Bench"
    }

    private void updatePlayerNameHint() {
        if (etPlayerName != null) {
            String nextPos = getNextAvailablePosition();
            int currentPlayers = playerNames.size();

            if (currentPlayers >= 10) { // Or a specific limit like Player.POSITIONS.length + 3 subs
                etPlayerName.setHint("Player Name (e.g., Sub)");
            } else if (nextPos.equalsIgnoreCase("Sub")) {
                etPlayerName.setHint("Enter Name (Sub)");
            } else {
                etPlayerName.setHint("Enter " + nextPos + " Name");
            }
        }
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

                    viewModel.setTeams(new HashMap<>());
                    updateSpinner();
                    Toast.makeText(getContext(), "All Teams deleted.", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    dialog.dismiss(); // Do nothing, user canceled.
                })
                .show();
    }

    public interface OnTeamActionListener {
        void onTeamSaved(String teamName, ArrayList<String> playerNames);

        void onTeamChosen();

        void onTeamEdited(String teamName, ArrayList<String> playerNames);
    }

    private void chooseTeam(String teamName) {
        SharedViewModel viewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        viewModel.setActiveTeamName(teamName);

        Toast.makeText(requireContext(), "Active team set to: " + teamName, Toast.LENGTH_SHORT).show();
    }

    private void setupTeamDropdown() {
        SharedViewModel viewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        // Observe the active team LiveData to update the UI when it changes
        viewModel.getActiveTeam().observe(getViewLifecycleOwner(), activeTeam -> {
            Log.d("TeamSelectionDebug", "Active team LiveData updated: " + (activeTeam != null ? activeTeam.getTeamName() : "null"));

            if (activeTeam != null && activeTeam.getPlayers() != null) {
                playerNames.addAll(activeTeam.getPlayers());
            }
            playerAdapter.notifyDataSetChanged();

            // Update the EditText with the active team name
            if (activeTeam != null && !activeTeam.getTeamName().equals(etTeamName.getText().toString())) {
                etTeamName.setText(activeTeam.getTeamName());
            }
        });
        viewModel.getActiveTeam().observe(getViewLifecycleOwner(), activeTeam -> {
            if (activeTeam != null && activeTeam.getPlayers() != null && !activeTeam.getPlayers().isEmpty()) {
                // Save the old size for comparison
                int oldSize = playerNames.size();
                playerNames.clear();
                playerNames.addAll(activeTeam.getPlayers());
                
                // Use more specific notify methods
                if (oldSize == 0) {
                    // If the list was empty before, use notifyItemRangeInserted
                    playerAdapter.notifyItemRangeInserted(0, playerNames.size());
                } else if (playerNames.size() == 0) {
                    // If the list is now empty, use notifyItemRangeRemoved
                    playerAdapter.notifyItemRangeRemoved(0, oldSize);
                } else {
                    // Otherwise, use notifyItemRangeChanged for existing items
                    int minSize = Math.min(oldSize, playerNames.size());
                    playerAdapter.notifyItemRangeChanged(0, minSize);
                    
                    // If new list is larger, insert the additional items
                    if (playerNames.size() > oldSize) {
                        playerAdapter.notifyItemRangeInserted(oldSize, playerNames.size() - oldSize);
                    } 
                    // If new list is smaller, remove the extra items
                    else if (oldSize > playerNames.size()) {
                        playerAdapter.notifyItemRangeRemoved(playerNames.size(), oldSize - playerNames.size());
                    }
                }
                Log.d("RecyclerViewDebug", "RecyclerView updated with " + playerNames.size() + " players.");
            } else {
                Log.w("RecyclerViewDebug", "No players found for the active team.");
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
    }

    private ArrayList<Player> getPlayersFromRecyclerView() {
        ArrayList<Player> playersFromRV = new ArrayList<>();
        if (playerAdapter == null || rvPlayerNames == null) { // rvPlayerList is your RecyclerView instance
            Log.e("SaveTeamDebug", "getPlayersFromRecyclerView: Adapter or RecyclerView is null");
            return new ArrayList<>(this.playerNames); // Or new ArrayList<>();
        }

        for (int i = 0; i < playerAdapter.getItemCount(); i++) {
            PlayerAdapter.PlayerViewHolder viewHolder = (PlayerAdapter.PlayerViewHolder) rvPlayerNames.findViewHolderForAdapterPosition(i);
            Player originalPlayer = playerAdapter.getPlayerAt(i); // Need a method in adapter to get Player at position

            if (viewHolder != null) {
                String editedName = viewHolder.etPlayerNameItem.getText().toString();
                String position = originalPlayer.getPosition(); // Get original position
                playersFromRV.add(new Player(editedName, position));
            } else {
                Log.w("SaveTeamDebug", "ViewHolder not found for position " + i + ". Using original name for player: " + originalPlayer.getName());
                playersFromRV.add(new Player(originalPlayer.getName(), originalPlayer.getPosition())); // Fallback to original, might be incorrect
            }
        }
        return playersFromRV;
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("Frag_TeamList", "onPause called.");
        // Check if there are unsaved changes before attempting to save
        if (hasUnsavedChanges) {
            Log.d("Frag_TeamList", "Unsaved changes detected in onPause, calling saveTeam().");
            saveTeam(); // Call your existing saveTeam method
        } else {
            Log.d("Frag_TeamList", "No unsaved changes in onPause, not saving.");
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        super.onPause();
        Log.d("Frag_TeamList", "onPause called.");
        // Check if there are unsaved changes before attempting to save
        if (hasUnsavedChanges) {
            Log.d("Frag_TeamList", "Unsaved changes detected in onPause, calling saveTeam().");
            saveTeam(); // Call your existing saveTeam method
        } else {
            Log.d("Frag_TeamList", "No unsaved changes in onPause, not saving.");
        }
    }
}