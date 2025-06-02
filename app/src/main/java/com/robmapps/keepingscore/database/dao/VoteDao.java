/*
package com.robmapps.keepingscore.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.robmapps.keepingscore.database.entities.Vote;

import java.util.List;

// VoteDao.java
@Dao
public interface VoteDao {
    @Insert
    void insert(Vote vote);

    @Query("SELECT votedForPlayerId, votedForName, COUNT(votedForPlayerId) as totalVotes " +
            "FROM votes " +
            "WHERE NOT isCoachVote " + // Exclude coach's votes from player-voted tallies if needed
            // "AND seasonId = :seasonId " + // If you add season filtering
            "GROUP BY votedForPlayerId, votedForName " +
            "ORDER BY totalVotes DESC")
    LiveData<List<PlayerVoteSummary>> getPlayerVoteSummary(); // PlayerVoteSummary is a POJO

    @Query("SELECT votedForPlayerId, votedForName, COUNT(votedForPlayerId) as totalVotes " +
            "FROM votes " +
            "WHERE isCoachVote " +
            // "AND seasonId = :seasonId " +
            "GROUP BY votedForPlayerId, votedForName " +
            "ORDER BY totalVotes DESC")
    LiveData<List<PlayerVoteSummary>> getCoachVoteSummary(); // PlayerVoteSummary is a POJO

    @Query("SELECT * FROM votes WHERE gameId = :gameId")
    LiveData<List<Vote>> getVotesForGame(long gameId);
}

// POJO for summarizing votes
public class PlayerVoteSummary {
    public int votedForPlayerId;
    public String votedForName;
    public int totalVotes;
}*/
