package com.robmapps.keepingscore.database.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import java.util.ArrayList;
import java.util.List;

import com.robmapps.keepingscore.Player;

@Entity(tableName = "teams")
public class Team {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "teamName")
    private String teamName;

    @ColumnInfo(name = "score")
    private int score;

    @ColumnInfo(name = "players")
    private List<Player> players;

    // Parameterized constructor to create a new Team with a given team name
    public Team(String teamName) {
        this.teamName = teamName;
        this.score = 0;
        // Initialize players here to avoid null issues
        this.players = new ArrayList<>();
    }

    // --- Getters ---
    public int getId() {
        return id;
    }
    public String getTeamName() {
        return teamName;
    }
    public int getScore() {
        return score;
    }
    public List<Player> getPlayers() {
        // Return a copy if you want to prevent external modification
        // return new ArrayList<>(players);
        return players;
    }

    // --- Setters ---
    public void setId(int id) {
        this.id = id;
    }
    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }
    public void setScore(int score) {
        this.score = score;
    }
    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true; // Check if both references point to the same object
        if (obj == null || getClass() != obj.getClass()) return false; // Check null and class type
        Team team = (Team) obj;
        return id == team.id && // Compare IDs
                teamName.equals(team.teamName) && // Compare names
                score == team.score; // Compare scores
    }
    // Optional: Add hashCode() if you override equals()
    // This is good practice, especially if you put Team objects in collections like HashSets or HashMaps.
    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + teamName.hashCode();
        result = 31 * result + score;
        // Consider including players in hashCode calculation
        // result = 31 * result + (players != null ? players.hashCode() : 0);
        return result;
    }

    // Optional: Override toString() for easier logging/debugging
    @Override
    public String toString() {
        return "Team{" +
                "id=" + id +
                ", teamName='" + teamName + '\'' +
                ", players=" + players +
                ", score=" + score +
                '}';
    }

}
//    TODO I've analyzed Team.java. It correctly defines a Room entity for storing team data, including an auto-generated
//     primary key (id), team name, score, and list of players. The use of equals() ensures proper object comparisons,
//     but since players isn't included, two teams with the same name and score will be considered equal
//     even if their player lists differ.