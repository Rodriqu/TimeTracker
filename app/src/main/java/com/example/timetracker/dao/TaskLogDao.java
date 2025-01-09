package com.example.timetracker.dao;
import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.timetracker.TaskDay;
import com.example.timetracker.TaskLog;

import java.util.List;

@Dao
public interface TaskLogDao {
    @Insert
    void insert(TaskLog taskLog);

    @Query("SELECT * FROM task_logs WHERE taskId = :taskId AND dayOfWeek = :dayOfWeek")
    List<TaskLog> getLogsForTaskDayOfWeek(long taskId, String dayOfWeek);

    @Query("SELECT * FROM task_logs WHERE taskId = :taskId")
    List<TaskLog> getLogsForTask(long taskId);
}