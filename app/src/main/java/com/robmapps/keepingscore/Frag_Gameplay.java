package com.robmapps.keepingscore;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.content.IntentFilter;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.robmapps.keepingscore.database.AppDatabase;
import com.robmapps.keepingscore.database.entities.GameStats;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Frag_Gameplay extends Fragment {
    public TextView tvScore1, tvScore2, tvTimeRem,tvGameTitle, tvQuarterNum,tvTeam1;
    public EditText etTeam1,etTeam2;
    public int iScore1, iScore2, iGS1, iGA1, iGS2, iGA2, iGS1M, iGA1M, iGS2M, iGA2M, iNumPers,iPerDuration,iPerNum,iLength;
    public long TimeMultiplier;
    public String sScore1, timeFormatted,sTeam1,sTeam2,sAllStats, StatsFileName,filePath, sLastAction, sCentrePass,sInGameTime,sCurrMode;
    public Boolean bTimerRunning=false, bDebugMode=false;
    public Button btnShowStats, btnPlayerList, btnStartGame, btnBestOnCourt, btnLoadSaved,btnUndo,btnGameMode,btnReset;
    public Button btnGS1,btnGA1,btnGS1M,btnGA1M,btnGS2,btnGA2,btnGS2M,btnGA2M; /*For enabling and disabling*/
    public SharedPreferences spSavedValues;
    public CountDownTimer cdEndofPeriodTimer;
    public StringBuilder sAllActions;

    @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gameplay, container, false);
        sAllActions = new StringBuilder(0);
        // Access the ViewModel
        SharedViewModel viewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        // Bind UI elements
        tvScore1 = view.findViewById(R.id.Team1Score);
        tvScore2 = view.findViewById(R.id.Team2Score);
        tvGameTitle=view.findViewById(R.id.GameTitle);
        tvTeam1=view.findViewById(R.id.T1Name);
        etTeam2 = view.findViewById(R.id.T2Name); // Initialize EditText for Team 2

        btnGS1=view.findViewById(R.id.GS1);btnGA1=view.findViewById(R.id.GA1);btnGS1M=view.findViewById(R.id.GS1Miss);btnGA1M=view.findViewById(R.id.GA1Miss);
        btnGS2=view.findViewById(R.id.GS2);btnGA2=view.findViewById(R.id.GA2);btnGS2M=view.findViewById(R.id.GS2Miss);btnGA2M=view.findViewById(R.id.GA2Miss);
        btnUndo=view.findViewById(R.id.UndoButton);btnReset= view.findViewById(R.id.ResetGame);
        btnGS1.setEnabled(false);btnGA1.setEnabled(false);btnGS1M.setEnabled(false);btnGA1M.setEnabled(false);
        btnGS2.setEnabled(false);btnGA2.setEnabled(false);btnGS2M.setEnabled(false);btnGA2M.setEnabled(false);

        //sCurrMode = "10m,2H"; // Default mode
        tvTimeRem = view.findViewById(R.id.TimeRem);
        tvQuarterNum = view.findViewById(R.id.QuarterNum);

        // Observe Active Team Name
        viewModel.getActiveTeamName().observe(getViewLifecycleOwner(), teamName -> {
            if (teamName != null) {
                tvTeam1.setText(teamName); // Set the TextView to display the active team name
            }
        });

        TextView gameTimer = view.findViewById(R.id.TimeRem);
        TextView quarterNum = view.findViewById(R.id.QuarterNum);

        // Observe LiveData
        viewModel.getTeam1Score().observe(getViewLifecycleOwner(), score -> {
            tvScore1.setText(String.valueOf(score));
        });
        viewModel.getTeam2Score().observe(getViewLifecycleOwner(), score -> {
            tvScore2.setText(String.valueOf(score));
        });
        viewModel.getGameTimer().observe(getViewLifecycleOwner(), time -> {
            gameTimer.setText(time);
        });
        viewModel.getCurrentQuarter().observe(getViewLifecycleOwner(), quarter -> {
            quarterNum.setText("Quarter: " + quarter);
        });

        // Set listeners for Team 1 buttons
        btnGS1.setOnClickListener(v -> {            incrementScore(viewModel, tvScore1, "GS1", true);        });
        btnGA1.setOnClickListener(v -> {            incrementScore(viewModel, tvScore1, "GA1", true);        });
        btnGS1M.setOnClickListener(v -> {            incrementScore(viewModel, tvScore1, "GS1", false);        });
        btnGA1M.setOnClickListener(v -> {            incrementScore(viewModel, tvScore1, "GA1", false);        });
        // Set listeners for Team 2 buttons
        btnGS2.setOnClickListener(v -> {            incrementScore(viewModel, tvScore2, "GS2", true);        });
        btnGA2.setOnClickListener(v -> {            incrementScore(viewModel, tvScore2, "GA2", true);        });
        btnGS2M.setOnClickListener(v -> {            incrementScore(viewModel, tvScore2, "GS2", false);        });
        btnGA2M.setOnClickListener(v -> {            incrementScore(viewModel, tvScore2, "GA2", false);        });

        view.findViewById(R.id.Team1Score).setOnClickListener(v -> {            viewModel.swapCentrePass();         });
        view.findViewById(R.id.Team2Score).setOnClickListener(v -> {            viewModel.swapCentrePass();         });

        btnUndo.setOnClickListener(v -> {undoLastAction(viewModel);});

        btnGameMode = view.findViewById(R.id.GameMode); // Initialize the Button
        sCurrMode = btnGameMode.getText().toString(); // Default mode
        btnGameMode.setOnClickListener(v -> {GameMode(view);});
        btnStartGame = view.findViewById(R.id.btnStartGame);
        btnStartGame.setOnClickListener(v -> startGameTimer());
        btnGameMode.setOnLongClickListener(v -> GameModeDebug());
        btnReset.setOnClickListener(v -> resetGame());
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

        // Register BroadcastReceiver for Timer Updates
        Context context = getContext();
        if (context != null) {
            IntentFilter filter = new IntentFilter();
            filter.addAction("GAME_TIMER_UPDATE");
            filter.addAction("END_OF_PERIOD_ACTION");
            context.registerReceiver(timerReceiver, filter);
            Log.d("Frag_Gameplay", "BroadcastReceiver registered");
        } else {
            Log.e("Frag_Gameplay", "Context is null, BroadcastReceiver not registered");
        }
        return view;
    }

    private Void resetGame() {
        /*Routine to reset the game to zeros*/
        /*Done Make a confirmation requirement*/
        AlertDialog.Builder adBuilder = new AlertDialog.Builder(requireContext());
        adBuilder.setMessage("Confirm Clear current game?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                iScore1 = 0; iScore2 = 0;
                iGS1 = 0;
                iGA1 = 0;
                iGS1M = 0;
                iGA1M = 0;
                iGS2 = 0;
                iGA2 = 0;
                iGS2M = 0;
                iGA2M = 0;
                iPerNum=1;
                btnGS1.setEnabled(false);btnGA1.setEnabled(false);btnGS1M.setEnabled(false);btnGA1M.setEnabled(false);
                btnGS2.setEnabled(false);btnGA2.setEnabled(false);btnGS2M.setEnabled(false);btnGA2M.setEnabled(false);
                sAllActions.setLength(0);
                if (bTimerRunning) {
                    Intent stopIntent = new Intent(requireContext(), TimerService.class);
                    requireContext().stopService(stopIntent); // Stop the service
                    bTimerRunning = false; // Update the flag
                    Toast.makeText(requireContext(), "Game Timer stopped.", Toast.LENGTH_SHORT).show();
                } else {
                    // Toast.makeText(requireContext(), "Game Timer isn't running", Toast.LENGTH_SHORT).show();
                }
                if (cdEndofPeriodTimer == null) {
                    //Timer doesn't exist, or isn't running
                    // Toast.makeText(requireContext(), "ExtraTime Timer isn't running", Toast.LENGTH_SHORT).show();
                } else {
                    cdEndofPeriodTimer.cancel();
                }
                btnStartGame.setEnabled(true);
                tvTimeRem.setTextColor(Color.rgb(0,0,0));
                tvScore1.setText("" + iScore1);
                tvScore2.setText("" + iScore2);

                tvTimeRem.setText("Timer");
                tvQuarterNum.setText("Quarter");
                btnGameMode.setEnabled(true);
                onPause();
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //
            }
        });
        AlertDialog mDialog = adBuilder.create();
        mDialog.show();
        return null;
    }

    private void incrementScore(SharedViewModel viewModel, TextView scoreView, String playerPosition, boolean isSuccessful) {
        if (isSuccessful) {
            if (scoreView == tvScore1) {
                viewModel.updateTeam1Score(1); // Increment score for Team 1
            } else if (scoreView == tvScore2) {
                viewModel.updateTeam2Score(1); // Increment score for Team 2
            }
            viewModel.swapCentrePass();
        }
        String timestamp = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
        sAllActions.append("\n~ " + playerPosition + "GS1, " +" "+timeFormatted);
        viewModel.recordAttempt(playerPosition, isSuccessful, timestamp);
    }

    private void undoLastAction(SharedViewModel viewModel) {
        List<ScoringAttempt> currentActions = viewModel.getAllActions().getValue();
        if (currentActions != null && !currentActions.isEmpty()) {
            ScoringAttempt lastAction = currentActions.remove(currentActions.size() - 1);

            // Use the new method in SharedViewModel to update LiveData
            viewModel.updateAllActions(currentActions);

            // Adjust score if it was a successful goal
            if (lastAction.isSuccessful()) {
                if (lastAction.getPlayerPosition().startsWith("GS1") || lastAction.getPlayerPosition().startsWith("GA1")) {
                    viewModel.updateTeam1Score(-1); // Decrement score for Team 1
                    viewModel.swapCentrePass();
                } else if (lastAction.getPlayerPosition().startsWith("GS2") || lastAction.getPlayerPosition().startsWith("GA2")) {
                    viewModel.updateTeam2Score(-1); // Decrement score for Team 2
                    viewModel.swapCentrePass();
                }
            }
        }
    }
    public void GameMode(View view){
        bDebugMode=false;
        if (sCurrMode == null) {
            sCurrMode = "10m,2H"; // Fallback to a default value if null
        }
        switch (sCurrMode){
            case  "GameMode":
            case "01m,2H":
                sCurrMode="10m,4Q";
                iPerDuration=10;iNumPers=4;
                btnGameMode.setText(sCurrMode);
                break;
            case "10m,4Q":
                sCurrMode="12m,4Q";
                iPerDuration=12;iNumPers=4;
                btnGameMode.setText(sCurrMode);
                break;
            case "12m,4Q":
                sCurrMode="15m,4Q";
                iPerDuration=15;iNumPers=4;
                btnGameMode.setText(sCurrMode);
                break;
            case "15m,4Q": /*For debugging*/
                sCurrMode="10m,2H";
                iPerDuration=10;iNumPers=2;
                btnGameMode.setText(sCurrMode);
                break;
            case "10m,2H":
                sCurrMode="12m,2H";
                iPerDuration=10;iNumPers=2;
                btnGameMode.setText(sCurrMode);
                break;
            case "12m,2H":
                sCurrMode="15m,2H";
                iPerDuration=10;iNumPers=4;
                btnGameMode.setText(sCurrMode);
                break;
            case "15m,2H":
                sCurrMode="10m,4Q";
                iPerDuration=10;iNumPers=4;
                btnGameMode.setText(sCurrMode);
                break;
        }
    }
    public boolean GameModeDebug(){
        //Toast.makeText(MainActivity.this,"Debug mode",Toast.LENGTH_SHORT).show();
        sCurrMode="01m,2H";
        iPerDuration=1;iNumPers=2;
        btnGameMode.setText(sCurrMode);
        bDebugMode=true;
        Toast.makeText(requireContext(),"Debug mode",Toast.LENGTH_SHORT).show();
        return true;
    }

    private void startGameTimer() {
        if (bTimerRunning) {
            Toast.makeText(requireContext(), "Game Timer is already running!", Toast.LENGTH_SHORT).show();
            return;
        }

        btnGS1.setEnabled(true);btnGA1.setEnabled(true);btnGS1M.setEnabled(true);btnGA1M.setEnabled(true);
        btnGS2.setEnabled(true);btnGA2.setEnabled(true);btnGS2M.setEnabled(true);btnGA2M.setEnabled(true);
        sCurrMode = btnGameMode.getText().toString();

        parseGameMode(); // Ensure period duration and number of periods are set
        // Start the timer service
        Intent intent = new Intent(requireContext(), TimerService.class);
        intent.putExtra("PERIOD_DURATION", iPerDuration); // Pass the period duration
        intent.putExtra("TOTAL_PERIODS", iNumPers);       // Pass the total number of periods
        intent.putExtra("CURRENT_PERIOD", 1);            // Start with the first period
        requireContext().startService(intent);

        bTimerRunning = true; // Set the timer state to running
        Toast.makeText(requireContext(), "Game Timer Started!", Toast.LENGTH_SHORT).show();
        btnGameMode.setEnabled(false);btnStartGame.setEnabled(false);
    }
    private void EndOfPeriodTimer(){
        tvTimeRem.setTextColor(Color.rgb(255,100,0));
        if (bDebugMode){
            TimeMultiplier=6000;
        } else {
            TimeMultiplier=60000;
        }
        cdEndofPeriodTimer = new CountDownTimer(TimeMultiplier /3, 1000) { /* x minutes countdown after normal period is ended, until buttons are disabled.*/
            @Override
            public void onTick(long millisUntilFinished) {
                long hours = (millisUntilFinished / 1000 / 3600);
                long minutes = ((millisUntilFinished / 1000) % 3600) / 60;
                long seconds = (millisUntilFinished / 1000 % 60);
                timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);

                tvTimeRem.setText(""+timeFormatted);
                bTimerRunning=true;
            }
            @Override
            public void onFinish() {
                bTimerRunning=false;
                btnGS1.setEnabled(false);btnGA1.setEnabled(false);btnGS1M.setEnabled(false);btnGA1M.setEnabled(false);
                btnGS2.setEnabled(false);btnGA2.setEnabled(false);btnGS2M.setEnabled(false);btnGA2M.setEnabled(false);
            }
        }.start();
    }
    private void parseGameMode() {
        if (sCurrMode != null && !sCurrMode.isEmpty()) {
            int duration = Integer.parseInt(sCurrMode.substring(0, 2)); // Extract duration (first two digits)
            iPerDuration = duration; // Set period duration in minutes

            if (sCurrMode.contains("2H")) {
                iNumPers = 2; // 2 Halves
            } else if (sCurrMode.contains("4Q")) {
                iNumPers = 4; // 4 Quarters
            }
        }
    }

    private BroadcastReceiver timerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("GAME_TIMER_UPDATE".equals(action)) {
                long timeRemaining = intent.getLongExtra("TIME_REMAINING", 0);
                int currentPeriod = intent.getIntExtra("CURRENT_PERIOD", 1);

                // Calculate minutes and seconds from timeRemaining
                long seconds = (timeRemaining / 1000) % 60;
                long minutes = (timeRemaining / (1000 * 60)) % 60;

                // Format the time as MM:SS
                timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);

                // Update the TextView's color and text
                if (minutes < 1) {
                    tvTimeRem.setTextColor(Color.rgb(200, 0, 0)); // Red for less than a minute
                } else {
                    tvTimeRem.setTextColor(Color.BLACK); // Default color
                }
                tvTimeRem.setText(timeFormatted);
                // Update the quarter number
                tvQuarterNum.setText("Quarter: " + currentPeriod);
            } else if ("END_OF_PERIOD_ACTION".equals(action)) {
                int currentPeriod = intent.getIntExtra("CURRENT_PERIOD", 1);
                int totalPeriods = intent.getIntExtra("TOTAL_PERIODS", 4);

                // Trigger UI updates or alerts for end of period
                Toast.makeText(context, "Period " + currentPeriod + " has ended.", Toast.LENGTH_SHORT).show();
                EndOfPeriodTimer();
            }
        }
    };
    @Override
    public void onResume() {
        super.onResume();
        spSavedValues = requireContext().getSharedPreferences("MySharedPref", Context.MODE_PRIVATE);
        iGS1 = spSavedValues.getInt("iGS1",0);
        iGA1 = spSavedValues.getInt("iGA1",0);
        iGS1M = spSavedValues.getInt("iGS1M",0);
        iGA1M = spSavedValues.getInt("iGA1M",0);
        iGS2 = spSavedValues.getInt("iGS2",0);
        iGA2 = spSavedValues.getInt("iGA2",0);
        iGS2M = spSavedValues.getInt("iGS2M",0);
        iGA2M = spSavedValues.getInt("iGA2M",0);
        sTeam1=spSavedValues.getString("tvTeam1",sTeam1);
        sTeam2=spSavedValues.getString("etTeam2",sTeam2);
        sAllActions.setLength(0);
        sAllActions.append(spSavedValues.getString("sAllActions",sAllActions.toString()));

        tvTeam1.setText(sTeam1);
        etTeam2.setText(sTeam2);
    }

    public void onPause() {
        super.onPause();
        SharedViewModel viewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        spSavedValues = requireContext().getSharedPreferences("MySharedPref", Context.MODE_PRIVATE);

        SharedPreferences.Editor spEditor = spSavedValues.edit();

        spEditor.putInt("iScore1",iScore1);
        spEditor.putInt("iScore2",iScore2);
        spEditor.putInt("iGS1",iGS1);
        spEditor.putInt("iGA1",iGA1);
        spEditor.putInt("iGS1M",iGS1M);
        spEditor.putInt("iGA1M",iGA1M);
        spEditor.putInt("iGS2",iGS2);
        spEditor.putInt("iGA2",iGA2);
        spEditor.putInt("iGS2M",iGS2M);
        spEditor.putInt("iGA2M",iGA2M);
        spEditor.putString("tvTeam1",tvTeam1.getText().toString());
        spEditor.putString("etTeam2",etTeam2.getText().toString());
        spEditor.putString("sAllActions",sAllActions.toString());

        GameStats stats = new GameStats(
                new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date()),
                tvTeam1.getText().toString(), // Team 1 name
                "testname", //etTeam2.getText().toString(), // Team 2 name
                iScore1,                      // Team 1 score
                iScore2,                      // Team 2 score
                sAllActions.toString()        // Game log
        );

        new Thread(() -> {
            AppDatabase db = MyApplication.getDatabase();
            db.gameStatsDao().insertGameStats(stats);
        }).start();
    }

    private void exportGameStats(String fileName, String gameStats) {
        try {
            File file = new File(requireContext().getExternalFilesDir(null), fileName);
            FileWriter writer = new FileWriter(file);
            writer.write(gameStats); // Write stats content to file
            writer.close();
            Toast.makeText(getContext(), "Stats saved to " + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(getContext(), "Failed to save stats.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

/*
    // Usage example
    String fileName = "Netball Score-" + new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date())
            + " [" + team1Name + "] v [" + team2Name + "].csv";
    exportGameStats(fileName, gameStatsContent);
*/
@Override
public void onDestroyView() {
    super.onDestroyView();
    requireContext().unregisterReceiver(timerReceiver);
}

    private String formatTime(long timeInMillis) {
        long seconds = (timeInMillis / 1000) % 60;
        long minutes = (timeInMillis / (1000 * 60)) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}

// TODO     Buzz for points, end of playing time
// TODO     Use nice pretty icons
// TODO     Generate sub-out routines, keep track of players' in-game time; Add maybe an array to keep track of player on and off times, and sum up total in-game time
// TODO     Add players' player (best on court function)
// TODO     Add first name of player in GS and GA to button
// TODO     Export saved games to file; only commit finalised games.
// TODO     Fix up exports
// TODO     Fix up Playerlist display (RecyclerView)


// TO DO     Implement reset game
// TO DO     Implement enabled/disabled buttons
// TO DO     Assign player name to positions
// TO DO     Implement quarter/Half timer (game mode and time); copy from other version
// TO DO     Save Game/app Data when exiting and reload when restarting
// TO DO     Bring chosen team name into Gameplay
// TO DO     Change colours for centre pass change
