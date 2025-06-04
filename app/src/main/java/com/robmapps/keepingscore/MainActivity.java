package com.robmapps.keepingscore;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.widget.ViewPager2;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements Frag_TeamList.OnTeamActionListener {
    private SharedViewModel sharedViewModel;
    private ViewPager2 viewPager;
    public static final int TEAM_LIST_PAGE = 0;
    public static final int GAMEPLAY_PAGE = 1;
    public static final int STATS_PAGE = 2;
    private MyFragmentPagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);

        viewPager = findViewById(R.id.view_pager);
        pagerAdapter = new MyFragmentPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);
/*
        FragmentManager fragmentManager = getSupportFragmentManager();
        List<Fragment> fragments = fragmentManager.getFragments();
*/

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (viewPager.getCurrentItem() == 0) {
                    setEnabled(false); // Disable this callback
                    MainActivity.super.onBackPressed(); // Call Activity's default onBackPressed
                    // Or, if you want to ensure it's re-enabled for future state changes:
                    // if (!isFinishing()) { setEnabled(true); }
                } else {
                    viewPager.setCurrentItem(viewPager.getCurrentItem() - 1, true);
                }
            }
        });
/*        // Set the initial fragment if none exists
        if (savedInstanceState == null) {
            MyFragmentPagerAdapter.beginTransaction()
                    .add(R.id.fragmentContainerView1, new Frag_TeamList(), "Frag_TeamList")
                    .addToBackStack("Frag_TeamList") // Ensure the tag is also the back stack name
                    .commit();
        }*/

        Button btnGameplay = findViewById(R.id.btnGamePlay);
        btnGameplay.setOnClickListener(v -> {
            //activateFragment("Frag_Gameplay", new Frag_Gameplay());
            viewPager.setCurrentItem(GAMEPLAY_PAGE, true); // true for smooth scroll
        });
        Button btnTeamsList=findViewById(R.id.btnTeamsLists);
        btnTeamsList.setOnClickListener(v -> {
            //activateFragment("Frag_TeamList", new Frag_TeamList());
            viewPager.setCurrentItem(TEAM_LIST_PAGE, true); // true for smooth scroll

        });
        Button btnStats=findViewById(R.id.btnStats);
        btnStats.setOnClickListener(v -> {
            //activateFragment("Frag_Stats", new Frag_Stats());
            viewPager.setCurrentItem(STATS_PAGE, true); // true for smooth scroll
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