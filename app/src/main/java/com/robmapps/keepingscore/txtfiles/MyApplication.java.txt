package com.robmapps.keepingscore;

import android.app.Application;
import androidx.room.Room;
import com.robmapps.keepingscore.database.AppDatabase;

public class MyApplication extends android.app.Application {
    private static AppDatabase database;

    @Override
    public void onCreate() {
        super.onCreate();
        database = Room.databaseBuilder(getApplicationContext(),
                        AppDatabase.class, "game_database")
                .fallbackToDestructiveMigration()
                .build();
    }

    public static AppDatabase getDatabase() {
        return database;
    }
}
