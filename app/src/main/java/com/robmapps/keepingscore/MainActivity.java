package com.robmapps.keepingscore;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements Frag_TeamList.OnTeamActionListener {
    private SharedViewModel sharedViewModel;
    public String sTeamName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FragmentManager fragmentManager = getSupportFragmentManager();
        List<Fragment> fragments = fragmentManager.getFragments();
        boolean noActiveFragments = true; // Assume no active fragments initially\


        // Register the back press callback
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Check if there are any fragments in the back stack
                if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                    // Close the app if no fragments are left
                    finish(); // Ends the activity
                } else {
                    // Navigate to the previous fragment
                    getSupportFragmentManager().popBackStack();
                }
            }
        });
        // Set the initial fragment if none exists
        if (savedInstanceState == null) {
            fragmentManager.beginTransaction()
                    .add(R.id.fragmentContainerView1, new Frag_FrontPage(), "Frag_FrontPage")
                    .addToBackStack("Frag_FrontPage") // Ensure the tag is also the back stack name
                    .commit();
        }

        Button btnFront=findViewById(R.id.btnFrontPage);
        btnFront.setOnClickListener(v -> {
            activateFragment("Frag_FrontPage", new Frag_FrontPage());
        });
        Button btnGameplay = findViewById(R.id.btnGamePlay);
        btnGameplay.setOnClickListener(v -> {
            activateFragment("Frag_Gameplay", new Frag_Gameplay());
        });
        Button btnTeamsList=findViewById(R.id.btnTeamsLists);
        btnTeamsList.setOnClickListener(v -> {
            activateFragment("Frag_TeamList", new Frag_TeamList());
        });
        Button btnStats=findViewById(R.id.btnStats);
        btnStats.setOnClickListener(v -> {
            activateFragment("Frag_Stats", new Frag_Stats());
        });
    }

    private void activateFragment(String fragmentTag, Fragment newFragmentInstance) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment desiredFragment = null;
        // Check active fragments
        for (Fragment fragment : fragmentManager.getFragments()) {
            if (fragment != null && fragmentTag.equals(fragment.getTag())) {
                desiredFragment = fragment;
                break;
            }
        }
        // If not found in active fragments, check back stack
        if (desiredFragment == null) {
            int backStackCount = fragmentManager.getBackStackEntryCount();
            for (int i = backStackCount - 1; i >= 0; i--) { // Iterate backward through the stack
                FragmentManager.BackStackEntry entry = fragmentManager.getBackStackEntryAt(i);
                if (fragmentTag.equals(entry.getName())) {
                    fragmentManager.popBackStack(entry.getName(), 0); // Pop back stack up to this fragment without removing it
                    desiredFragment = fragmentManager.findFragmentByTag(fragmentTag); // Retrieve it from active fragments
                    break;
                }
            }
        }
        // Activate or add the fragment
        if (desiredFragment != null) {
            fragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainerView1, desiredFragment, fragmentTag) // Ensure the fragment is displayed
                    .commit();
        } else {
            fragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainerView1, newFragmentInstance, fragmentTag)
                    .addToBackStack(fragmentTag)
                    .commit();
        }
    }
    @Override
    public void onTeamSaved(String teamName, ArrayList<String> playerNames) {
        Log.d("MainActivity", "Team Saved: " + teamName + ", Players: " + playerNames);
    }

    @Override
    public void onTeamChosen() {
        Log.d("MainActivity", "Team Chosen!");
    }

    @Override
    public void onTeamEdited(String teamName, ArrayList<String> playerNames) {
        Log.d("MainActivity", "Team Edited: " + teamName + ", Players: " + playerNames);
    }
    public SharedViewModel getSharedViewModel() {
        return sharedViewModel;
    }

    /*@Override   //Uncomment this if you want the exit warning
    public void onBackPressed() {
        // Check if the back stack is empty
        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            showExitConfirmationDialog(); // Show warning
        } else {
            super.onBackPressed(); // Navigate back
        }
    }
    private void showExitConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Exit App?")
                .setMessage("Are you sure you want to exit? Any unsaved data will be lost.")
                .setPositiveButton("Yes", (dialog, which) -> finish()) // Exit the app
                .setNegativeButton("No", (dialog, which) -> reloadGameplayFragment()) // Reload Frag_Gameplay
                .create()
                .show();
    }*/
    private void reloadGameplayFragment() {
        // Reload Frag_Gameplay
        activateFragment("Frag_Gameplay", new Frag_Gameplay());
    }
}