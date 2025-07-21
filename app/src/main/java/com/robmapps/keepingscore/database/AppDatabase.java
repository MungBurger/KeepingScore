package com.robmapps.keepingscore.database;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;
import android.content.Context;

import com.robmapps.keepingscore.database.dao.TeamDAO;
import com.robmapps.keepingscore.database.dao.GameStatsDAO;
import com.robmapps.keepingscore.database.dao.GameActionDAO;
import com.robmapps.keepingscore.database.dao.OppositionTeamDAO;
import com.robmapps.keepingscore.database.entities.Team;
import com.robmapps.keepingscore.database.entities.GameStats;
import com.robmapps.keepingscore.database.entities.GameAction;
import com.robmapps.keepingscore.database.entities.OppositionTeam;

@Database(entities = {Team.class, GameStats.class, GameAction.class, OppositionTeam.class}, version = 4, exportSchema = false)
@TypeConverters({Converters.class})

public abstract class AppDatabase extends RoomDatabase { // Ensure AppDatabase is public

    public abstract TeamDAO teamDao(); // Ensure teamDao() is public
    public abstract GameStatsDAO gameStatsDao();
    public abstract GameActionDAO gameActionDao();
    public abstract OppositionTeamDAO oppositionTeamDao();
    
    private static volatile AppDatabase INSTANCE;
    
    // Migration from version 2 to 3 - adding game_actions table
    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Create the new game_actions table
            database.execSQL("CREATE TABLE IF NOT EXISTS `game_actions` " +
                    "(`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`game_id` INTEGER NOT NULL, " +
                    "`team_name` TEXT, " +
                    "`player_position` TEXT, " +
                    "`action_type` TEXT, " +
                    "`player_name` TEXT, " +
                    "`timestamp` TEXT, " +
                    "`sequence` INTEGER NOT NULL, " +
                    "FOREIGN KEY(`game_id`) REFERENCES `game_stats`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
            
            // Create index on game_id for faster lookups
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_game_actions_game_id` ON `game_actions` (`game_id`)");
        }
    };
    
    // Migration from version 3 to 4 - adding opposition_teams table
    static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Create the opposition_teams table
            database.execSQL("CREATE TABLE IF NOT EXISTS `opposition_teams` " +
                    "(`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`team_name` TEXT, " +
                    "`last_played_date` TEXT)");
            
            // Create index on team_name for faster lookups
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_opposition_teams_team_name` ON `opposition_teams` (`team_name`)");
        }
    };

    public static AppDatabase getDatabase(final Context context) { // Ensure getDatabase is public
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "netball_database")
                            .addMigrations(MIGRATION_2_3, MIGRATION_3_4)
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}