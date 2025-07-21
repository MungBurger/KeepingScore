package com.robmapps.keepingscore;// MyFragmentPagerAdapter.java
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity; // Or FragmentManager + Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.robmapps.keepingscore.Frag_Gameplay;
import com.robmapps.keepingscore.Frag_OppositionStats;
import com.robmapps.keepingscore.Frag_Stats;
import com.robmapps.keepingscore.Frag_TeamList;

public class MyFragmentPagerAdapter extends FragmentStateAdapter {

    private static final int NUM_PAGES = 4; // Number of fragments/pages
    public static final int TEAM_LIST_PAGE = 0;
    public static final int GAMEPLAY_PAGE = 1;
    public static final int STATS_PAGE = 2;
    public static final int OPPOSITION_STATS_PAGE = 3;

    public MyFragmentPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    // Or, if using inside a Fragment:
    // public MyFragmentPagerAdapter(@NonNull Fragment fragment) {
    //    super(fragment);
    // }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Return a NEW fragment instance for the given position
        switch (position) {
            case 0:
                return new Frag_TeamList();
            case 1:
                return new Frag_Gameplay();
            case 2:
                return new Frag_Stats();
            case 3:
                return new Frag_OppositionStats();
            default:
                return new Frag_Gameplay(); // Should not happen
        }
    }

    @Override
    public int getItemCount() {
        return NUM_PAGES;
    }
}