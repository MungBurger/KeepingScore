package com.robmapps.keepingscore.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.robmapps.keepingscore.database.entities.OppositionTeam;

import java.util.List;

@Dao
public interface OppositionTeamDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertOppositionTeam(OppositionTeam team);

    @Query("SELECT * FROM opposition_teams ORDER BY team_name ASC")
    LiveData<List<OppositionTeam>> getAllOppositionTeams();

    @Query("SELECT * FROM opposition_teams WHERE team_name = :teamName LIMIT 1")
    LiveData<OppositionTeam> getOppositionTeamByName(String teamName);
    
    @Query("SELECT * FROM opposition_teams WHERE team_name = :teamName LIMIT 1")
    OppositionTeam getOppositionTeamByNameDirect(String teamName);

    @Query("UPDATE opposition_teams SET last_played_date = :lastPlayedDate WHERE team_name = :teamName")
    void updateLastPlayedDate(String teamName, String lastPlayedDate);
}