package com.robmapps.keepingscore.database.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "game_actions",
    foreignKeys = @ForeignKey(
        entity = GameStats.class,
        parentColumns = "id",
        childColumns = "game_id",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {@Index("game_id")}
)
public class GameAction {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "game_id")
    public int gameId;

    @ColumnInfo(name = "team_name")
    public String teamName;

    @ColumnInfo(name = "player_position")
    public String playerPosition;

    @ColumnInfo(name = "action_type")
    public String actionType; // "Goal", "Miss", "Period End", etc.

    @ColumnInfo(name = "player_name")
    public String playerName;

    @ColumnInfo(name = "timestamp")
    public String timestamp;

    @ColumnInfo(name = "sequence")
    public int sequence; // To maintain the order of actions

    public GameAction(int gameId, String teamName, String playerPosition, String actionType, 
                     String playerName, String timestamp, int sequence) {
        this.gameId = gameId;
        this.teamName = teamName;
        this.playerPosition = playerPosition;
        this.actionType = actionType;
        this.playerName = playerName;
        this.timestamp = timestamp;
        this.sequence = sequence;
    }
}