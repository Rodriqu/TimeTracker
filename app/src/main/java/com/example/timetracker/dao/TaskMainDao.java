package com.example.timetracker.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.timetracker.TaskMain;

import java.util.List;

@Dao
public interface TaskMainDao {
    @Insert
    long insert(TaskMain task);

    @Query("SELECT * FROM tasks")
    List<TaskMain> getAllTasks();

    @Query("UPDATE tasks SET name = :name WHERE id = :taskId")
    void updateName(String name, long taskId);
}