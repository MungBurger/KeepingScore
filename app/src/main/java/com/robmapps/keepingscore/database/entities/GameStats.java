package com.robmapps.keepingscore.database.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "game_stats")
public class GameStats {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "game_date")
    public String gameDate;

    @ColumnInfo(name = "team1_name")
    public String team1Name;

    @ColumnInfo(name = "team2_name")
    public String team2Name;

    @ColumnInfo(name = "team1_score")
    public int team1Score;

    @ColumnInfo(name = "team2_score")
    public int team2Score;

    @ColumnInfo(name = "log")
    public String log; // Stores all in-game actions as a JSON or delimited string

    public GameStats(String gameDate, String team1Name, String team2Name, int team1Score, int team2Score, String log) {
        this.gameDate = gameDate;
        this.team1Name = team1Name;
        this.team2Name = team2Name;
        this.team1Score = team1Score;
        this.team2Score = team2Score;
        this.log = log;
    }
}
