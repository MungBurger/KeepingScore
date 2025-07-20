package com.robmapps.keepingscore;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
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
    private static Drawable defaultSpinnerBackground;
    private static int offPositionColor = Color.parseColor("#FFCDD2");
    public PlayerAdapter(List<Player> playerList, OnPlayerActionListener listener) {
        this.playerList = playerList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PlayerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_player_name, parent, false);
        PlayerViewHolder holder = new PlayerViewHolder(view);

        // Capture the default spinner background ONCE when the first ViewHolder is created.
        // This assumes all spinners initially have the same default background.
        if (defaultSpinnerBackground == null && holder.spPosition != null) {
            defaultSpinnerBackground = holder.spPosition.getBackground();
        }
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

        holder.spPosition.setSelection(getPositionIndex(player.getPosition()), false); // false to not trigger onItemSelected

        // --- Spinner Background Color Logic ---
        if ("Off".equalsIgnoreCase(player.getPosition())) {
            holder.spPosition.setBackgroundColor(offPositionColor);
        } else {
            if (defaultSpinnerBackground != null) {
                holder.spPosition.setBackground(defaultSpinnerBackground);
            } else {
                // Fallback if defaultSpinnerBackground wasn't captured or is null
                // This might be a system default and might not match your app's theme perfectly.
                // For API < 16, use setBackgroundDrawable.
                holder.spPosition.setBackgroundResource(android.R.drawable.btn_dropdown);
            }
        }

        // --- Listeners ---
        // Clear existing listeners to prevent multiple calls on recycled views
        holder.etPlayerNameItem.setOnFocusChangeListener(null);
        holder.spPosition.setOnItemSelectedListener(null);

        // Handle position change
        holder.spPosition.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
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
                    // Use more specific notify methods instead of notifyDataSetChanged
                    // Update the current item that changed position
                    notifyItemChanged(holder.getAdapterPosition());
                    
                    // Find and update any other players whose positions were changed to "Off"
                    for (int i = 0; i < playerList.size(); i++) {
                        if (i != holder.getAdapterPosition() && playerList.get(i).getPosition().equals("Off")) {
                            notifyItemChanged(i);
                        }
                    }
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

    class PlayerViewHolder extends RecyclerView.ViewHolder {
        EditText etPlayerNameItem;
        Spinner spPosition;

        public PlayerViewHolder(@NonNull View itemView) {
            super(itemView);
            etPlayerNameItem = itemView.findViewById(R.id.etPlayerNameItem);
            spPosition = itemView.findViewById(R.id.spPosition);
            //btnDeletePlayer = itemView.findViewById(R.id.btnDeletePlayer);
            // New Below
            etPlayerNameItem.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    Player player = playerList.get(getAdapterPosition());
                    player.setName(((EditText) v).getText().toString());
                }
            });
            //New Above
        }
    }
    public Player getPlayerAt(int position) {
        if (playerList != null && position >= 0 && position < playerList.size()) {
            return playerList.get(position);
        }
        return null; // Or throw exception
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
