package com.example.timetracker;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "task_days",
        indices = {@Index(value = {"taskId", "date"}, unique = true)}
)
public class TaskDay {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public long taskId; // Foreign key to Task
    public String date; // Format: "DD-MM-YYYY"
    public int timeLogged; // Total time logged for this task in minutes

    public TaskDay(long taskId, String date, int timeLogged) {
        this.taskId = taskId;
        this.date = date;
        this.timeLogged = timeLogged;
    }
}