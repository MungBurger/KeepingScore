package com.robmapps.keepingscore;

import static androidx.core.content.ContextCompat.getSystemService;

import android.annotation.SuppressLint;
import androidx.core.content.ContextCompat;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.content.IntentFilter;

import com.robmapps.keepingscore.database.entities.Team;

import android.view.animation.AccelerateDecelerateInterpolator;
import android.animation.ObjectAnimator;
import android.animation.Animator;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.robmapps.keepingscore.database.AppDatabase;
import com.robmapps.keepingscore.database.entities.GameStats;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.content.ContentValues;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.OutputStream;
import java.util.Map;

/**
 * Fragment for the gameplay screen of the netball scoring app.
 * 
 * This fragment handles:
 * - Displaying and updating team scores
 * - Recording successful and unsuccessful goal attempts
 * - Managing game timer and periods
 * - Tracking player statistics
 * - Saving game state when the app is paused
 */
public class Frag_Gameplay extends Fragment {
    public TextView tvScore1, tvScore2, tvTimeRem, tvGameTitle, tvQuarterNum, tvTeam1;
    public EditText etTeam2;
    public int iScore1, iScore2, iGS1, iGA1, iGS2, iGA2, iGS1M, iGA1M, iGS2M, iGA2M, iNumPers, iPerDuration, iPerNum, iLength;
    public long TimeMultiplier;
    public String sScore1, timeFormatted, sTeam1, sTeam2, StatsFileName, sGSPlayer, sGAPlayer, sCurrMode;
    public Boolean bTimerRunning = false, bDebugMode = false;
    public Button btnShowStats, btnStartGame, btnBestOnCourt, btnUndo, btnGameMode, btnReset, btnTeamList;
    public Button btnGS1, btnGA1, btnGS1M, btnGA1M, btnGS2, btnGA2, btnGS2M, btnGA2M;
    public SharedPreferences spSavedValues;
    public CountDownTimer cdEndofPeriodTimer;
    public StringBuilder sbExportStats; // sAllActions,
    private SharedViewModel viewModel;
    private ImageView ivCentrePassCircle;
    private ObjectAnimator animatorY; // To control the animation
    private boolean movingToEndLocation = true; // To track animation direction
    private float startX, startY, endX, endY;   // Coordinates for movement
    // Below is vibration patterns
    long[] patternGoal = {0, 800, 400};
    long[] patternMiss = {0, 100, 100, 100, 100};
    long[] patternEndGame = {1, 100, 1000, 300, 200, 100, 500, 200, 100};
    long[] patternEndPeriod = {1, 100, 1000, 300, 200, 100, 500, 200, 100};
    private float target1CenterX, target1CenterY;
    private float target2CenterX, target2CenterY;
    private boolean coordinatesInitialized = false;

    /**
     * Creates and initializes the fragment's view
     */
    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gameplay, container, false);
        LinearLayout linearLayout = view.findViewById(R.id.gameplay_root_layout);
        setupUI(linearLayout);

        viewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        
        // Initialize UI components
        initializeViews(view);
        
        // Setup game mode
        setupGameMode();
        
        // Setup button states based on game progress
        updateButtonStates();
        
        // Setup observers
        setupObservers();
        
        // Setup button click listeners
        setupButtonListeners(view);
        
        // Register broadcast receiver
        registerTimerReceiver();
        
        // Setup text change listener for Team 2 name
        setupTeam2NameListener();
        
        updateGameTitle();
        return view;
    }
    
    /**
     * Initializes all view components
     */
    private void initializeViews(View view) {
        tvScore1 = view.findViewById(R.id.Team1Score);
        tvScore2 = view.findViewById(R.id.Team2Score);
        tvGameTitle = view.findViewById(R.id.GameTitle);
        tvTeam1 = view.findViewById(R.id.T1Name);
        tvTeam1.setEnabled(true);
        etTeam2 = view.findViewById(R.id.T2Name);
        btnUndo = view.findViewById(R.id.UndoButton);
        btnReset = view.findViewById(R.id.ResetGame);
        btnGS1 = view.findViewById(R.id.GS1);
        btnGA1 = view.findViewById(R.id.GA1);
        btnGS1M = view.findViewById(R.id.GS1Miss);
        btnGA1M = view.findViewById(R.id.GA1Miss);
        btnGS2 = view.findViewById(R.id.GS2);
        btnGA2 = view.findViewById(R.id.GA2);
        btnGS2M = view.findViewById(R.id.GS2Miss);
        btnGA2M = view.findViewById(R.id.GA2Miss);
        tvTimeRem = view.findViewById(R.id.TimeRem);
        tvQuarterNum = view.findViewById(R.id.QuarterNum);
        btnGameMode = view.findViewById(R.id.GameMode);
        ivCentrePassCircle = view.findViewById(R.id.centrePassCircle);
        btnStartGame = view.findViewById(R.id.btnStartGame);
        
        if (sGSPlayer != null) {
            btnGS1.setText("GS" + "\n" + sGSPlayer);
            btnGA1.setText("GA" + "\n" + sGAPlayer);
        }
    }
    
    /**
     * Sets up the game mode from the view model
     */
    private void setupGameMode() {
        sCurrMode = viewModel.getGameMode().getValue().toString();
        Log.d("GameVariables", "Game Mode = " + sCurrMode);
        
        if (sCurrMode.length() > 8) {
            sCurrMode = "15m,4Q"; // Default game mode
            btnGameMode.setText(sCurrMode);
        } else {
            btnGameMode.setText(sCurrMode);
        }
        
        viewModel.setGameMode(sCurrMode);
    }
    
    /**
     * Updates button enabled states based on game progress
     */
    private void updateButtonStates() {
        boolean gameInProgress = viewModel.getGameInProgress().getValue() == true;
        
        btnGS1.setEnabled(gameInProgress);
        btnGA1.setEnabled(gameInProgress);
        btnGS1M.setEnabled(gameInProgress);
        btnGA1M.setEnabled(gameInProgress);
        btnGS2.setEnabled(gameInProgress);
        btnGA2.setEnabled(gameInProgress);
        btnGS2M.setEnabled(gameInProgress);
        btnGA2M.setEnabled(gameInProgress);
        btnGameMode.setEnabled(!gameInProgress);
        btnStartGame.setEnabled(!gameInProgress);
        bTimerRunning = gameInProgress;
    }
    
    /**
     * Sets up all observers for LiveData from the ViewModel
     */
    private void setupObservers() {
        // Observe Active Team
        viewModel.getActiveTeam().observe(getViewLifecycleOwner(), this::handleActiveTeamChange);
        
        // Observe Team 2 name
        viewModel.getTeam2Name().observe(getViewLifecycleOwner(), team2Name -> {
            Log.d("GameplayTeam2Observer", "Team 2 name observed: " + team2Name);
            if (!etTeam2.getText().toString().equals(team2Name)) {
                etTeam2.setText(team2Name);
            }
        });
        
        // Observe scores
        viewModel.getTeam1Score().observe(getViewLifecycleOwner(), score -> {
            tvScore1.setText(String.valueOf(score));
        });
        viewModel.getTeam2Score().observe(getViewLifecycleOwner(), score -> {
            tvScore2.setText(String.valueOf(score));
        });
        
        // Observe Centre-Pass State and Colors
        viewModel.getCurrentCentrePass().observe(getViewLifecycleOwner(), centrePass -> {
            Log.d("CentrePass", "Current Centre-Pass: " + centrePass);
        });
        viewModel.getTeam1ScoreColor().observe(getViewLifecycleOwner(), color -> {
            tvScore1.setTextColor(color);
        });
        viewModel.getTeam2ScoreColor().observe(getViewLifecycleOwner(), color -> {
            tvScore2.setTextColor(color);
        });
    }
    
    /**
     * Handles changes to the active team
     */
    private void handleActiveTeamChange(Team activeTeam) {
        Log.d("GameplayActiveTeamObs", "Observer triggered.");

        if (activeTeam != null) {
            Log.d("GameplayActiveTeamObs", "Active team is NOT null.");
            if (activeTeam.getTeamName() != null) {
                tvTeam1.setText(activeTeam.getTeamName());
                tvTeam1.requestLayout();
                tvTeam1.invalidate();
                
                // Find GS and GA players
                findScoringPlayers(activeTeam.getPlayers());
                updateGameTitle();
            } else {
                Log.d("GameplayActiveTeamObs", "Active team name IS null.");
                tvTeam1.setText("");
                sGSPlayer = null;
                sGAPlayer = null;
                updateGameTitle();
            }
        } else {
            Log.d("GameplayActiveTeamObs", "Active team IS null.");
            sTeam1 = "";
            tvTeam1.setText("");
            sGSPlayer = null;
            sGAPlayer = null;
            updateGameTitle();
        }
    }
    
    /**
     * Finds players with GS and GA positions in the team
     */
    private void findScoringPlayers(List<Player> players) {
        sGSPlayer = null;
        sGAPlayer = null;

        if (players != null) {
            for (Player player : players) {
                if (player.getPosition() != null) {
                    if (player.getPosition().equals("GS")) {
                        sGSPlayer = player.getName();
                    } else if (player.getPosition().equals("GA")) {
                        sGAPlayer = player.getName();
                    }
                }
            }
        }
        
        // Set default values if no players found
        if (sGSPlayer == null) sGSPlayer = "GS";
        if (sGAPlayer == null) sGAPlayer = "GA";
    }
    
    /**
     * Sets up all button click listeners
     */
    private void setupButtonListeners(View view) {
        // Team 1 scoring buttons
        btnGS1.setOnClickListener(v -> incrementScore(viewModel, tvScore1, "GS1", sGSPlayer, true));
        btnGA1.setOnClickListener(v -> incrementScore(viewModel, tvScore1, "GA1", sGAPlayer, true));
        btnGS1M.setOnClickListener(v -> incrementScore(viewModel, tvScore1, "GS1", sGSPlayer, false));
        btnGA1M.setOnClickListener(v -> incrementScore(viewModel, tvScore1, "GA1", sGAPlayer, false));
        
        // Team 2 scoring buttons
        btnGS2.setOnClickListener(v -> incrementScore(viewModel, tvScore2, "GS2", "Other", true));
        btnGA2.setOnClickListener(v -> incrementScore(viewModel, tvScore2, "GA2", "Other", true));
        btnGS2M.setOnClickListener(v -> incrementScore(viewModel, tvScore2, "GS2", "Other", false));
        btnGA2M.setOnClickListener(v -> incrementScore(viewModel, tvScore2, "GA2", "Other", false));
        
        // Score display click listeners for centre pass
        view.findViewById(R.id.Team1Score).setOnClickListener(v -> {
            viewModel.swapCentrePass();
            startImageAnimation();
        });
        view.findViewById(R.id.Team2Score).setOnClickListener(v -> {
            viewModel.swapCentrePass();
            startImageAnimation();
        });
        
        // Other button listeners
        btnUndo.setOnClickListener(v -> undoLastAction(viewModel));
        sCurrMode = btnGameMode.getText().toString();
        btnGameMode.setOnClickListener(v -> GameMode(view));
        btnStartGame.setOnClickListener(v -> startGameTimer());
        btnGameMode.setOnLongClickListener(v -> GameModeDebug());
        btnReset.setOnClickListener(v -> resetGame());
    }
    
    /**
     * Registers the broadcast receiver for timer updates
     */
    private void registerTimerReceiver() {
        Context context = getContext();
        if (context != null) {
            IntentFilter filter = new IntentFilter();
            filter.addAction("GAME_TIMER_UPDATE");
            filter.addAction("END_OF_PERIOD_ACTION");
            // Use ContextCompat.registerReceiver with RECEIVER_NOT_EXPORTED flag
            ContextCompat.registerReceiver(context, timerReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);
        } else {
            Log.e("Frag_Gameplay", "Context is null, BroadcastReceiver not registered");
        }
    }
    
    /**
     * Sets up the text change listener for Team 2 name
     */
    private void setupTeam2NameListener() {
        etTeam2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No action needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // No action needed
            }

            @Override
            public void afterTextChanged(Editable s) {
                updateGameTitle();
                viewModel.saveTeam2Name(s.toString());
            }
        });
    }

    /**
     * Shows a confirmation dialog for resetting the game
     */
    private Void resetGame() {
        AlertDialog.Builder adBuilder = new AlertDialog.Builder(requireContext());
        adBuilder.setMessage("Confirm Clear current game?")
                .setPositiveButton("Yes", (dialogInterface, i) -> performGameReset())
                .setNegativeButton("Cancel", (dialogInterface, i) -> {
                    // Do nothing on cancel
                });
                
        AlertDialog mDialog = adBuilder.create();
        mDialog.show();
        return null;
    }
    
    /**
     * Performs the actual game reset operations
     */
    private void performGameReset() {
        resetScores();
        resetGameState();
        stopTimers();
        resetUI();
        onPause(); // Save the reset state
    }
    
    /**
     * Resets all score variables to zero
     */
    private void resetScores() {
        iScore1 = 0;
        iScore2 = 0;
        viewModel.resetGame();
        iGS1 = 0;
        iGA1 = 0;
        iGS1M = 0;
        iGA1M = 0;
        iGS2 = 0;
        iGA2 = 0;
        iGS2M = 0;
        iGA2M = 0;
        iPerNum = 1;
    }
    
    /**
     * Resets the game state and disables scoring buttons
     */
    private void resetGameState() {
        disableGameButtons();
        
        if (viewModel.getGameInProgress().getValue() == true) {
            viewModel.setGameInProgress(false);
            Intent stopIntent = new Intent(requireContext(), TimerService.class);
            requireContext().stopService(stopIntent);
            bTimerRunning = false;
            Toast.makeText(requireContext(), "Game Timer stopped.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(requireContext(), "Game Timer isn't running", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Stops any active timers
     */
    private void stopTimers() {
        if (cdEndofPeriodTimer != null) {
            cdEndofPeriodTimer.cancel();
        }
    }
    
    /**
     * Resets the UI elements to their initial state
     */
    private void resetUI() {
        btnStartGame.setEnabled(true);
        tvTimeRem.setTextColor(Color.rgb(0, 0, 0));
        tvScore1.setText(String.valueOf(iScore1));
        tvScore2.setText(String.valueOf(iScore2));
        tvTimeRem.setText("Timer");
        tvQuarterNum.setText("Quarter");
        btnGameMode.setEnabled(true);
    }

    /**
     * Records a scoring attempt and updates the score if successful
     * 
     * @param viewModel The shared view model
     * @param scoreView The TextView displaying the score
     * @param playerPosition The position of the player (GS1, GA1, etc.)
     * @param playerName The name of the player
     * @param isSuccessful Whether the scoring attempt was successful
     */
    private void incrementScore(SharedViewModel viewModel, TextView scoreView, String playerPosition, String playerName, boolean isSuccessful) {
        updatePlayerLabels();
        
        if (isSuccessful) {
            updateScore(scoreView);
            startImageAnimation();
            viewModel.swapCentrePass();
        }

        playerName = resolvePlayerName(playerPosition, playerName);
        viewModel.recordAttempt(playerName, playerPosition, isSuccessful, timeFormatted);
    }
    
    /**
     * Updates the player labels on buttons
     */
    private void updatePlayerLabels() {
        if (sGSPlayer != null) {
            btnGS1.setText("GS" + "\n" + sGSPlayer);
            btnGA1.setText("GA" + "\n" + sGAPlayer);
        }
    }
    
    /**
     * Updates the score for the appropriate team
     */
    private void updateScore(TextView scoreView) {
        if (scoreView == tvScore1) {
            viewModel.updateTeam1Score(1); // Increment score for Team 1
        } else if (scoreView == tvScore2) {
            viewModel.updateTeam2Score(1); // Increment score for Team 2
        }
    }
    
    /**
     * Resolves the player name based on position
     */
    private String resolvePlayerName(String playerPosition, String playerName) {
        if (playerPosition.equals("GS1")) {
            return sGSPlayer;
        } else if (playerPosition.equals("GA1")) {
            return sGAPlayer;
        } else {
            return "Other Team";
        }
    }

    /**
     * Undoes the last scoring action
     */
    private void undoLastAction(SharedViewModel viewModel) {
        List<ScoringAttempt> currentActions = viewModel.getAllActions().getValue();
        if (currentActions != null && !currentActions.isEmpty()) {
            ScoringAttempt lastAction = currentActions.remove(currentActions.size() - 1);
            viewModel.updateAllActions(currentActions);
            
            if (lastAction.isSuccessful()) {
                adjustScoreForUndo(lastAction);
            }
        }
    }
    
    /**
     * Adjusts the score when undoing a successful goal
     */
    private void adjustScoreForUndo(ScoringAttempt lastAction) {
        String position = lastAction.getPlayerPosition();
        
        if (position.startsWith("GS1") || position.startsWith("GA1")) {
            viewModel.updateTeam1Score(-1); // Decrement score for Team 1
            viewModel.swapCentrePass();
            startImageAnimation();
        } else if (position.startsWith("GS2") || position.startsWith("GA2")) {
            viewModel.updateTeam2Score(-1); // Decrement score for Team 2
            viewModel.swapCentrePass();
            startImageAnimation();
        }
    }

    /**
     * Cycles through available game modes
     * 
     * @param view The view that triggered this method
     */
    public void GameMode(View view) {
        bDebugMode = false;
        if (sCurrMode == null) {
            sCurrMode = "15m,4Q"; // Fallback to a default value if null
        }
        
        cycleToNextGameMode();
        updateGameModeSettings();
        
        Log.d("GameVariables", "Game Mode (local) = " + sCurrMode);
        Log.d("GameVariables", "Game Mode (SVM) = " + viewModel.getGameMode().getValue());
    }
    
    /**
     * Cycles to the next game mode based on the current mode
     */
    private void cycleToNextGameMode() {
        switch (sCurrMode) {
            case "GameMode":
            case "01m,2H":
                setGameMode("10m,4Q", 10, 4);
                break;
            case "10m,4Q":
                setGameMode("12m,4Q", 12, 4);
                break;
            case "12m,4Q":
                setGameMode("15m,4Q", 15, 4);
                break;
            case "15m,4Q":
                setGameMode("10m,2H", 10, 2);
                break;
            case "10m,2H":
                setGameMode("12m,2H", 10, 2);
                break;
            case "12m,2H":
                setGameMode("15m,2H", 10, 4);
                break;
            case "15m,2H":
                setGameMode("10m,4Q", 10, 4);
                break;
        }
    }
    
    /**
     * Sets the game mode with the specified parameters
     * 
     * @param mode The game mode string
     * @param duration The period duration in minutes
     * @param periods The number of periods
     */
    private void setGameMode(String mode, int duration, int periods) {
        sCurrMode = mode;
        iPerDuration = duration;
        iNumPers = periods;
        btnGameMode.setText(sCurrMode);
    }
    
    /**
     * Updates the game mode in the ViewModel
     */
    private void updateGameModeSettings() {
        viewModel.setGameMode(sCurrMode);
    }

    /**
     * Sets the game to debug mode with short periods
     * 
     * @return true to indicate the long click was handled
     */
    public boolean GameModeDebug() {
        setGameMode("01m,2H", 1, 2);
        bDebugMode = true;
        viewModel.setGameMode(sCurrMode);
        Log.d("GameVariables", "Game Mode = " + sCurrMode);
        return true;
    }

    /**
     * Starts the game timer and initializes game state
     */
    private void startGameTimer() {
        if (viewModel.getGameInProgress().getValue() == true) {
            Toast.makeText(requireContext(), "Game Timer is already running!", Toast.LENGTH_SHORT).show();
            btnStartGame.setEnabled(false);
            return;
        }

        enableGameButtons();
        updatePlayerButtonLabels();
        initializeGameState();
        startTimerService();
        updateUIForGameStart();
        initializeCentrePassAnimation();
    }
    
    /**
     * Enables all game scoring buttons
     */
    private void enableGameButtons() {
        btnGS1.setEnabled(true);
        btnGA1.setEnabled(true);
        btnGS1M.setEnabled(true);
        btnGA1M.setEnabled(true);
        btnGS2.setEnabled(true);
        btnGA2.setEnabled(true);
        btnGS2M.setEnabled(true);
        btnGA2M.setEnabled(true);
    }
    
    /**
     * Updates player button labels with player names
     */
    private void updatePlayerButtonLabels() {
        btnGS1.setText("GS" + "\n" + sGSPlayer);
        btnGA1.setText("GA" + "\n" + sGAPlayer);
        sCurrMode = btnGameMode.getText().toString();
    }
    
    /**
     * Initializes the game state in the ViewModel
     */
    private void initializeGameState() {
        if (iPerNum < 1) {
            iPerNum++;
        }
        viewModel.setGameInProgress(true);
        viewModel.setCurrentPeriod(iPerNum);
        parseGameMode(); // Ensure period duration and number of periods are set
    }
    
    /**
     * Starts the timer service with appropriate parameters
     */
    private void startTimerService() {
        Intent intent = new Intent(requireContext(), TimerService.class);
        intent.putExtra("PERIOD_DURATION", iPerDuration);
        intent.putExtra("TOTAL_PERIODS", iNumPers);
        intent.putExtra("CURRENT_PERIOD", iPerNum);
        requireContext().startService(intent);
    }
    
    /**
     * Updates UI elements for game start
     */
    private void updateUIForGameStart() {
        btnStartGame.setEnabled(false);
        bTimerRunning = true;
        Toast.makeText(requireContext(), "Game Timer Started!", Toast.LENGTH_SHORT).show();
        btnGameMode.setEnabled(false);
    }
    
    /**
     * Initializes the centre pass animation coordinates
     */
    private void initializeCentrePassAnimation() {
        int[] tvScore1ScreenCoordinates = new int[2];
        int[] tvScore2ScreenCoordinates = new int[2];
        tvScore1.getLocationOnScreen(tvScore1ScreenCoordinates);
        tvScore2.getLocationOnScreen(tvScore2ScreenCoordinates);

        // Check if we're in landscape mode
        boolean isLandscape = ScreenSizeHelper.isLandscape(requireContext());
        
        if (isLandscape) {
            // In landscape mode, adjust animation path for horizontal movement
            startX = tvScore1ScreenCoordinates[0] + (tvScore1.getWidth() / 2f) - (ivCentrePassCircle.getWidth() / 2f);
            startY = tvScore1ScreenCoordinates[1] + (tvScore1.getHeight() / 2f) - (ivCentrePassCircle.getHeight() / 2f);
            endX = tvScore2ScreenCoordinates[0] + (tvScore2.getWidth() / 2f) - (ivCentrePassCircle.getWidth() / 2f);
            endY = tvScore2ScreenCoordinates[1] + (tvScore2.getHeight() / 2f) - (ivCentrePassCircle.getHeight() / 2f);
        } else {
            // In portrait mode, use vertical movement
            startX = tvScore1ScreenCoordinates[0] + (tvScore1.getWidth() / 2f) - (ivCentrePassCircle.getWidth() / 2f);
            startY = tvScore1ScreenCoordinates[1] - (tvScore1.getHeight() / 2f) - (ivCentrePassCircle.getHeight() / 3f);
            endX = tvScore2ScreenCoordinates[0] + (tvScore2.getWidth() / 2f) - (ivCentrePassCircle.getWidth() / 2f);
            endY = tvScore2ScreenCoordinates[1] - (tvScore2.getHeight() / 2f) - (ivCentrePassCircle.getHeight() / 3f);
        }
        
        ivCentrePassCircle.setX(startX);
        ivCentrePassCircle.setY(startY);
        ivCentrePassCircle.setVisibility(View.VISIBLE);
    }

    /**
     * Starts the end-of-period timer
     */
    private void EndOfPeriodTimer() {
        tvTimeRem.setTextColor(Color.rgb(255, 150, 0));
        TimeMultiplier = bDebugMode ? 10000 : 60000;

        cdEndofPeriodTimer = new CountDownTimer(TimeMultiplier / 3, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                updateTimerDisplay(millisUntilFinished);
            }

            @Override
            public void onFinish() {
                handlePeriodEnd();
            }
        }.start();
    }
    
    /**
     * Updates the timer display during countdown
     */
    private void updateTimerDisplay(long millisUntilFinished) {
        long minutes = ((millisUntilFinished / 1000) % 3600) / 60;
        long seconds = (millisUntilFinished / 1000 % 60);
        timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);

        tvTimeRem.setText(timeFormatted);
        viewModel.setGameInProgress(true);
        bTimerRunning = true;
    }
    
    /**
     * Handles the end of a period
     */
    private void handlePeriodEnd() {
        bTimerRunning = false;
        viewModel.setGameInProgress(false);
        disableGameButtons();

        if (iPerNum < iNumPers) {
            handleMidGamePeriodEnd();
        } else {
            handleGameOver();
        }
    }
    
    /**
     * Disables all game scoring buttons
     */
    private void disableGameButtons() {
        btnGS1.setEnabled(false);
        btnGA1.setEnabled(false);
        btnGS1M.setEnabled(false);
        btnGA1M.setEnabled(false);
        btnGS2.setEnabled(false);
        btnGA2.setEnabled(false);
        btnGS2M.setEnabled(false);
        btnGA2M.setEnabled(false);
    }
    
    /**
     * Handles the end of a period when the game is not over
     */
    private void handleMidGamePeriodEnd() {
        String periodLabel = iNumPers == 2 ? "End of H: " : "End of Q: ";
        tvQuarterNum.setText(periodLabel + iPerNum);
        
        viewModel.recordAttempt("\n-------========-------", "", false, timeFormatted);
        btnStartGame.setEnabled(true);
        iPerNum++;
        MakeitShake(patternEndPeriod);
    }
    
    /**
     * Handles the end of the game
     */
    private void handleGameOver() {
        tvQuarterNum.setText("Game Over");
        btnStartGame.setEnabled(false);
        MakeitShake(patternEndGame);
        
        // TODO: Auto change to Stats page after game finishes
        /*
        Frag_Stats fragStats = new Frag_Stats();
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainerView1, fragStats)
                .addToBackStack(null) // Optional, allows back navigation to gameplay
                .commit();
        */
    }

    /**
     * Parses the current game mode string to extract duration and period count
     */
    private void parseGameMode() {
        if (sCurrMode != null && !sCurrMode.isEmpty()) {
            try {
                // Extract duration (first two digits)
                int duration = Integer.parseInt(sCurrMode.substring(0, 2));
                iPerDuration = duration; // Set period duration in minutes

                // Determine number of periods based on mode string
                if (sCurrMode.contains("2H")) {
                    iNumPers = 2; // 2 Halves
                } else if (sCurrMode.contains("4Q")) {
                    iNumPers = 4; // 4 Quarters
                }
            } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
                Log.e("Frag_Gameplay", "Error parsing game mode: " + sCurrMode, e);
                // Set default values if parsing fails
                iPerDuration = 15;
                iNumPers = 4;
            }
        }
    }

    /**
     * BroadcastReceiver for handling timer updates and period end events
     * Receives broadcasts from the TimerService
     */
    private BroadcastReceiver timerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("GAME_TIMER_UPDATE".equals(action)) {
                handleTimerUpdate(intent);
            } else if ("END_OF_PERIOD_ACTION".equals(action)) {
                handlePeriodEndAction(context, intent);
            }
        }
    };
    
    /**
     * Handles timer update broadcasts
     * 
     * @param intent The intent containing timer information
     */
    private void handleTimerUpdate(Intent intent) {
        long timeRemaining = intent.getLongExtra("TIME_REMAINING", 0);
        int currentPeriod = intent.getIntExtra("CURRENT_PERIOD", 1);

        // Calculate minutes and seconds from timeRemaining
        long seconds = (timeRemaining / 1000) % 60;
        long minutes = (timeRemaining / (1000 * 60)) % 60;

        // Format the time as MM:SS
        timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);

        // Update the TextView's color and text
        updateTimerColor(minutes);
        updatePeriodDisplay(currentPeriod);
    }
    
    /**
     * Updates the timer display color based on remaining time
     * 
     * @param minutes Minutes remaining
     */
    private void updateTimerColor(long minutes) {
        if (minutes < 1) {
            tvTimeRem.setTextColor(Color.rgb(200, 0, 0)); // Red for less than a minute
        } else {
            tvTimeRem.setTextColor(Color.BLACK); // Default color
        }
        tvTimeRem.setText(timeFormatted);
    }
    
    /**
     * Updates the period display (quarter or half)
     * 
     * @param currentPeriod The current period number
     */
    private void updatePeriodDisplay(int currentPeriod) {
        if (iNumPers == 2) {
            tvQuarterNum.setText("Half:\n" + currentPeriod);
        } else {
            tvQuarterNum.setText("Quarter:\n" + currentPeriod);
        }
    }
    
    /**
     * Handles period end action broadcasts
     * 
     * @param context The context
     * @param intent The intent containing period information
     */
    private void handlePeriodEndAction(Context context, Intent intent) {
        int currentPeriod = intent.getIntExtra("CURRENT_PERIOD", 1);
        int numPeriods = intent.getIntExtra("TOTAL_PERIODS", 4);
        Toast.makeText(context, "Period " + currentPeriod + " has ended.", Toast.LENGTH_SHORT).show();
        EndOfPeriodTimer();
    }

    /**
     * Saves the current game state when the fragment is paused
     */
    @Override
    public void onPause() {
        super.onPause();
        saveGameState();
        saveGameStats();
    }
    
    /**
     * Saves the current game state to SharedPreferences
     */
    private void saveGameState() {
        spSavedValues = requireContext().getSharedPreferences("MySharedPref", Context.MODE_PRIVATE);
        SharedPreferences.Editor spEditor = spSavedValues.edit();
        
        // Save scores and statistics
        saveScoreStats(spEditor);
        
        // Save team information
        saveTeamInfo(spEditor);
        
        // Save game state
        saveGameProgress(spEditor);
        
        spEditor.apply();
    }
    
    /**
     * Saves score statistics to SharedPreferences
     */
    private void saveScoreStats(SharedPreferences.Editor editor) {
        editor.putInt("iScore1", iScore1);
        editor.putInt("iScore2", iScore2);
        editor.putInt("iGS1", iGS1);
        editor.putInt("iGA1", iGA1);
        editor.putInt("iGS1M", iGS1M);
        editor.putInt("iGA1M", iGA1M);
        editor.putInt("iGS2", iGS2);
        editor.putInt("iGA2", iGA2);
        editor.putInt("iGS2M", iGS2M);
        editor.putInt("iGA2M", iGA2M);
    }
    
    /**
     * Saves team information to SharedPreferences
     */
    private void saveTeamInfo(SharedPreferences.Editor editor) {
        editor.putString("tvTeam1", tvTeam1.getText().toString());
        editor.putString("etTeam2", etTeam2.getText().toString());
        editor.putString("sGSPlayer", sGSPlayer);
        editor.putString("sGAPlayer", sGAPlayer);
        
        String gameLogForPrefs = viewModel.getCurrentActionsLogString();
        editor.putString("GameLog", gameLogForPrefs);
    }
    
    /**
     * Saves game progress information to SharedPreferences
     */
    private void saveGameProgress(SharedPreferences.Editor editor) {
        editor.putBoolean("GameInProgress", viewModel.getGameInProgress().getValue());
        editor.putString("GameMode", viewModel.getGameMode().getValue());
        
        // Save period as integer to avoid type conversion issues
        Integer periodValue = viewModel.getCurrentPeriod().getValue();
        if (periodValue != null) {
            editor.putInt("CurrPeriod", periodValue);
        } else {
            editor.putInt("CurrPeriod", 1); // Default value
        }
    }
    
    /**
     * Saves the current game statistics to the database
     */
    private void saveGameStats() {
        String gameLogForPrefs = viewModel.getCurrentActionsLogString();
        
        GameStats stats = new GameStats(
                new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date()),
                tvTeam1.getText().toString(),
                etTeam2.getText().toString(),
                iScore1,
                iScore2,
                gameLogForPrefs
        );

        new Thread(() -> {
            AppDatabase db = MyApplication.getDatabase();
            db.gameStatsDao().insertGameStats(stats);
        }).start();
    }

    /**
     * Restores the game state when the fragment is resumed
     */
    @Override
    public void onResume() {
        super.onResume();
        spSavedValues = requireContext().getSharedPreferences("MySharedPref", Context.MODE_PRIVATE);
        
        // Restore scores and statistics
        restoreScoreStats();
        
        // Restore team information
        restoreTeamInfo();
        
        // Restore game progress
        restoreGameProgress();
        
        // Restore scoring attempts
        restoreScoringAttempts();
        
        // Update UI with game mode
        updateGameModeUI();
    }
    
    /**
     * Restores score statistics from SharedPreferences
     */
    private void restoreScoreStats() {
        iGS1 = spSavedValues.getInt("iGS1", 0);
        iGA1 = spSavedValues.getInt("iGA1", 0);
        iGS1M = spSavedValues.getInt("iGS1M", 0);
        iGA1M = spSavedValues.getInt("iGA1M", 0);
        iGS2 = spSavedValues.getInt("iGS2", 0);
        iGA2 = spSavedValues.getInt("iGA2", 0);
        iGS2M = spSavedValues.getInt("iGS2M", 0);
        iGA2M = spSavedValues.getInt("iGA2M", 0);
    }
    
    /**
     * Restores team information from SharedPreferences
     */
    private void restoreTeamInfo() {
        sTeam1 = spSavedValues.getString("tvTeam1", sTeam1);
        sTeam2 = spSavedValues.getString("etTeam2", sTeam2);
        sGSPlayer = spSavedValues.getString("sGSPlayer", sGSPlayer);
        sGAPlayer = spSavedValues.getString("sGAPlayer", sGAPlayer);
    }
    
    /**
     * Restores game progress information from SharedPreferences
     */
    private void restoreGameProgress() {
        viewModel.setGameInProgress(spSavedValues.getBoolean("GameInProgress", viewModel.getGameInProgress().getValue()));
        viewModel.setGameMode(spSavedValues.getString("GameMode", viewModel.getGameMode().getValue()));
        
        // Fix for ClassCastException: String cannot be cast to Integer
        try {
            // First try to get as integer (for backward compatibility)
            int period = spSavedValues.getInt("CurrPeriod", 1);
            viewModel.setCurrentPeriod(period);
        } catch (ClassCastException e) {
            // If that fails, get as string and parse
            String periodStr = spSavedValues.getString("CurrPeriod", "1");
            try {
                int period = Integer.parseInt(periodStr);
                viewModel.setCurrentPeriod(period);
            } catch (NumberFormatException nfe) {
                // If parsing fails, use default value
                viewModel.setCurrentPeriod(1);
                Log.e("Frag_Gameplay", "Error parsing period value: " + periodStr, nfe);
            }
        }
        
        bTimerRunning = viewModel.getGameInProgress().getValue();
    }
    
    /**
     * Restores scoring attempts from SharedPreferences
     */
    private void restoreScoringAttempts() {
        String actionsLogFromPrefs = spSavedValues.getString("GameLog", null);
        if ((viewModel.currentActions.getValue() == null || viewModel.currentActions.getValue().isEmpty()) &&
                actionsLogFromPrefs != null && !actionsLogFromPrefs.isEmpty()) {

            List<ScoringAttempt> restoredAttempts = new ArrayList<>();
            String[] lines = actionsLogFromPrefs.split("\n");
            
            for (String line : lines) {
                if (line.trim().isEmpty()) continue;
                
                try {
                    parseScoringAttempt(line, restoredAttempts);
                } catch (Exception e) {
                    Log.e("FragGameplay", "Error parsing action line from SharedPreferences: " + line, e);
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
            // Use Locale.ROOT for internal string comparisons
            boolean isSuccessful = parts[2].trim().toLowerCase(Locale.ROOT).equals("goal");
            attempts.add(new ScoringAttempt(
                parts[0].trim(),  // playerName
                parts[1].trim(),  // playerPosition
                isSuccessful,     // isSuccessful
                parts[3].trim()   // timestamp
            ));
        } else {
            Log.w("FragGameplay", "Could not parse action line: " + line);
        }
    }
    
    /**
     * Updates the UI with the current game mode
     */
    private void updateGameModeUI() {
        String gameMode = viewModel.getGameMode().getValue();
        if (gameMode != null && !gameMode.isEmpty()) {
            btnGameMode.setText(gameMode);
        } else {
            btnGameMode.setText("15m,4Q");
        }
    }
    /**
     * Triggers device vibration with the specified pattern
     * 
     * @param inputPattern The vibration pattern to use
     */
    private void MakeitShake(long[] inputPattern) {
        try {
            Vibrator vibrator = (Vibrator) requireContext().getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null && vibrator.hasVibrator()) {
                vibrator.vibrate(inputPattern, -1); // -1 means don't repeat
            }
        } catch (Exception e) {
            Log.e("Frag_Gameplay", "Error triggering vibration", e);
        }
    }

    /**
     * Updates the game title based on team names
     * Creates a title in the format "Team1 vs Team2" if both teams have names,
     * or uses a single team name if only one is available
     * 
     * @return null (method signature kept for compatibility)
     */
    private View.OnClickListener updateGameTitle() {
        String team1Name = tvTeam1.getText().toString();
        String team2Name = etTeam2.getText().toString();

        String gameTitle = determineGameTitle(team1Name, team2Name);
        tvGameTitle.setText(gameTitle);
        
        return null; // Method signature kept for compatibility
    }
    
    /**
     * Determines the appropriate game title based on available team names
     * 
     * @param team1Name The name of team 1
     * @param team2Name The name of team 2
     * @return The appropriate game title
     */
    private String determineGameTitle(String team1Name, String team2Name) {
        if (!team1Name.isEmpty() && !team2Name.isEmpty()) {
            // Both team names available
            return team1Name + " vs " + team2Name;
        } else if (!team1Name.isEmpty()) {
            // Only Team 1 name available
            return team1Name;
        } else if (!team2Name.isEmpty()) {
            // Only Team 2 name available
            return team2Name;
        } else {
            // No team names available
            return "Game Title";
        }
    }

    /**
     * Sets up the UI by adding touch listeners to hide keyboard when touching outside EditText fields
     * Recursively applies to all views in the hierarchy
     * 
     * @param view The root view to set up
     */
    private void setupUI(View view) {
        setupTouchListenerForView(view);
        setupChildViews(view);
    }
    
    /**
     * Sets up touch listener for a view to hide keyboard when touched
     * 
     * @param view The view to set up
     */
    private void setupTouchListenerForView(View view) {
        // Only add touch listener to non-EditText views
        if (!(view instanceof EditText)) {
            view.setOnTouchListener((v, event) -> {
                hideSoftKeyboard();
                clearFocusFromEditTexts();
                return false; // Allow the touch event to continue to underlying views
            });
        }
    }
    
    /**
     * Recursively sets up child views in a ViewGroup
     * 
     * @param view The parent view
     */
    private void setupChildViews(View view) {
        // If the view is a container, recursively set up its children
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View innerView = viewGroup.getChildAt(i);
                setupUI(innerView);
            }
        }
    }

    /**
     * Hides the soft keyboard
     */
    private void hideSoftKeyboard() {
        try {
            Activity activity = requireActivity();
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
            
            // Find the currently focused view to get a window token
            View focusedView = activity.getCurrentFocus();
            
            // If no view has focus, create a temporary one
            if (focusedView == null) {
                focusedView = new View(activity);
            }
            
            inputMethodManager.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
        } catch (Exception e) {
            Log.e("Frag_Gameplay", "Error hiding keyboard", e);
        }
    }

    /**
     * Clears focus from any focused EditText
     */
    private void clearFocusFromEditTexts() {
        try {
            Activity activity = requireActivity();
            View focusedView = activity.getCurrentFocus();
            
            if (focusedView instanceof EditText) {
                focusedView.clearFocus();
            }
        } catch (Exception e) {
            Log.e("Frag_Gameplay", "Error clearing focus", e);
        }
    }

    /**
     * Cleans up resources when the view is destroyed
     * Unregisters the broadcast receiver to prevent memory leaks
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        try {
            Context context = getContext();
            if (context != null) {
                context.unregisterReceiver(timerReceiver);
            }
        } catch (IllegalArgumentException e) {
            // Receiver might not be registered
            Log.w("Frag_Gameplay", "Error unregistering receiver: " + e.getMessage());
        }
    }

    /**
     * Animates the centre pass circle between teams
     * This visual indicator shows which team has the centre pass
     */
    private void startImageAnimation() {
        // Validate that animation can be performed
        if (ivCentrePassCircle == null || endX == 0) {
            return;
        }
        
        // Show the animation circle
        ivCentrePassCircle.setVisibility(View.VISIBLE);

        // Create and configure the animation
        createAndStartAnimation();
    }
    
    /**
     * Creates and starts the animation for the centre pass indicator
     */
    private void createAndStartAnimation() {
        // Check if we're in landscape mode
        boolean isLandscape = ScreenSizeHelper.isLandscape(requireContext());
        
        if (isLandscape) {
            // In landscape mode, animate horizontally (X axis)
            float targetX = movingToEndLocation ? endX : startX;
            animatorY = ObjectAnimator.ofFloat(ivCentrePassCircle, "X", ivCentrePassCircle.getX(), targetX);
        } else {
            // In portrait mode, animate vertically (Y axis)
            float targetY = movingToEndLocation ? endY : startY;
            animatorY = ObjectAnimator.ofFloat(ivCentrePassCircle, "Y", ivCentrePassCircle.getY(), targetY);
        }
        animatorY.setDuration(200); // Ensure smooth animation both ways
        animatorY.setInterpolator(new AccelerateDecelerateInterpolator());

        // Add animation listener
        animatorY.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                movingToEndLocation = !movingToEndLocation; // Flip direction for next movement
            }

            @Override
            public void onAnimationStart(Animator animation) {
                // Not used
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                // Not used
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                // Not used
            }
        });
        
        // Start the animation
        animatorY.start();
    }
}

class PlayerShotStats {
    String playerName;
    String playerPosition;
    int goals;
    int misses;

    public PlayerShotStats(String position) {
        this.playerName = playerName;
        this.playerPosition = position;
        this.goals = 0;
        this.misses = 0;
    }

    public String playerName() {
        return playerName;
    }

    public String playerPosition() {
        return playerPosition;
    }

    public int getGoals() {
        return goals;
    }

    public void incrementGoals() {

        this.goals++;
    }

    public int getMisses() {
        return misses;
    }

    public void incrementMisses() {
        this.misses++;
    }

    @Override
    public String toString() {
        return "PlayerShotStats{" +
                "Player='" + playerName + '\'' +
                ", goals=" + goals +
                ", misses=" + misses +
                '}';
    }
}
// TODO     Use nice pretty icons
// TODO     Generate sub-out routines, keep track of players' in-game time; Add maybe an array to keep track
//     of player on and off times, and sum up total in-game time, maybe just a single string with all sub events.
// TODO     Add players' player (best on court function)
// TODO     Data to hold stats for each player position
// TODO     Allow swiping from one tab to the next
// TODO     Change Main Activity from buttons to tab layout
// TODO     Change away from and Disable Stats and TeamList when game timer is less than 10 seconds, to stop end-of period glitching


// Done:
// TO DO     Buzz for in-game button clicks, points, end of playing time
// TO DO     Going from a game-in-progress to Stats and back is fine, but going to TeamList disables the buttons.// TO DO     Backing out of a game in progress also disables the buttons.// TO DO     Add first name of player in GS and GA to button
// TO DO     Export saved games to file; only commit finalised games.
// TO DO     Fix up exports
// TO DO     Fix up Playerlist display (RecyclerView)
// TO DO     Implement reset game
// TO DO     Implement enabled/disabled buttons
// TO DO     Assign player name to positions
// TO DO     Implement quarter/Half timer (game mode and time); copy from other version
// TO DO     Save Game/app Data when exiting and reload when restarting
// TO DO     Bring chosen team name into Gameplay
// TO DO     Change colours for centre pass change
// TO DO     Add Game Mode and Period Number and total Periods to both SharedModelView and SharedPref, also to be reset by reset button.
// TO DO     Put end of period note in gamestats




