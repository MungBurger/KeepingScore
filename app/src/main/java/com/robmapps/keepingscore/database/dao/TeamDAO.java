package com.robmapps.keepingscore.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Insert;
import androidx.room.Delete;
import androidx.room.Update;

import com.robmapps.keepingscore.database.entities.Team;

import java.util.List;

@Dao
public interface TeamDAO {
    @Query("SELECT * FROM teams")
    LiveData<List<Team>> getAllTeams(); // Query to fetch all teams
    @Query("SELECT * FROM teams")
    List<Team> getTeamsDirect(); // Fetch all teams directly without LiveData
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertTeam(Team team);
    @Query("SELECT * FROM teams WHERE teamName = :teamName LIMIT 1")
    LiveData<Team> getTeamByName(String teamName);
    @Delete
    void deleteTeam(Team team);

    @Query("DELETE FROM teams")
    void deleteAllTeams();

    @Update
    void updateTeam(Team team);
}

