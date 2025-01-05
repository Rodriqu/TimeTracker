package com.example.timetracker;

import java.util.Comparator;

public class TaskComparators {
    public static final Comparator<TaskItem> BY_NAME = (task1, task2) ->
            task1.getName().compareToIgnoreCase(task2.getName());

    public static final Comparator<TaskItem> BY_TIME = Comparator.comparingInt(TaskItem::getTime);
}