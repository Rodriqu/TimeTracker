package com.example.timetracker.dao;
import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.timetracker.TaskDay;
import com.example.timetracker.TaskLog;
import com.example.timetracker.TaskMain;

@Database(entities = {TaskMain.class, TaskLog.class, TaskDay.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract TaskMainDao taskMainDao();
    public abstract TaskLogDao taskLogDao();

    public abstract TaskDayDao taskDayDao();
}