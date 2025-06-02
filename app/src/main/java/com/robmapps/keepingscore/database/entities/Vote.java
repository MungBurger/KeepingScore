package com.robmapps.keepingscore.database.entities;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

// Vote.java (Room Entity)
@Entity(tableName = "votes",
        foreignKeys = {
                @ForeignKey(entity = androidx.media3.common.Player.class, // Assuming you have a Player entity
                        parentColumns = "id",
                        childColumns = "voterPlayerId",
                        onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = androidx.media3.common.Player.class,
                        parentColumns = "id",
                        childColumns = "votedForPlayerId",
                        onDelete = ForeignKey.CASCADE)
        },
        indices = {@Index("voterPlayerId"), @Index("votedForPlayerId")})
public class Vote {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public long gameId;          // Foreign key to a Game entity if you have one, or just a timestamp/identifier
    public String gameDate;      // Or a long timestamp for the game date

    public int voterPlayerId;    // ID of the player who is voting
    public String voterName;     // Denormalized for easier display, or fetch from Player table

    public int votedForPlayerId; // ID of the player being voted for
    public String votedForName;  // Denormalized for easier display

    public boolean isCoachVote;  // True if this vote is from the coach
    public String seasonID;
    // Consider adding a season identifier if you plan to run multiple seasons
    // public String seasonId;

    // Constructor, getters, setters
    public Vote(long gameId, String gameDate, int voterPlayerId, String voterName,
                int votedForPlayerId, String votedForName, boolean isCoachVote) {
        this.gameId = gameId;
        this.gameDate = gameDate;
        this.voterPlayerId = voterPlayerId;
        this.voterName = voterName;
        this.votedForPlayerId = votedForPlayerId;
        this.votedForName = votedForName;
        this.isCoachVote = isCoachVote;
    }
}