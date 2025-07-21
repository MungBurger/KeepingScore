package com.robmapps.keepingscore;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class GameStatsAdapter extends RecyclerView.Adapter<GameStatsAdapter.ViewHolder> {

    private final List<ScoringAttempt> attempts;

    public GameStatsAdapter(List<ScoringAttempt> attempts) {
        this.attempts = attempts;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_game_stat, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ScoringAttempt attempt = attempts.get(position);
        String playerName = attempt.getPlayerName();
        holder.tvPlayerName.setText(playerName != null && !playerName.isEmpty() ? playerName : "Player Name not recorded");
        holder.tvPlayerPosition.setText(attempt.getPlayerPosition());
        holder.tvActionType.setText(attempt.isSuccessful() ? "Goal" : "Miss");
        holder.tvTimestamp.setText(attempt.getTimestamp());
    }

    @Override
    public int getItemCount() {
        return attempts.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPlayerName,tvPlayerPosition, tvActionType, tvTimestamp;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPlayerName=itemView.findViewById(R.id.tvPlayerName);
            tvPlayerPosition = itemView.findViewById(R.id.tvPlayerPosition);
            tvActionType = itemView.findViewById(R.id.tvActionType);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
        }
    }
}

