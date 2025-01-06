package com.example.timetracker;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "task_logs")
public class TaskLog {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public int taskId; // Foreign key to Task
    public String date; // Format: "YYYY-MM-DD"
    public int hourLoggedAt; // hour at which time was logged

    public TaskLog(int taskId, String date, int hourLoggedAt) {
        this.taskId = taskId;
        this.date = date;
        this.hourLoggedAt = hourLoggedAt;
    }
}