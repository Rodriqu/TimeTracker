package com.example.timetracker;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tasks")
public class TaskMain {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;

    public TaskMain(String name) {
        this.name = name;
    }
}
