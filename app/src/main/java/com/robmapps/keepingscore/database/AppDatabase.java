// In AppDatabase.java
package com.robmapps.keepingscore.database; // Ensure correct package

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import android.content.Context;
import com.robmapps.keepingscore.database.dao.TeamDAO;
import com.robmapps.keepingscore.database.dao.GameStatsDAO;
import com.robmapps.keepingscore.database.entities.Team;
import com.robmapps.keepingscore.database.entities.GameStats;
import com.robmapps.keepingscore.database.Converters;

@Database(entities = {Team.class,GameStats.class}, version = 2, exportSchema = false)
@TypeConverters({Converters.class})

public abstract class AppDatabase extends RoomDatabase { // Ensure AppDatabase is public

    public abstract TeamDAO teamDao(); // Ensure teamDao() is public
    public abstract GameStatsDAO gameStatsDao();
    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) { // Ensure getDatabase is public
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "netball_database")
                            .build();
                    /*INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "netball_database")
                            .fallbackToDestructiveMigration() // Use only during development
                            .build();*/
                }
            }
        }
        return INSTANCE;
    }
}