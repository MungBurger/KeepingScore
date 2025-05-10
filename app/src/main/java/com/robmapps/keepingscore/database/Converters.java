package com.robmapps.keepingscore.database;

import android.util.Log;

import androidx.room.TypeConverter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.robmapps.keepingscore.Player;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;


public class Converters {
    private static final Gson gson = new Gson();

    @TypeConverter
    public static String fromList(List<String> players) {
        return gson.toJson(players);
    }

    @TypeConverter
    public static List<String> toList(String json) {
        Type type = new TypeToken<List<String>>() {}.getType();
        return gson.fromJson(json, type);
    }
    @TypeConverter
    public static String fromPlayerList(List<Player> players) {
        String json = gson.toJson(players);
        Log.d("DatabaseDebug", "Saving players to JSON: " + json);
        return json;
    }

    @TypeConverter
    public static List<Player> toPlayerList(String playerString) {
        Log.d("DatabaseDebug", "Loading players from JSON: " + playerString);
        Type listType = new TypeToken<List<Player>>() {}.getType();
        return gson.fromJson(playerString, listType);
    }


}