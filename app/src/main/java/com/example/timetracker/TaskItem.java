package com.example.timetracker;

public class TaskItem {

    private long id;
    private String name;
    private int time;

    public TaskItem(long id, String name, int time) {
        this.id = id;
        this.name = name;
        this.time = time;
    }

    public String getTimeToDisplay(){
        if (time == 0) {
            return "";
        }

        int hours = this.time / 60;
        int minutes = time % 60;

        if (hours == 0) {
            return minutes + "m";
        }

        if (minutes == 0) {
            return hours + "h";
        }

        if (minutes < 10) {
            return hours + "h 0" + minutes + "m";
        }

        return hours + "h " + minutes + "m";
    }

    public void updateTime(int minutesToChange){
        time += minutesToChange;

        if (time < 0) {
            time = 0;
        }
    }

    public String getName() {
        return name;
    }

    public int getTime() {
        return time;
    }

    public long getId() { return id; }
}
