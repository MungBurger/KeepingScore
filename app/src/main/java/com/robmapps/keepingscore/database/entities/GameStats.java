package com.robmapps.keepingscore.database.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
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

    @ColumnInfo(name = "game_start_time")
    public String gameStartTime;
    
    @ColumnInfo(name = "game_mode")
    public String gameMode;
    
    @ColumnInfo(name = "period_duration")
    public int periodDuration;

    @Ignore
    public GameStats(String gameDate, String team1Name, String team2Name, int team1Score, int team2Score) {
        this.gameDate = gameDate;
        this.team1Name = team1Name;
        this.team2Name = team2Name;
        this.team1Score = team1Score;
        this.team2Score = team2Score;
        this.gameStartTime = "";
        this.gameMode = "";
        this.periodDuration = 0;
    }
    
    @Ignore
    public GameStats(String gameDate, String gameStartTime, String team1Name, String team2Name, 
                    int team1Score, int team2Score, String gameMode, int periodDuration) {
        this.gameDate = gameDate;
        this.gameStartTime = gameStartTime;
        this.team1Name = team1Name;
        this.team2Name = team2Name;
        this.team1Score = team1Score;
        this.team2Score = team2Score;
        this.gameMode = gameMode;
        this.periodDuration = periodDuration;
    }
    public GameStats() {
    }
}
