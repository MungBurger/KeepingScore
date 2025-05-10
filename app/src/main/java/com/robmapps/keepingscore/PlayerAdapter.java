package com.robmapps.keepingscore;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PlayerAdapter extends RecyclerView.Adapter<PlayerAdapter.PlayerViewHolder> {

    private List<Player> playerList;
    private OnPlayerActionListener listener;

    public PlayerAdapter(List<Player> playerList, OnPlayerActionListener listener) {
        this.playerList = playerList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PlayerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_player_name, parent, false);
        return new PlayerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlayerViewHolder holder, int position) {
        Player player = playerList.get(position);

        holder.etPlayerNameItem.setText(player.getName());//setting the player's name

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(holder.itemView.getContext(),
                android.R.layout.simple_spinner_dropdown_item,
                Player.POSITIONS);
        holder.spPosition.setAdapter(spinnerAdapter);
        holder.spPosition.setSelection(getPositionIndex(player.getPosition()));
        // Handle player deletion
        holder.btnDeletePlayer.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPlayerDeleted(player);
            }
        });

        // Handle position change
        holder.spPosition.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            /*public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                String selectedPosition = Player.POSITIONS[pos];
                player.setPosition(selectedPosition);
                if (listener != null) {
                    listener.onPositionChanged(player);
                }
            }*/
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                String selectedPos = Player.POSITIONS[pos];
                // Only update if the selection actually changed for this player.
                if (!player.getPosition().equals(selectedPos)) {
                    // Update the player's position
                    player.setPosition(selectedPos);

                    // Check for duplicates in all other players.
                    for (Player otherPlayer : playerList) {
                        // If another player (different from this one) is occupying the selected position,
                        // change that other player's position to "Off"
                        if (otherPlayer != player && otherPlayer.getPosition().equals(selectedPos)) {
                            otherPlayer.setPosition("Off");
                        }
                    }
                    // Notify adapter to update all views
                    notifyDataSetChanged();
                    if (listener != null) {
                        listener.onPositionChanged(player);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    @Override
    public int getItemCount() {
        return playerList.size();
    }

    static class PlayerViewHolder extends RecyclerView.ViewHolder {
        EditText etPlayerNameItem;
        Spinner spPosition;
        ImageButton btnDeletePlayer;

        public PlayerViewHolder(@NonNull View itemView) {
            super(itemView);
            etPlayerNameItem = itemView.findViewById(R.id.etPlayerNameItem);
            spPosition = itemView.findViewById(R.id.spPosition);
            btnDeletePlayer = itemView.findViewById(R.id.btnDeletePlayer);
        }
    }

    private int getPositionIndex(String position) {
        for (int i = 0; i < Player.POSITIONS.length; i++) {
            if (Player.POSITIONS[i].equals(position)) {
                return i;
            }
        }
        return 0; // Default to first position if not found
    }

    public interface OnPlayerActionListener {
        void onPlayerDeleted(Player player);

        void onPositionChanged(Player player);
    }
}
