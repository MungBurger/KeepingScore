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
    private Button btnAddPlayer, btnSaveTeam, btnDeleteCurrentTeam, btnEditTeam,btnAddNewTeam,btnDeleteAllTeams;
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
/*
        etPlayerName = view.findViewById(R.id.etPlayerName);
        btnAddPlayer = view.findViewById(R.id.btnAddPlayer);
*/
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
        /*etPlayerName.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {

                addPlayer(); // Call the addPlayer method
                etPlayerName.requestFocus(); // Ensure focus stays on the input field
                return true; // Consume the action to prevent focus changes
            }
            return false; // Allow default behavior otherwise
        });*/

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

        //btnAddPlayer.setOnClickListener(v -> addPlayer());

        // Existing button logic (e.g., save, choose, edit teams)
        btnSaveTeam.setOnClickListener(v -> saveTeam());
        btnAddNewTeam.setOnClickListener(v ->addNewTeam());
        btnDeleteCurrentTeam.setOnClickListener(v -> deleteCurrentTeam());
        btnDeleteAllTeams.setOnClickListener(v ->deleteAllTeams());
        // Set up the dropdown for team selection
        setupTeamDropdown();


        viewModel.getActiveTeam().observe(getViewLifecycleOwner(), activeTeam -> {
            if (activeTeam != null && activeTeam.getPlayers() != null && !activeTeam.getPlayers().isEmpty()) {
                playerNames.clear();
                playerNames.addAll(activeTeam.getPlayers());
                playerAdapter.notifyDataSetChanged();
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
        Team teamToSave;

        if (currentActiveTeam != null && currentActiveTeam.getId() != 0) {
            // Case 1: We are saving an EXISTING team (it has a non-zero ID)
            teamToSave = currentActiveTeam;
            teamToSave.setTeamName(teamName);

            // **NEW LOGIC: Iterate RecyclerView to get updated player names for an EXISTING team**
            ArrayList<Player> updatedPlayersFromRV = getPlayersFromRecyclerView();
            teamToSave.setPlayers(updatedPlayersFromRV);
            viewModel.updateTeam(teamToSave);
            Log.d("SaveTeamDebug", "Requested update for existing team: " + teamName);
            Toast.makeText(getContext(), "Team updated!", Toast.LENGTH_SHORT).show();
            //End new

            Log.d("SaveTeamDebug", "Saving existing team: " + teamName + " with ID: " + currentActiveTeam.getId());
            // Update the properties of the existing Team object


            //Maybe Remove this bit
            /*currentActiveTeam.setTeamName(teamName);
            currentActiveTeam.setPlayers(new ArrayList<>(playerNames));
            viewModel.updateTeam(currentActiveTeam);
            Log.d("SaveTeamDebug", "Requested update for existing team: " + teamName);
            Toast.makeText(getContext(), "Team updated!", Toast.LENGTH_SHORT).show();*/
            // Maybe remove above
        } else {
            Log.d("SaveTeamDebug", "Saving new team: " + teamName);
            if (currentActiveTeam != null && currentActiveTeam.getTeamName().equals(teamName) && currentActiveTeam.getId() == 0) {
                teamToSave = currentActiveTeam; // Re-use the shell created by proceedWithAddNewTeam
            } else {
                teamToSave = new Team(teamName);
            }
            // **NEW LOGIC: Iterate RecyclerView to get updated player names for a NEW team**
            ArrayList<Player> updatedPlayersFromRV = getPlayersFromRecyclerView();
            teamToSave.setPlayers(updatedPlayersFromRV);
            // newTeamToSave.setScore(0); // Set default score if needed
            viewModel.insertTeam(teamToSave);
            Log.d("SaveTeamDebug", "Requested insert for new team: " + teamName);
            Toast.makeText(getContext(), "New team saved!", Toast.LENGTH_SHORT).show();
            //End of new code

            // Our old code
/*            Log.d("SaveTeamDebug", "Saving new team: " + teamName);
            Team newTeam = new Team(teamName); // Create a new Team object
            newTeam.setPlayers(new ArrayList<>(playerNames));
            newTeam.setScore(0); // Assuming a default score for a new team

            viewModel.insertTeam(newTeam);
            Log.d("SaveTeamDebug", "Requested insert for new team: " + teamName);
            //viewModel.setActiveTeamName(teamName);
            Toast.makeText(getContext(), "New team saved!", Toast.LENGTH_SHORT).show();*/
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

        // etTeamName.requestFocus(); // Not strictly necessary now as name is set
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
            playerAdapter.notifyDataSetChanged();
            Log.d("TeamListDebug", "proceedWithAddNewTeam: playerAdapter.notifyDataSetChanged() called. playerNames size: " + playerNames.size());
        } else {
            Log.e("TeamListError", "proceedWithAddNewTeam: playerAdapter IS NULL.");
        }

        isInitiatingNewTeam = true;
        viewModel.setActiveTeam(new Team(teamName)); // Or: new Team(teamName) if your ViewModel needs it immediately
        // but null is fine if the save operation creates the full Team object.
        Log.d("TeamListDebug", "proceedWithAddNewTeam: "+ viewModel.getActiveTeam());

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

        // If all standard positions are filled, assign further "Sub" positions
        // You might want a limit on total subs, e.g., up to 3 or 5 total subs.
        // For this example, if initial 10 players include 3 subs, the 11th could be Sub 4.
        return "Sub"; // Or "Reserve", "Bench"
        // Or you could count existing "Sub" players and return "Sub " + (count + 1)
        // For simplicity, just returning "Sub" is fine.
    }

    private void updatePlayerNameHint() {
        if (etPlayerName != null) {
            String nextPos = getNextAvailablePosition();
            int currentPlayers = playerNames.size();

            if (currentPlayers >= 10) { // Or a specific limit like Player.POSITIONS.length + 3 subs
                // All initial 10 spots filled, or more
                etPlayerName.setHint("Player Name (e.g., Sub)");
                // Alternatively, disable adding more players if 10 is the hard limit for creation
                // btnAddPlayer.setEnabled(false);
            } else if (nextPos.equalsIgnoreCase("Sub")) {
                etPlayerName.setHint("Enter Name (Sub)");
            }
            else {
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
/*    private void addPlayer() {
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
    }*/

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

/*        viewModel.getTeams().observe(getViewLifecycleOwner(), teams -> {
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
        });*/

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
                playerNames.clear();
                playerNames.addAll(activeTeam.getPlayers());
                playerAdapter.notifyDataSetChanged();
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
        // Optionally change appearance, e.g., alpha
        // float alpha = enabled ? 1.0f : 0.5f;
        // if (etPlayerName != null) etPlayerName.setAlpha(alpha);
        // if (btnAddPlayer != null) btnAddPlayer.setAlpha(alpha);
    }
    private ArrayList<Player> getPlayersFromRecyclerView() {
        ArrayList<Player> playersFromRV = new ArrayList<>();
        if (playerAdapter == null || rvPlayerNames == null) { // rvPlayerList is your RecyclerView instance
            Log.e("SaveTeamDebug", "getPlayersFromRecyclerView: Adapter or RecyclerView is null");
            // Fallback: return the original playerNames list if UI cannot be accessed
            // This might happen if save is called when view is not fully available.
            // Consider if `this.playerNames` (the original list) is a better fallback or an empty list.
            return new ArrayList<>(this.playerNames); // Or new ArrayList<>();
        }

        // Iterate through the items currently managed by the adapter.
        // This assumes the adapter's internal list (`playerList` in PlayerAdapter)
        // has the same size and order as what's potentially visible
        // and that its Player objects still hold the original positions.

        for (int i = 0; i < playerAdapter.getItemCount(); i++) {
            PlayerAdapter.PlayerViewHolder viewHolder = (PlayerAdapter.PlayerViewHolder) rvPlayerNames.findViewHolderForAdapterPosition(i);
            Player originalPlayer = playerAdapter.getPlayerAt(i); // Need a method in adapter to get Player at position

            if (viewHolder != null) {
                // View is visible and recycled, get text directly from EditText
                String editedName = viewHolder.etPlayerNameItem.getText().toString();
                String position = originalPlayer.getPosition(); // Get original position
                playersFromRV.add(new Player(editedName, position));
            } else {
                // View is not visible (scrolled off-screen).
                // This is the tricky part. If the adapter doesn't update its internal list,
                // we can't reliably get the edited text for off-screen items.
                // THIS APPROACH HAS A MAJOR FLAW FOR OFF-SCREEN ITEMS if adapter's list isn't updated.

                // To SOLVE the off-screen issue IF PlayerAdapter's list ISN'T updated:
                // This entire getPlayersFromRecyclerView() approach is flawed if PlayerAdapter's
                // playerList is not the single source of truth for names.

                // *** PREFERRED REVISED STRATEGY (see point 6 below) ***
                // For now, let's assume we can get the original player and if it wasn't visible,
                // we assume its name didn't change (which might be wrong).
                // OR, better, PlayerAdapter *should* update its internal list but just not
                // propagate it back to Frag_TeamList's original playerNames list until save.
                Log.w("SaveTeamDebug", "ViewHolder not found for position " + i + ". Using original name for player: " + originalPlayer.getName());
                playersFromRV.add(new Player(originalPlayer.getName(), originalPlayer.getPosition())); // Fallback to original, might be incorrect
            }
        }
        return playersFromRV;
    }
}