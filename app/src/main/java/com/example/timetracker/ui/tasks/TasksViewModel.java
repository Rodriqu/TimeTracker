package com.example.timetracker.ui.tasks;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.timetracker.AppDatabaseInstance;
import com.example.timetracker.TaskComparators;
import com.example.timetracker.TaskDay;
import com.example.timetracker.TaskItem;
import com.example.timetracker.TaskMain;
import com.example.timetracker.dao.AppDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TasksViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    private final MutableLiveData<List<TaskItem>> tasksItems;

    private final AppDatabase db;

    private Comparator selectedComparator = TaskComparators.BY_TIME;

    public TasksViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is tasks fragment");

        tasksItems = new MutableLiveData<>(new ArrayList<>());

        db = AppDatabaseInstance.getInstance();

        fetchTaskItems("06.01.2025");
    }

    public void fetchTaskItems(String date){
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<TaskItem> taskItemsFetched = tasksItems.getValue();
                if(taskItemsFetched != null) {
                    // Insert task into Task database
                    List<TaskDay> taskDays = db.taskDayDao().getTasksOnDay(date);
                    List<TaskMain> taskMains = db.taskMainDao().getAllTasks();

                    for (TaskMain taskMain : taskMains) {
                        // Find the TaskMain for the current TaskDay
                        TaskDay taskDay = findTaskDayByTaskId(taskDays, taskMain.id);
                        Log.d("DB", taskMain.id + " task");

                        if (taskDay != null) {
                            // Create TaskItem and add to the list
                            TaskItem taskItem = new TaskItem(taskMain.id, taskMain.name, taskDay.timeLogged);
                            taskItemsFetched.add(taskItem);
                        } else {
                            TaskItem taskItem = new TaskItem(taskMain.id, taskMain.name, 0);
                            taskItemsFetched.add(taskItem);
                        }
                    }

                    tasksItems.postValue(taskItemsFetched);
                    sortTasksAsync();
                }
            }
        }).start();

    }

    // Helper method to find TaskMain by ID
    private TaskMain findTaskMainById(List<TaskMain> taskMains, long taskId) {
        for (TaskMain taskMain : taskMains) {
            if (taskMain.id == taskId) {
                return taskMain;
            }
        }
        return null;
    }

    // Helper method to find TaskMain by ID
    private TaskDay findTaskDayByTaskId(List<TaskDay> taskDays, long Id) {
        for (TaskDay taskDay : taskDays) {
            if (taskDay.taskId == Id) {
                return taskDay;
            }
        }
        return null;
    }

    // Add a new task
    public void addNewTask(String name, int time, String date) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // Insert task into Task database
                TaskMain taskMain = new TaskMain(name);
                long taskId = db.taskMainDao().insert(taskMain);

                // Create a log entry and insert into TaskLog database
                TaskDay taskDay = new TaskDay(taskId, date, time);
                db.taskDayDao().insert(taskDay);

                List<TaskItem> currentTasks = tasksItems.getValue();
                if (currentTasks != null) {
                    TaskItem taskItem = new TaskItem(taskId, name, time);
                    currentTasks.add(taskItem);
                    tasksItems.postValue(currentTasks);  // Notify LiveData
                    sortTasksAsync();
                }
            }
        }).start();
    }

    // Update the task time at a given position direction -> 1=right, -1=left
    public void updateTaskTime(int position, int direction) {

        // Najpierw aktualizacja frontu
        List<TaskItem> currentTasks = tasksItems.getValue();
        if (currentTasks != null && position >= 0 && position < currentTasks.size()) {
            TaskItem task = currentTasks.get(position);
            task.updateTime(direction*15);
            tasksItems.setValue(currentTasks);  // Notify LiveData

            // Potem wpisanie nowej wartości do DB
            new Thread(() -> db.taskDayDao().setNewTime(task.getId(), task.getTime())).start();
        }
    }

    public void setComparator(Comparator<TaskItem> comparator){
        selectedComparator = comparator;
        this.sortTasksAsync();
    }

    public void sortTasksAsync(){
        List<TaskItem> currentTasks = tasksItems.getValue();
        if (currentTasks != null) {
            Collections.sort(currentTasks, selectedComparator);
            tasksItems.postValue(currentTasks); // Notify observers about the sorted list
        }
    }

    public LiveData<List<TaskItem>> getTasksItems() {
        return tasksItems;
    }
    public LiveData<String> getText() {
        return mText;
    }
}

