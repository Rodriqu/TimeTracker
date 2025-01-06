package com.example.timetracker.ui.tasks;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.timetracker.AppDatabaseInstance;
import com.example.timetracker.MainActivity;
import com.example.timetracker.TaskComparators;
import com.example.timetracker.TaskItem;
import com.example.timetracker.TaskMain;
import com.example.timetracker.dao.AppDatabase;
import com.example.timetracker.dao.TaskMainDao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TasksViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    private final MutableLiveData<List<TaskItem>> tasks;

    private final AppDatabase db;

    private Comparator selectedComparator;

    public TasksViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is tasks fragment");

        tasks = new MutableLiveData<>(new ArrayList<>());

        db = AppDatabaseInstance.getInstance();

        //test data
        List<TaskItem> test_tasks = new ArrayList<>();
        test_tasks.add(new TaskItem("Task 1", 30));
        test_tasks.add(new TaskItem("Task 2", 56));

        tasks.setValue(test_tasks);
        this.setComparator(TaskComparators.BY_TIME); //default comparator
    }

    public LiveData<List<TaskMain>> getAllTasks(){
        return db.taskMainDao().getAllTasks();
    }

    // Add a new task
    public void addTask(TaskItem task) {
        List<TaskItem> currentTasks = tasks.getValue();
        if (currentTasks != null) {
            currentTasks.add(task);
            tasks.setValue(currentTasks);  // Notify LiveData

            new Thread(() -> )
            this.sortTasks();
        }
    }

    // Update the task time at a given position direction -> 1=right, -1=left
    public void updateTaskTime(int position, int direction) {
        List<TaskItem> currentTasks = tasks.getValue();
        if (currentTasks != null && position >= 0 && position < currentTasks.size()) {
            TaskItem task = currentTasks.get(position);
            task.updateTime(direction*15);
            tasks.setValue(currentTasks);  // Notify LiveData
        }
    }

    public void setComparator(Comparator<TaskItem> comparator){
        selectedComparator = comparator;
        this.sortTasks();
    }

    public void sortTasks(){
        List<TaskItem> currentTasks = tasks.getValue();
        if (currentTasks != null) {
            Collections.sort(currentTasks, selectedComparator);
            tasks.setValue(currentTasks); // Notify observers about the sorted list
        }
    }

    public LiveData<List<TaskItem>> getTasks() {
        return tasks;
    }
    public LiveData<String> getText() {
        return mText;
    }
}

