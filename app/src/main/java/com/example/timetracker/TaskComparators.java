package com.example.timetracker;

import androidx.annotation.NonNull;

import java.util.Comparator;

public enum TaskComparators {
    BY_NAME((task1, task2) -> task1.getName().compareToIgnoreCase(task2.getName())),
    BY_TIME(Comparator.comparingInt(TaskItem::getTime)),
    BY_TIME_REVERSED(Comparator.comparingInt(TaskItem::getTime).reversed()),
    BY_SMART(Comparator.comparingInt(TaskItem::getSmartPoints));

    private final Comparator<TaskItem> comparator;

    TaskComparators(Comparator<TaskItem> comparator) {
        this.comparator = comparator;
    }

    public Comparator<TaskItem> getComparator() {
        return comparator;
    }

    // Method to get the next comparator in cyclic order
    public TaskComparators getNextComparator() {
        TaskComparators[] values = TaskComparators.values();
        return values[(this.ordinal() + 1) % values.length];
    }

    @NonNull
    @Override
    public String toString() {
        switch (this) {
            case BY_NAME:
                return "Sorting by Name";
            case BY_TIME:
                return "Sorting by Time Low to High";
            case BY_TIME_REVERSED:
                return "Sorting by Time High To low";
            case BY_SMART:
                return "Smart Sorting";
            default:
                return super.toString();
        }
    }
}