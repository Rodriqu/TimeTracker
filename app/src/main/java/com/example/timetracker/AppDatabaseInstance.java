package com.example.timetracker;

import android.content.Context;

import androidx.room.Room;

import com.example.timetracker.dao.AppDatabase;

public class AppDatabaseInstance {
    private static AppDatabase instance;

    public static AppDatabase initialize(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    AppDatabase.class,
                    "tasks_database"
            ).build();
        }
        return instance;
    }

    public static AppDatabase getInstance() {
        return instance;
    }
}