package com.example.timetracker;

public class TaskItem {
    private String name;
    private int time;

    public TaskItem(String name, int time) {
        this.name = name;
        this.time = time;
    }

    public String getName() {
        return name;
    }

    public int getTime() {
        return time;
    }
}
