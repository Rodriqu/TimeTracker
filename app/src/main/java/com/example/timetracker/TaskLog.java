package com.example.timetracker;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "task_logs")
public class TaskLog {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public long taskId; // Foreign key to Task
    public String dayOfWeek; // Day of week
    public int hourLoggedAt; // hour at which time was logged

    public int timeLogged; //timeLogged (added by LogTaskDialog) or 0 if edited or added by swiping

    public TaskLog(long taskId, String dayOfWeek, int hourLoggedAt, int timeLogged) {
        this.taskId = taskId;
        this.dayOfWeek = dayOfWeek;
        this.hourLoggedAt = hourLoggedAt;
        this.timeLogged = timeLogged;
    }

    @Ignore
    public TaskLog(long taskId, String dayOfWeek, int hourLoggedAt) {
        this.taskId = taskId;
        this.dayOfWeek = dayOfWeek;
        this.hourLoggedAt = hourLoggedAt;
        this.timeLogged = 0;
    }
}