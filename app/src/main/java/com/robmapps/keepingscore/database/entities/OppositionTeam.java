package com.robmapps.keepingscore.database.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "opposition_teams")
public class OppositionTeam {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "team_name")
    public String teamName;

    @ColumnInfo(name = "last_played_date")
    public String lastPlayedDate;

    public OppositionTeam(String teamName, String lastPlayedDate) {
        this.teamName = teamName;
        this.lastPlayedDate = lastPlayedDate;
    }
}