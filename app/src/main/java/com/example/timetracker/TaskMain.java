package com.example.timetracker;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tasks")
public class TaskMain {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public String name;

    public int updateLeft;

    public int updateRight;

    public TaskMain(String name, int updateLeft, int updateRight) {
        this.name = name;
        this.updateLeft = updateLeft;
        this.updateRight = updateRight;
    }
}
