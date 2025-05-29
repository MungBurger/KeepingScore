package com.robmapps.keepingscore;

import android.app.Application;
import android.graphics.Color;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import android.content.SharedPreferences;

import androidx.lifecycle.Observer;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.Transformations;
import com.robmapps.keepingscore.database.AppDatabase;
import com.robmapps.keepingscore.database.entities.GameStats;
import com.robmapps.keepingscore.database.entities.Team;
import com.robmapps.keepingscore.database.dao.TeamDAO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;


public class SharedViewModel extends AndroidViewModel { // Extend AndroidViewModel
    private final MutableLiveData<HashMap<String, ArrayList<Player>>> teams = new MutableLiveData<>(new HashMap<>());
    private final MutableLiveData<String> gameTimer = new MutableLiveData<>("00:00");
    private final MutableLiveData<String> bonusTimer = new MutableLiveData<>("00:00");
    private final MutableLiveData<String> gameMode = new MutableLiveData<>("10m,2H");
    private final MutableLiveData<Integer> currentPeriod = new MutableLiveData<>(1);
    private final MutableLiveData<Boolean> gameInProgress = new MutableLiveData<>(false);
    private final MutableLiveData<Integer> currentQuarter = new MutableLiveData<>(1);
    private final MutableLiveData<String> currentCentrePass = new MutableLiveData<>("Team1");
    private final MutableLiveData<Integer> team1ScoreColor = new MutableLiveData<>(Color.rgb(51, 232, 20)); // Default color for Team 1
    private final MutableLiveData<Integer> team2ScoreColor = new MutableLiveData<>(Color.rgb(0, 0, 0)); // Default color for Team 2
    private final MutableLiveData<String> gameStats = new MutableLiveData<>();
    private final AppDatabase database;
    private final TeamDAO teamDao;
    private final LiveData<List<Team>> teamsLive;     // LiveData for the list of all teams (observed by the spinner)

    private static final String PREFS_NAME = "GamePreferences";
    private static final String KEY_TEAM_1_NAME = "team1_name";
    private static final String KEY_TEAM_2_NAME = "team2_name";
    private final MutableLiveData<String> activeTeamName = new MutableLiveData<>(); // LiveData for the name of the active team (driven by spinner selection)
    private final MutableLiveData<Team> _activeTeam = new MutableLiveData<>();
    private final SharedPreferences sharedPreferences;
    private final SavedStateHandle savedStateHandle; // Declare SavedStateHandle
    public LiveData<String> getActiveTeamName() {
        return activeTeamName;
    }
    private MutableLiveData<Integer> _team1Score = new MutableLiveData<>(0);
    private MutableLiveData<Integer> _team2Score = new MutableLiveData<>(0);
    private MutableLiveData<String> _team1Name = new MutableLiveData<>();
    private MutableLiveData<String> _team2Name = new MutableLiveData<>();
    public LiveData<Integer> getTeam1Score() { return _team1Score; }
    public LiveData<Integer> getTeam2Score() { return _team2Score; }
    // Public LiveData to be observed by the Fragment
    public LiveData<String> getTeam1Name() { return _team1Name;
    }
    public LiveData<String> getTeam2Name() {
        return _team2Name;
    }
    // Existing LiveData for active team
    public LiveData<Team> getActiveTeam() {
        return _activeTeam;
    }
    public LiveData<String> getGameMode() {
        return gameMode;
    }
    public LiveData<Integer> getCurrentPeriod() {
        return currentPeriod;
    }
    public LiveData<Boolean> getGameInProgress() {
        return gameInProgress;
    }
    public void setGameInProgress(Boolean isInProgress) {
        gameInProgress.setValue(isInProgress);
    }
    public void setCurrentPeriod(Integer period) {
        currentPeriod.setValue(period);
    }
    public void setGameMode(String mode) {
        gameMode.setValue(mode);
    }
    private final MutableLiveData<List<ScoringAttempt>> allActions = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<ScoringAttempt>> _currentActions = new MutableLiveData<>(new ArrayList<>());
    public final LiveData<List<ScoringAttempt>> currentActions = _currentActions;

    // Constructor that accepts Application
    public SharedViewModel(@NonNull Application application, SavedStateHandle savedStateHandle) {
        super(application); // Pass the application to the super constructor
        this.savedStateHandle = savedStateHandle;
        this.database = AppDatabase.getDatabase(application);
        sharedPreferences = application.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE);

        teamDao = database.teamDao();
        teamsLive = teamDao.getAllTeams();

        String initialTeam1Name = sharedPreferences.getString(KEY_TEAM_1_NAME, "");
        String initialTeam2Name = sharedPreferences.getString(KEY_TEAM_2_NAME, "");
        _team1Name.setValue(initialTeam1Name);
        _team2Name.setValue(initialTeam2Name);

        _activeTeam.observeForever(activeTeam -> {
            Log.d("GameVMActiveTeam", "Active team changed in ViewModel: " + (activeTeam != null ? activeTeam.getTeamName() : "null"));
        });
    };
    // Method to update Team 2 name in SavedStateHandle
    public void setActiveTeamName(String name) {
        // When activeTeamName is set externally, fetch and set _activeTeam
        if (name != null && !name.isEmpty()) {
            // Use the DAO to get the LiveData for the team by name
            LiveData<Team> teamLiveData = teamDao.getTeamByNameLive(name);
            // Observe this LiveData temporarily to get the value and set _activeTeam
            teamLiveData.observeForever(new Observer<Team>() {
                @Override
                public void onChanged(Team team) {
                    _activeTeam.setValue(team); // Update the internal active team
                    // Remove the observer immediately after getting the value
                    teamLiveData.removeObserver(this);
                }
            });
        } else {
            _activeTeam.setValue(null); // No active team
        }
        // Consider if you still need to set activeTeamName itself here or if _activeTeam is sufficient
        activeTeamName.setValue(name);
    }

    public LiveData<List<Team>> getTeamsLive() {
        return database.teamDao().getAllTeams(); // Return all teams
    }

    // New LiveData for Game Stats
    public LiveData<List<GameStats>> getAllGameStats() {
        return database.gameStatsDao().getAllGameStats(); // Fetch all game stats from Room
    }

    public void insertGameStats(GameStats gameStats) {
        new Thread(() -> database.gameStatsDao().insertGameStats(gameStats)).start(); // Save game stats asynchronously
    }

    public void deleteGameStatsById(int id) {
        new Thread(() -> database.gameStatsDao().deleteGameStatsById(id)).start(); // Delete a specific game by ID
    }

    public List<Team> getTeamsDirect() {
        List<Team> teams = MyApplication.getDatabase().teamDao().getTeamsDirect();
        Log.d("DatabaseDebug", "Teams fetched directly: " + (teams != null ? teams.size() : "null"));
        return teams;
    }

    // Method to set the active team (existing logic)
    public void setActiveTeam(Team team) {
        Log.d("SharedViewModel", "Setting active team: " + (team != null ? team.getTeamName() : "null"));
        _activeTeam.setValue(team);
        // Optional: Save the active team ID in SavedStateHandle if you need to restore it later
        savedStateHandle.set("activeTeamId", team != null ? team.getId() : -1L);
    }

    public void insertTeam(Team team) {
        Executors.newSingleThreadExecutor().execute(() -> {
            Log.d("DatabaseDebug1", "Inserting team: " + team.getTeamName() + ", Players count: " + (team.getPlayers() != null ? team.getPlayers().size() : 0));
            long insertedId = teamDao.insertTeam(team); // Insert and get the generated ID

            // After insertion, fetch the team by its generated ID.
            // Use the LiveData method from the DAO.
            LiveData<Team> insertedTeamLiveData = teamDao.getTeamById((int) insertedId);

            // Now, switch to the main thread to observe the LiveData and update _activeTeam
            // Use ContextCompat.getMainExecutor() for a modern approach
            ContextCompat.getMainExecutor(getApplication()).execute(() -> {
                insertedTeamLiveData.observeForever(new Observer<Team>() {
                    @Override
                    public void onChanged(Team insertedTeam) {
                        // This onChanged will be called on the main thread when the data is available
                        if (insertedTeam != null) {
                            // Set the newly fetched Team object to the main thread's _activeTeam
                            // Use setValue() now because we are on the main thread
                            _activeTeam.setValue(insertedTeam);
                            Log.d("DatabaseDebug1", "Successfully inserted team and set active team to: " + insertedTeam.getTeamName() + " (ID: " + insertedTeam.getId() + ")");
                        } else {
                            Log.e("DatabaseDebug1", "Fetched inserted team is null for ID: " + insertedId);
                            // Handle the case where fetching the inserted team fails
                            _activeTeam.setValue(null); // Clear active team (use setValue on main thread)
                        }
                        // Remove this temporary observer immediately after getting the data
                        insertedTeamLiveData.removeObserver(this);
                    }
                });
            });
        });
    }

    public void updateTeam(Team team) {
        Log.d("DatabaseDebug2", "Updating team: " + team.getTeamName() + ", Players count: " + (team.getPlayers() != null ? team.getPlayers().size() : 0));
        Executors.newSingleThreadExecutor().execute(() -> {
            teamDao.updateTeam(team);
            // If the updated team is the active one, the UI observer will react
            // to the LiveData update from Room.
            Log.d("DatabaseDebug2", "Update team operation complete for: " + team.getTeamName());
        });
    }
    public LiveData<String> getCurrentCentrePass() {
        return currentCentrePass;
    }

    // Method to update centre pass (existing logic)
    public void setCurrentCentrePass(String centrePass) {
        Log.d("SharedViewModel", "Setting centre pass: " + centrePass);
        currentCentrePass.setValue(centrePass);
    }

    public LiveData<Integer> getTeam1ScoreColor() {
        return team1ScoreColor;
    }

    public void setTeam1ScoreColor(int color) {
        team1ScoreColor.setValue(color);
    }

    public LiveData<Integer> getTeam2ScoreColor() {
        return team2ScoreColor;
    }

    public void setTeam2ScoreColor(int color) {
        team2ScoreColor.setValue(color);
    }

    public LiveData<Integer> getCurrentQuarter() {
        return currentQuarter;
    }

    public void setCurrentQuarter(int quarter) {currentQuarter.setValue(quarter);}

    public void updateTeam1Score(int scoreChange) {
        if (_team1Score.getValue() != null) {
            _team1Score.setValue(_team1Score.getValue() + scoreChange);
        }
    }
    public void updateTeam2Score(int scoreChange) {
        if (_team2Score.getValue() != null) {
            _team2Score.setValue(_team2Score.getValue() + scoreChange);
        }
    }

    public LiveData<HashMap<String, ArrayList<Player>>> getTeams() {
        return teams;
    }

    public void addTeam(String teamName, ArrayList<Player> players) {
        if (teams.getValue() != null) {
            HashMap<String, ArrayList<Player>> currentTeams = new HashMap<>(teams.getValue());
            currentTeams.put(teamName, players); // Add or update the team with the provided players
            teams.setValue(currentTeams); // Update the LiveData with the modified team list
        } else {
            HashMap<String, ArrayList<Player>> newTeams = new HashMap<>();
            newTeams.put(teamName, players); // Create a new team with the provided players
            teams.setValue(newTeams);
        }
    }

    public ArrayList<Player> getPlayersForActiveTeam() {
        String activeName = activeTeamName.getValue();
        if (activeName != null && teams.getValue() != null) {
            Log.d("DatabaseDebug", "Retrieving players for team: " + (_activeTeam != null ? activeName : "null"));

            HashMap<String, ArrayList<Player>> currentTeams = teams.getValue();
            if (currentTeams.containsKey(activeName)) {
                return new ArrayList<>(currentTeams.get(activeName)); // Return a copy to avoid reference issues
            }
        }
        return new ArrayList<>(); // Return an empty list if no active team or players exist
    }

    public void updateAllActions(List<ScoringAttempt> updatedActions) {
        allActions.setValue(updatedActions); // Update the LiveData with the new list
    }
    public void setTeams(HashMap<String, ArrayList<Player>> updatedTeams) {
        teams.setValue(updatedTeams);
    }// Inside SharedViewModel.java

    public void deleteTeam(String teamNameToDelete) {
        Log.d("SharedViewModel", "Attempting to delete team by name: " + teamNameToDelete);
        if (teamNameToDelete == null || teamNameToDelete.isEmpty()) {
            Log.e("SharedViewModel", "Cannot delete team, name is null or empty.");
            return;
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            // Get the current active team BEFORE deleting
            Team currentActiveTeamBeforeDeletion = _activeTeam.getValue();

            // Use the specific DAO method to delete by name
            teamDao.deleteTeamByName(teamNameToDelete); // This is from your TeamDAO.java
            Log.d("SharedViewModel", "Deletion executed for team: " + teamNameToDelete + " from database.");

            // Check if the deleted team was the active team
            if (currentActiveTeamBeforeDeletion != null && teamNameToDelete.equals(currentActiveTeamBeforeDeletion.getTeamName())) {
                Log.d("SharedViewModel", "Deleted team '" + teamNameToDelete + "' was active. Clearing activeTeam.");
                _activeTeam.postValue(null); // Update activeTeam on the main thread via postValue
            }
        });
    }

    public void deleteAllTeams() {
        Executors.newSingleThreadExecutor().execute(() -> { // Use Executors for consistency
            Log.d("SharedViewModel", "Deleting all teams from database.");
            teamDao.deleteAllTeams(); // Deletes all from DB
            // Room will auto-update the LiveData from getTeamsLive()

            Log.d("SharedViewModel", "Clearing active team after deleting all teams.");
            _activeTeam.postValue(null); // Clear active team
        });
    }
    private void loadScoresFromPreferences() {
        int score1 = sharedPreferences.getInt("iScore1", 0);
        int score2 = sharedPreferences.getInt("iScore2", 0);
        _team1Score.setValue(score1);
        _team2Score.setValue(score2);
    }

    private void saveScoresToPreferences() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("iScore1", _team1Score.getValue());
        editor.putInt("iScore2", _team2Score.getValue());
        editor.apply();
    }

    public LiveData<List<ScoringAttempt>> getAllActions() {
        return allActions;
    }
    public void validatePlayerPositions(ArrayList<Player> players) {
        Map<String, Player> positionMap = new HashMap<>();
        for (Player player : players) {
            String position = player.getPosition();
            if (positionMap.containsKey(position) && !position.equals("Off")) {
                // Move the previous occupant to "Off"
                positionMap.get(position).setPosition("Off");
            }
            positionMap.put(position, player);
        }
    }
    public void recordAttempt(String playerName,String playerPosition, boolean isSuccessful, String timestamp) {
        List<ScoringAttempt> currentActions = allActions.getValue();
        if (currentActions == null) {
            currentActions = new ArrayList<>();
        }
        currentActions.add(new ScoringAttempt(playerName, playerPosition, isSuccessful, timestamp));
        allActions.setValue(currentActions); // Update LiveData
    }
    public String getCurrentActionsLogString() {
        StringBuilder logBuilder = new StringBuilder();
        List<ScoringAttempt> attempts = allActions.getValue();
        Log.d("ViewModel", "Inside getCurrentActionsLogString - attempts list size: " + attempts.size()); // Add this log
        if (attempts != null) {
            for (int i = 0; i < attempts.size(); i++) {
                logBuilder.append(attempts.get(i).toString());
                if (i < attempts.size() - 1) {
                    logBuilder.append("\n");
                }

            }
        }
        return logBuilder.toString();
    }

    // Method to clear all actions

    public LiveData<String> getGameTimer() {
        return gameTimer;
    }
    public void updateGameTimer(String time) {
        gameTimer.setValue(time); // Update the timer display
    }
    public void resetGame() {
        _team1Score.setValue(0); // Reset Team 1 score
        _team2Score.setValue(0); // Reset Team 2 score
        gameTimer.setValue("00:00"); // Reset the timer
        currentQuarter.setValue(1); // Reset to the first quarter
        allActions.setValue(new ArrayList<>());
    }
    public void swapCentrePass() {
        if (currentCentrePass.getValue() != null && currentCentrePass.getValue().equals("Team2")) {
            setCurrentCentrePass("Team1");
            setTeam1ScoreColor(Color.rgb(51, 232, 20));
            setTeam2ScoreColor(Color.rgb(0, 0, 0));
        } else {
            setCurrentCentrePass("Team2");
            setTeam1ScoreColor(Color.rgb(0, 0, 0));
            setTeam2ScoreColor(Color.rgb(51, 232, 20));
        }

    }
    public void saveTeam1Name(String name) {
        if (!name.equals(_team1Name.getValue())) { // Only save and update if the name has changed
            sharedPreferences.edit().putString(KEY_TEAM_1_NAME, name).apply();
            _team1Name.setValue(name); // Update the LiveData
        }
    }

    // Method to save Team 2 name to SharedPreferences and update LiveData
    public void saveTeam2Name(String name) {
        if (!name.equals(_team2Name.getValue())) { // Only save and update if the name has changed
            sharedPreferences.edit().putString(KEY_TEAM_2_NAME, name).apply();
            _team2Name.setValue(name); // Update the LiveData
        }
    }
}
// TODO     Add Game Mode and Period Number and total Periods to both SharedModelView and SharedPref, also to be reset by reset button.