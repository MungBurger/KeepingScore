/*
package com.robmapps.keepingscore;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.media3.common.Player;
import androidx.media3.common.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PlayerVotingFragment extends Fragment {

    private SharedViewModel viewModel; // Your existing SharedViewModel
    // Or a new ViewModel specific for voting if preferred: private VotingViewModel votingViewModel;
    private GameStatsViewModel gameStatsViewModel; // To get game details and save votes

    private TextView tvVoterName;
    private Spinner spinnerVoteForPlayer;
    private Button btnSubmitVote;

    private List<Player> availablePlayers; // List of all players in the game
    private Player currentPlayerVoting;    // The player who is currently casting their vote
    private int currentPlayerVotingIndex = 0;
    private long currentGameId; // Assuming you pass this or get it from ViewModel

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.playervotingfragment, container, false);

        tvVoterName = view.findViewById(R.id.tvVoterName);
        spinnerVoteForPlayer = view.findViewById(R.id.spinnerVoteForPlayer);
        btnSubmitVote = view.findViewById(R.id.btnSubmitVote);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        // Initialize gameStatsViewModel, likely also shared from activity or using a factory
        // gameStatsViewModel = new ViewModelProvider(this,
        //      YourViewModelFactory(requireActivity().getApplication())).get(GameStatsViewModel.class);

// PlayerVotingFragment.java (Continued)

        // TODO: Get currentGameId (e.g., from arguments or SharedViewModel)
        // Example: if (getArguments() != null) { currentGameId = getArguments().getLong("GAME_ID_ARG", -1L); }
        // Or, if it's the game that just finished (you'd need to set this in SharedViewModel after a game):
        // viewModel.getJustCompletedGameId().observe(getViewLifecycleOwner(), gameId -> {
        //     if (gameId != null) {
        //         currentGameId = gameId;
        //         // Now that we have gameId, fetch players
        //         fetchPlayersForVoting();
        //     }
        // });
        // For now, let's assume currentGameId is somehow set.
        // You'll need to pass the actual Game ID for linking votes to a game.


        // --- Player Loading ---
        // Replace this with your actual player loading logic.
        // This might come from SharedViewModel, a list of players in the current game,
        // or a list of all active players in the app.
        // For this example, let's assume 'availablePlayers' is populated somehow.
        // It's crucial that these are 'Player' objects with IDs and names.
        // viewModel.getPlayersInCurrentGame().observe(getViewLifecycleOwner(), players -> {
        //     if (players != null && !players.isEmpty()) {
        //         availablePlayers = new ArrayList<>(players); // Make a mutable copy
        //         currentPlayerVotingIndex = 0; // Start with the first player
        //         setupNextVoter();
        //     } else {
        //         Toast.makeText(getContext(), "No players available for voting.", Toast.LENGTH_LONG).show();
        //         // Consider navigating back or showing an appropriate message
        //         // requireActivity().getSupportFragmentManager().popBackStack();
        //     }
        // });
        // For demonstration, manually populating. Replace with actual data loading.
        // You would typically get this from your SharedViewModel or a repository.
        // Ensure Player objects have at least 'id' and 'name'.
        // availablePlayers = getMockPlayers(); // Replace with actual player data source


        // Simulate fetching game ID and players
        // In a real app, this data would come from your ViewModel or arguments
        currentGameId = 1L; // Placeholder: you MUST set this to the actual completed game's ID
        loadPlayersAndStartVoting();


        btnSubmitVote.setOnClickListener(v -> handleSubmitVote());
    }

    private void loadPlayersAndStartVoting() {
        // Example: Fetch players from SharedViewModel or a repository based on currentGameId or current team setup
        // This is a placeholder. You need to implement how you get your list of players.
        // Let's assume you have a method in SharedViewModel like:
        // LiveData<List<Player>> getPlayersForCurrentGame();
        // For now, using a mock list.
        if (viewModel != null && viewModel.getTeam1Players().getValue() != null && viewModel.getTeam2Players().getValue() != null) {
            List<Player> team1 = viewModel.getTeam1Players().getValue();
            List<Player> team2 = viewModel.getTeam2Players().getValue();
            availablePlayers = new ArrayList<>();
            if (team1 != null) availablePlayers.addAll(team1);
            if (team2 != null) availablePlayers.addAll(team2);

            // Remove duplicates if a player could be on both lists somehow (unlikely for team players)
            // availablePlayers = availablePlayers.stream().distinct().collect(Collectors.toList());
        }


        if (availablePlayers == null || availablePlayers.isEmpty()) {
            // Fallback or create dummy data for testing if above fails
            availablePlayers = createMockPlayers(); // Replace with your actual player fetching
        }


        if (availablePlayers != null && !availablePlayers.isEmpty()) {
            currentPlayerVotingIndex = 0;
            setupNextVoter();
        } else {
            Toast.makeText(getContext(), "No players available for voting.", Toast.LENGTH_LONG).show();
            // Navigate back or disable UI
            if (getView() != null) {
                btnSubmitVote.setEnabled(false);
            }
        }
    }


    // Mock player list for example. Replace with your actual Player data.
    private List<Player> createMockPlayers() {
        List<Player> players = new ArrayList<>();
        // Assuming your Player class has an id and name
        // The ID should be the actual primary key from your Player table in the database
        players.add(new Player(1, "Player Alice", "Team A"));
        players.add(new Player(2, "Player Bob", "Team A"));
        players.add(new Player(3, "Player Charlie", "Team B"));
        players.add(new Player(4, "Player Diana", "Team B"));
        return players;
    }


    private void setupNextVoter() {
        if (availablePlayers == null || availablePlayers.isEmpty()) {
            Toast.makeText(getContext(), "Error: Player list is empty.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentPlayerVotingIndex < availablePlayers.size()) {
            currentPlayerVoting = availablePlayers.get(currentPlayerVotingIndex);
            tvVoterName.setText("Voting: " + currentPlayerVoting.getName());

            // Populate spinner with other players (player cannot vote for themselves)
            List<Player> voteOptions = new ArrayList<>(availablePlayers);
            voteOptions.remove(currentPlayerVoting); // Remove current voter from options

            // Using Player objects directly in the adapter assumes Player.toString() returns the name
            // Or create a custom adapter if you need more complex display
            // ArrayAdapter<Player> adapter = new ArrayAdapter<>(requireContext(),
            //         android.R.layout.simple_spinner_item, voteOptions);
            // Using player names for simplicity here:
            List<String> playerNamesForSpinner = new ArrayList<>();
            for(Player p : voteOptions) {
                playerNamesForSpinner.add(p.getName());
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_spinner_item, playerNamesForSpinner);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerVoteForPlayer.setAdapter(adapter);
            spinnerVoteForPlayer.setSelection(0); // Default selection
        } else {
            // All players have voted, now do coach's vote
            setupCoachVote();
        }
    }

    private void setupCoachVote() {
        tvVoterName.setText("Coach's Vote");
        // Populate spinner with all players
        // ArrayAdapter<Player> adapter = new ArrayAdapter<>(requireContext(),
        //         android.R.layout.simple_spinner_item, availablePlayers);

        List<String> playerNamesForSpinner = new ArrayList<>();
        for(Player p : availablePlayers) {
            playerNamesForSpinner.add(p.getName());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, playerNamesForSpinner);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerVoteForPlayer.setAdapter(adapter);
        btnSubmitVote.setText("Submit Coach Vote & Finish");
        currentPlayerVoting = null; // Indicate it's the coach's turn
    }

    private void handleSubmitVote() {
        if (spinnerVoteForPlayer.getSelectedItem() == null) {
            Toast.makeText(getContext(), "Please select a player to vote for.", Toast.LENGTH_SHORT).show();
            return;
        }

        String selectedPlayerName = (String) spinnerVoteForPlayer.getSelectedItem();
        Player votedForPlayer = null;
        for (Player p : availablePlayers) {
            if (p.getName().equals(selectedPlayerName)) {
                votedForPlayer = p;
                break;
            }
        }

        if (votedForPlayer == null) {
            Toast.makeText(getContext(), "Error: Selected player not found.", Toast.LENGTH_SHORT).show();
            return;
        }


        // TODO: Get actual gameId and gameDate
        // String gameDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()); // Or from game object

        String voterNameText;
        int voterId;
        boolean isCoachVote;

        if (currentPlayerVoting != null) { // It's a player's vote
            voterNameText = currentPlayerVoting.getName();
            voterId = currentPlayerVoting.getId(); // Ensure your Player object has an ID
            isCoachVote = false;

            // Prevent voting for self (double check,

            if (votedForPlayer == null) {
                Toast.makeText(getContext(), "Error: Selected player not found.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Get gameDate - assuming it's the current date for simplicity,
            // but ideally, it should come from the actual game that just finished.
            String gameDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new com.google.type.Date());

            String voterNameText;
            int voterId; // Using -1 for coach or a predefined coach ID if you have one
            boolean isCoachVoteFlag;

            if (currentPlayerVoting != null) { // It's a player's vote
                // Double-check: ensure player is not voting for themselves (UI should prevent this, but good to check)
                if (currentPlayerVoting.getId() == votedForPlayer.getId()) {
                    Toast.makeText(getContext(), "Players cannot vote for themselves.", Toast.LENGTH_SHORT).show();
                    return;
                }
                voterNameText = currentPlayerVoting.getName();
                voterId = currentPlayerVoting.getId(); // Ensure your Player object has an ID
                isCoachVoteFlag = false;
            } else { // It's the coach's vote
                voterNameText = "Coach"; // Or a specific coach name if you have it
                voterId = -1; // Special ID for coach, or manage coach as a type of user/player
                isCoachVoteFlag = true;
            }

            // Create Vote object
            Vote vote = new Vote(
                    currentGameId,      // The ID of the game this vote belongs to
                    gameDate,
                    voterId,            // ID of the player voting (or -1 for coach)
                    voterNameText,      // Name of the player voting (or "Coach")
                    votedForPlayer.getId(), // ID of the player being voted for
                    votedForPlayer.getName(), // Name of the player being voted for
                    isCoachVoteFlag
            );

            // TODO: Save the vote to the database via ViewModel and DAO
            // Example: gameStatsViewModel.insertVote(vote);
            // This assumes GameStatsViewModel has a method that calls voteDao.insert(vote)
            // You'll need to set up GameStatsViewModel and VoteDao for this.
            // For demonstration purposes, just logging it:
            Log.d("PlayerVotingFragment", "Vote Recorded: " + voterNameText + " voted for " + votedForPlayer.getName() + (isCoachVoteFlag ? " (Coach Vote)" : ""));
            Toast.makeText(getContext(), "Vote for " + votedForPlayer.getName() + " recorded!", Toast.LENGTH_SHORT).show();

            // --- DATABASE INSERTION (This is where you'd actually save) ---
            // if (gameStatsViewModel != null) {
            // gameStatsViewModel.insertVote(vote); // Assuming you have this method in your GameStatsViewModel
            // } else {
            // Log.e("PlayerVotingFragment", "GameStatsViewModel is null. Vote not saved to DB.");
            // }


            // Move to the next voter or finish
            if (currentPlayerVoting != null) { // If it was a player's vote
                currentPlayerVotingIndex++;
                setupNextVoter(); // This will call setupCoachVote() if all players are done
            } else { // If it was the coach's vote, then voting is finished
                Toast.makeText(getContext(), "All votes recorded! Thank you.", Toast.LENGTH_LONG).show();
                // TODO: Navigate away from this fragment (e.g., back to stats, main screen)
                // Example: requireActivity().getSupportFragmentManager().popBackStack();
                // Or navigate to a new "Vote Results" screen if you have one.
                // For now, just disabling the button.
                btnSubmitVote.setEnabled(false);
                btnSubmitVote.setText("Voting Finished");
                // You might want to pass a result back to the previous fragment or activity.
            }
        }

        // --- ViewModel for DB Operations ---
        // You would typically have a ViewModel that handles database interactions.
        // This is a conceptual GameStatsViewModel that would interact with your VoteDao.
        // public class GameStatsViewModel extends AndroidViewModel {
        //     private VoteRepository voteRepository; // Your repository that uses VoteDao
        //
        //     public GameStatsViewModel(@NonNull Application application) {
        //         super(application);
        //         voteRepository = new VoteRepository(application);
        //     }
        //
        //     public void insertVote(Vote vote) {
        //         voteRepository.insertVote(vote);
        //     }
        //     // ... other methods to get vote summaries etc. ...
        // }
    }*/
