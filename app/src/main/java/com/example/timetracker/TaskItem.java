package com.example.timetracker;

public class TaskItem {

    private long id;
    private String name;
    private int time;

    //points for smartSort
    private int smartPoints;
    private int updateLeft;
    private int updateRight;

    public TaskItem(long id, String name, int time, int updateLeft, int updateRight) {
        this.id = id;
        this.name = name;
        this.time = time;
        smartPoints = 0;
        this.updateLeft = updateLeft;
        this.updateRight = updateRight;
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

    public int getSmartPoints() { return smartPoints; }

    public long getId() { return id; }

    public int getUpdateLeft() {
        return updateLeft;
    }

    public int getUpdateRight() {
        return updateRight;
    }

    public String getTextUpdateLeft(){
        return getTextUpdate(this.updateLeft);
    }

    public String getTextUpdateRight(){
        return getTextUpdate(this.updateRight);
    }

    private String getTextUpdate(int updateTime){
        if (updateTime == 0) {
            return "";
        }

        String sign;
        if (updateTime > 0){
            sign = "+";
        }
        else {
            sign = "-";
        }

        int hours = updateTime / 60;
        int minutes = updateTime % 60;

        if (hours == 0) {
            return sign + minutes + "m";
        }

        if (minutes == 0) {
            return sign + hours + "h";
        }

        if (minutes < 10) {
            return sign + hours + "h 0" + minutes + "m";
        }

        return sign + hours + "h " + minutes + "m";
    }

    public void setSmartPoints(int smartPoints) {
        this.smartPoints = smartPoints;
    }
}
