package com.robmapps.keepingscore;

public class Player {
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

