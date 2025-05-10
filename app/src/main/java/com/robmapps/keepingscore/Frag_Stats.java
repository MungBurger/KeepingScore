package com.robmapps.keepingscore;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.stream.Collectors;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Frag_Stats# newInstance} factory method to
 * create an instance of this fragment.
 */
public class Frag_Stats extends Fragment {

    private GameStatsAdapter adapter;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stats, container, false);

        Spinner dropdown = view.findViewById(R.id.statsDropdown); // Replace with correct ID from fragment_stats.xml
        RecyclerView rvCurrentGameStats = view.findViewById(R.id.rvCurrentGameStats);

        rvCurrentGameStats.setLayoutManager(new LinearLayoutManager(requireContext()));

        SharedViewModel viewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        viewModel.getAllGameStats().observe(getViewLifecycleOwner(), stats -> {
            if (stats != null) {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                        android.R.layout.simple_spinner_dropdown_item,
                        stats.stream().map(game -> game.gameDate + ": " + game.team1Name + " vs " + game.team2Name)
                                .collect(Collectors.toList()));
                dropdown.setAdapter(adapter); // Correctly set the adapter for the Spinner

            }
        });




        return view;
    }
}
