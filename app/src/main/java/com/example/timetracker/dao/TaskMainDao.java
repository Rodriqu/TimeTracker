package com.example.timetracker.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.timetracker.TaskMain;

import java.util.List;

@Dao
public interface TaskMainDao {
    @Insert
    void insert(TaskMain task);

    @Query("SELECT * FROM tasks")
    LiveData<List<TaskMain>> getAllTasks();
}