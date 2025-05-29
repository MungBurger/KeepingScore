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

import java.text.SimpleDateFormat;
import java.util.Date;
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

        Spinner dropdown = view.findViewById(R.id.statsDropdown); // Replace with correct ID from fragment_stats.xml

        viewModel.getAllGameStats().observe(getViewLifecycleOwner(), stats -> {
            if (stats != null) {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                        android.R.layout.simple_spinner_dropdown_item,
                        stats.stream().map(game -> game.gameDate + ": " + game.team1Name + " vs " + game.team2Name)
                                .collect(Collectors.toList()));
                dropdown.setAdapter(adapter); // Correctly set the adapter for the Spinner

            }
        });
        tvFullGameStatsString = view.findViewById(R.id.tvFullGameStatsString); // Initialize the new TextView

        gameStats = readGameStats("", new StringBuilder());
        if (gameStats != null) {
            tvFullGameStatsString.setText(gameStats.toString());
        }
        btnSaveStats.setOnClickListener(v -> saveGameStats());
        return view;
    }

    private String readGameStats(String fileName, StringBuilder exportFileContent) {
        String gameLogContent = viewModel.getCurrentActionsLogString();
        //OutputStream fos = null; // Use OutputStream
        //Uri uri = null;
        //StringBuilder exportFileContent = new StringBuilder();

        List<ScoringAttempt> actionsList = viewModel.getAllActions().getValue();
        if (actionsList != null && !actionsList.isEmpty()) {
            for (int i = 0; i < actionsList.size(); i++) {
                ScoringAttempt attempt = actionsList.get(i);
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

                Map<String, PlayerShotStats> playerShootingStats = ShotAnalyser.analyzeShotData(gameLogContent);

                if (!playerShootingStats.isEmpty()) {
                    exportFileContent.append("\n--- Player Shooting Stats ---\n");
                    for (Map.Entry<String, PlayerShotStats> entry : playerShootingStats.entrySet()) {
                        String playerName = entry.getKey();
                        PlayerShotStats stats = entry.getValue();
                        int totalShots = stats.getGoals() + stats.getMisses();
                        double accuracy = 0.0;
                        if (totalShots > 0) {
                            accuracy = ((double) stats.getGoals() / totalShots) * 100;
                        }
                        exportFileContent.append(String.format(Locale.getDefault(),
                                "%s: %d Goals, %d Misses (Accuracy: %.1f%%)\n",
                                playerName, stats.getGoals(), stats.getMisses(),  accuracy));
                    }
                    exportFileContent.append("---------------------------\n");
                } else {
                    exportFileContent.append("\nNo shooting data to analyze for player percentages.\n");
                }

                exportFileContent.append("\n" + gameLogContent);

                //sAllActions=String.valueOf(sbExportStats);
                //fos.write(exportFileContent.toString().getBytes()); // Write stats content to file
                //Toast.makeText(getContext(), "Stats saved to Downloads folder: " + fileName, Toast.LENGTH_LONG).show();
            } else {
                //Toast.makeText(getContext(), "Failed to open output stream.", Toast.LENGTH_SHORT).show();
            }

        } /*catch (IOException e) {
            Toast.makeText(getContext(), "Failed to save stats: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();*/
         finally {
            /*if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }*/
        }
        return exportFileContent.toString();
    }
    private void saveGameStats(){
        String gameLogContent = viewModel.getCurrentActionsLogString();
        StringBuilder exportFileContent=new StringBuilder();
        String fileName =new String();
        OutputStream fos = null; // Use OutputStream
        Uri uri = null;
        //StringBuilder exportFileContent = new StringBuilder();
        fileName = "Netball Score-" + new SimpleDateFormat("yyyy-MM-dd-hh:mm", Locale.getDefault()).format(new Date()) + " " + viewModel.getActiveTeamName().getValue() + " v " + viewModel.getTeam2Name().getValue() + ".txt";

        List<ScoringAttempt> actionsList = viewModel.getAllActions().getValue();
        if (actionsList != null && !actionsList.isEmpty()) {
            for (int i = 0; i < actionsList.size(); i++) {
                ScoringAttempt attempt = actionsList.get(i);
                if(!attempt.toString().contains("-------========-------")){
                    exportFileContent.append(attempt.toString());
                }else {
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

                Map<String, PlayerShotStats> playerShootingStats = ShotAnalyser.analyzeShotData(gameLogContent);

                if (!playerShootingStats.isEmpty()) {
                    exportFileContent.append("\n--- Player Shooting Stats ---\n");
                    for (Map.Entry<String, PlayerShotStats> entry : playerShootingStats.entrySet()) {
                        String playerName = entry.getKey();
                        PlayerShotStats stats = entry.getValue();
                        int totalShots = stats.getGoals() + stats.getMisses();
                        double accuracy = 0.0;
                        if (totalShots > 0) {
                            accuracy = ((double) stats.getGoals() / totalShots) * 100;
                        }
                        exportFileContent.append(String.format(Locale.getDefault(),
                                "%s: %d Goals, %d Misses (Accuracy: %.1f%%)\n",
                                playerName, stats.getGoals(), stats.getMisses(),  accuracy));
                    }
                    exportFileContent.append("---------------------------\n");
                } else {
                    exportFileContent.append("\nNo shooting data to analyze for player percentages.\n");
                }

                exportFileContent.append("\n" + gameLogContent);

                //sAllActions=String.valueOf(sbExportStats);
                fos.write(exportFileContent.toString().getBytes()); // Write stats content to file
                //Toast.makeText(getContext(), "Stats saved to Downloads folder: " + fileName, Toast.LENGTH_LONG).show();
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
