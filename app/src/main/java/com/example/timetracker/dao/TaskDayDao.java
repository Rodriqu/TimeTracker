package com.example.timetracker.dao;
import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.timetracker.TaskDay;

import java.util.List;

@Dao
public interface TaskDayDao {
    @Insert
    void insert(TaskDay taskDay);

    @Query("SELECT timeLogged FROM task_days WHERE date = :date AND taskId = :taskId")
    LiveData<Integer> getTimeForTaskDate(int taskId, String date);

    @Query("SELECT SUM(timeLogged) FROM task_days WHERE taskId = :taskId")
    LiveData<Integer> getTotalTimeForTask(int taskId);
}
