package com.robmapps.keepingscore.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.robmapps.keepingscore.database.entities.GameStats;

import java.util.List;

@Dao
public interface GameStatsDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertGameStats(GameStats stats);
    @Query("SELECT * FROM game_stats ORDER BY game_date DESC")
    LiveData<List<GameStats>> getAllGameStats();
    
    @Query("SELECT * FROM game_stats WHERE team1_name = :clubTeamName AND team2_name = :oppositionTeamName ORDER BY game_date DESC")
    LiveData<List<GameStats>> getGamesByOppositionTeam(String clubTeamName, String oppositionTeamName);

    @Query("DELETE FROM game_stats WHERE id = :id")
    void deleteGameStatsById(int id);
}
