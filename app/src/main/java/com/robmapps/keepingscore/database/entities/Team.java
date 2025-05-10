package com.robmapps.keepingscore.database.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;

import com.robmapps.keepingscore.Player;

import java.util.ArrayList;
import java.util.List;

@Entity(tableName = "teams")
public class Team {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "teamName")
    public String teamName;

    @ColumnInfo(name = "score")
    public int score;

    @ColumnInfo(name = "players")
    public List<Player> players;

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true; // Check if both references point to the same object
        if (obj == null || getClass() != obj.getClass()) return false; // Check null and class type
        Team team = (Team) obj;
        return id == team.id && // Compare IDs
                teamName.equals(team.teamName) && // Compare names
                score == team.score; // Compare scores
    }

    // Default constructor (required by Room)
    public Team() {
    }

    // Parameterized constructor to create a new Team with a given team name
    public Team(String teamName) {
        this.teamName = teamName;
        this.score = 0;
        this.players = new ArrayList<>();
    }

    // Getters and setters can be added here if needed.
}
//    TODO I've analyzed Team.java. It correctly defines a Room entity for storing team data, including an auto-generated
//     primary key (id), team name, score, and list of players. The use of equals() ensures proper object comparisons,
//     but since players isn't included, two teams with the same name and score will be considered equal
//     even if their player lists differ.