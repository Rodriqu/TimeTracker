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

    //more points is better
    public int calculatePointsForSmartSort(int hourNow){
        int margin = 2; //to add flexibility
        int points = 0;

        int hourDifference = Math.abs(hourLoggedAt - hourNow);

        // Close logs (within margin) are considered high priority
        if (hourDifference <= margin) {
            points += 20;  // High points for logs close to the current hour (within margin)
        } else if (hourDifference == margin + 1) {
            points += 5;   // Medium points for logs that are 1 hour away
        } else {
            points += 1;   // Low points for logs that are far from the current hour
        }

        return points;
    }
}