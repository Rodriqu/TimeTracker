package com.example.timetracker.ui.tasks;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.timetracker.TaskItem;

import java.util.ArrayList;
import java.util.List;

public class TasksViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    private final MutableLiveData<List<TaskItem>> tasks = new MutableLiveData<>(new ArrayList<>());

    public TasksViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is tasks fragment");

        //test data
        List<TaskItem> test_tasks = new ArrayList<>();
        test_tasks.add(new TaskItem("Task 1", 30));
        test_tasks.add(new TaskItem("Task 2", 60));

        tasks.setValue(test_tasks);
    }

    public LiveData<List<TaskItem>> getTasks() {
        return tasks;
    }
    public LiveData<String> getText() {
        return mText;
    }
}