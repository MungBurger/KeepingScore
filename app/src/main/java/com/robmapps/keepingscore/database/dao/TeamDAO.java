package com.robmapps.keepingscore.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Insert;
import androidx.room.Delete;
import androidx.room.Update;

import java.util.List;

import com.robmapps.keepingscore.database.entities.Team;

@Dao
public interface TeamDAO {
    @Update
    void updateTeam(Team team);
    @Query("SELECT * FROM teams")
    List<Team> getTeamsDirect(); // Fetch all teams directly without LiveData
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertTeam(Team team);
    @Query("SELECT * FROM teams WHERE teamName = :teamName LIMIT 1")
    LiveData<Team> getTeamByName(String teamName);
    @Delete
    void deleteTeam(Team team);
    @Query("DELETE FROM teams")
    void deleteAllTeams();
    @Query("DELETE FROM teams WHERE teamName = :teamName")
    void deleteTeamByName(String teamName);
    @Query("SELECT * FROM teams ORDER BY teamName ASC")
    LiveData<List<Team>> getAllTeams();
    @Query("SELECT * FROM teams WHERE id = :teamId")
    LiveData<Team> getTeamById(int teamId);
    @Query("SELECT * FROM teams WHERE teamName = :teamName")
    LiveData<Team> getTeamByNameLive(String teamName);

}

