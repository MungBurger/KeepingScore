package com.robmapps.keepingscore;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.base.Objects;

@Entity(tableName = "players")

public class Player {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name;
    private String position;
    private long timeOnCourt; // Total time on court in milliseconds
    private long lastEntryTime; // Timestamp of when the player last entered
    public static final String[] POSITIONS = {"GS", "GA", "WA", "C","WD", "GD", "GK",  "Off",  "Off",  "Off",  "Off"};

    public Player(String name, String position) {
        this.name = name;
        this.position = position;
    }
    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public long getTimeOnCourt() {
        return timeOnCourt;
    }

    public void addTimeOnCourt(long time) {
        this.timeOnCourt += time;
    }

    public long getLastEntryTime() {
        return lastEntryTime;
    }

    public void setLastEntryTime(long lastEntryTime) {
        this.lastEntryTime = lastEntryTime;
    }

}
/*

// Player.java (Room Entity - simplified for voting context)
@Entity(tableName = "players") // Or your actual table name
public class Player {
    @PrimaryKey // (autoGenerate = true if new players can be added dynamically)
    public int id;
    public String name;
    public String teamName; // Useful for grouping or display

    // Constructors
    public Player(int id, String name, String teamName) {
        this.id = id;
        this.name = name;
        this.teamName = teamName;
    }

    // Important for ArrayAdapter if you pass Player objects directly
    @NonNull
    @Override
    public String toString() {
        return name; // Spinner will display the player's name
    }

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getTeamName() { return teamName; }

    // It's good practice to implement equals and hashCode if you add Players to Sets or use them in Maps
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Player player = (Player) o;
        return id == player.id && Objects.equals(name, player.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}*/
