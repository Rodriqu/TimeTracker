package com.example.timetracker.dao;
import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.timetracker.TaskDay;

import java.util.List;

@Dao
public interface TaskDayDao {
    @Insert
    void insert(TaskDay taskDay);

    @Query("SELECT timeLogged FROM task_days WHERE date = :date AND taskId = :taskId")
    Integer getTimeForTaskDate(long taskId, String date);

    @Query("SELECT * FROM task_days WHERE date = :date")
    List<TaskDay> getTasksOnDay(String date);

    @Query("SELECT SUM(timeLogged) FROM task_days WHERE taskId = :taskId")
    Integer getTotalTimeForTask(long taskId);

    @Query("UPDATE task_days SET timeLogged = :newTime WHERE taskId = :taskId AND date = :date")
    void setNewTime(long taskId, String date, int newTime);

    @Query("SELECT COUNT(*) FROM task_days WHERE taskId = :taskId AND date = :date")
    int checkIfTaskExists(long taskId, String date);

    // Insert task if it doesn't exist
    @Insert
    void insertTaskIfNotExists(TaskDay taskDay);

    // Wrapper method that checks and inserts
    default void upsert(long taskId, String date, int newTime) {
        if (checkIfTaskExists(taskId, date) == 0) {
            insert(new TaskDay(taskId, date, newTime));
        }
        else{
            setNewTime(taskId, date, newTime);
        }
    }

}
