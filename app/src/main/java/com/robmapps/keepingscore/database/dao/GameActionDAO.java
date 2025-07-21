package com.robmapps.keepingscore.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import com.robmapps.keepingscore.database.entities.GameAction;

import java.util.List;

@Dao
public interface GameActionDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertGameAction(GameAction action);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertGameActions(List<GameAction> actions);

    @Query("SELECT * FROM game_actions WHERE game_id = :gameId ORDER BY sequence ASC")
    LiveData<List<GameAction>> getActionsForGame(int gameId);

    @Query("DELETE FROM game_actions WHERE game_id = :gameId")
    void deleteActionsForGame(int gameId);
    
    @Query("SELECT * FROM game_actions WHERE game_id IN (:gameIds) ORDER BY game_id ASC, sequence ASC")
    LiveData<List<GameAction>> getActionsForGames(List<Integer> gameIds);
}