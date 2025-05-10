/*
package com.robmapps.keepingscore;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.robmapps.keepingscore.database.entities.Team;

import java.util.ArrayList;

public class TeamAdapter extends ListAdapter<Team, TeamAdapter.TeamViewHolder> {

    public TeamAdapter(ArrayList<Player> playerNames, SharedViewModel viewModel) {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<Team> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Team>() {
                @Override
                public boolean areItemsTheSame(@NonNull Team oldItem, @NonNull Team newItem) {
                    return oldItem.id == newItem.id; // Compare by unique ID
                }

                @Override
                public boolean areContentsTheSame(@NonNull Team oldItem, @NonNull Team newItem) {
                    return oldItem.equals(newItem); // Compare entire content
                }
            };

    @NonNull
    @Override
    public TeamViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_gameplay, parent, false);
        return new TeamViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TeamViewHolder holder, int position) {
        Team team = getItem(position);
        holder.tvTeamName.setText(team.teamName); // Display team name
        holder.tvTeamScore.setText(String.valueOf(team.score)); // Display team score
    }

    static class TeamViewHolder extends RecyclerView.ViewHolder {
        TextView tvTeamName, tvTeamScore;

        public TeamViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTeamName = itemView.findViewById(R.id.T1Name);
            tvTeamScore = itemView.findViewById(R.id.Team1Score);
        }
    }
}
*/
