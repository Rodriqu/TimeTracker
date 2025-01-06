package com.example.timetracker.dao;
import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.timetracker.TaskLog;

import java.util.List;

@Dao
public interface TaskLogDao {
    @Insert
    void insert(TaskLog taskLog);

    @Query("SELECT * FROM task_logs WHERE date = :date")
    LiveData<List<TaskLog>> getLogsForDate(String date);

    @Query("SELECT SUM(hourLoggedAt) FROM task_logs WHERE taskId = :taskId")
    LiveData<Integer> getTotalTimeForTask(int taskId);
}