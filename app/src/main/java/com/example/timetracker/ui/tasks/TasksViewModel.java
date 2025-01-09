package com.example.timetracker.ui.tasks;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.timetracker.AppDatabaseInstance;
import com.example.timetracker.TaskComparators;
import com.example.timetracker.TaskDay;
import com.example.timetracker.TaskItem;
import com.example.timetracker.TaskLog;
import com.example.timetracker.TaskMain;
import com.example.timetracker.dao.AppDatabase;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import kotlinx.coroutines.scheduling.Task;

public class TasksViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    private final MutableLiveData<List<TaskItem>> tasksItems;

    private final MutableLiveData<LocalDate> selectedDate = new MutableLiveData<>(LocalDate.now());

    private final AppDatabase db;

    private Comparator selectedComparator = TaskComparators.BY_TIME;

    public TasksViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is tasks fragment");

        tasksItems = new MutableLiveData<>(new ArrayList<>());

        db = AppDatabaseInstance.getInstance();

        this.fetchTaskItems();
    }

    public LiveData<LocalDate> getSelectedDate() {
        return selectedDate;
    }

    public String dateToStringForDB(LocalDate date){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        return date.format(formatter);
    }

    public String dateToStringForDB(){
        if (selectedDate.getValue() == null) return "";
        return dateToStringForDB(selectedDate.getValue());
    }

    public void setSelectedDate(LocalDate date) {
        selectedDate.setValue(date);
        this.fetchTaskItems();
    }

    public void fetchTaskItems(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<TaskItem> taskItemsFetched = tasksItems.getValue();
                if(taskItemsFetched != null && selectedDate.getValue() != null) {

                    taskItemsFetched.clear();
                    // Insert task into Task database
                    List<TaskDay> taskDays = db.taskDayDao().getTasksOnDay(dateToStringForDB(selectedDate.getValue()));
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
    public void addNewTask(String name, int time, LocalDate date) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // Insert task into Task database
                TaskMain taskMain = new TaskMain(name);
                long taskId = db.taskMainDao().insert(taskMain);

                // Create a log entry and insert into TaskLog database
                TaskDay taskDay = new TaskDay(taskId, dateToStringForDB(date), time);
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

    public void editTaskDetails(TaskItem taskItem, LocalDate date){
        if (taskItem == null || date == null) return;

        new Thread(new Runnable() {
            @Override
            public void run() {
                db.taskMainDao().updateName(taskItem.getName(), taskItem.getId());
                db.taskDayDao().upsert(taskItem.getId(), dateToStringForDB(date), taskItem.getTime());
                db.taskLogDao().insert(new TaskLog(taskItem.getId(), getDayOfWeekToday(), getHourNow()));
                fetchTaskItems();
            }
        }).start();
    }

    public void addTimeToTask(TaskItem taskItem, int timeToAddMinutes, LocalDate date) {
        if (taskItem == null || timeToAddMinutes == 0 || date == null) return;
        //Wpisz zmianę do DB i odśwież front pobierając zmiany z DB - przypadek gdyby zmieniono dzień - dlatego z taskItem pobierane jest tylko id i sortowanie

        new Thread(new Runnable() {
            @Override
            public void run() {
                db.taskDayDao().addTimeOrInsert(taskItem.getId(), dateToStringForDB(date), timeToAddMinutes);
                db.taskLogDao().insert(new TaskLog(taskItem.getId(), getDayOfWeekToday(), getHourNow(), timeToAddMinutes));
                fetchTaskItems();
            }
        }).start();
    }

    // Update the task time at a given position direction -> 1=right, -1=left
    public void updateTaskTime(int position, int direction) {

        // Najpierw aktualizacja frontu
        List<TaskItem> currentTasks = tasksItems.getValue();
        String date = dateToStringForDB(getSelectedDate().getValue());
        if (currentTasks != null && position >= 0 && position < currentTasks.size() && date != null) {
            TaskItem task = currentTasks.get(position);
            task.updateTime(direction*15);
            tasksItems.setValue(currentTasks);  // Notify LiveData

            // Potem wpisanie nowej wartości do DB
            new Thread(new Runnable() {
                @Override
                public void run() {
                    db.taskDayDao().upsert(task.getId(), date, task.getTime());
                    db.taskLogDao().insert(new TaskLog(task.getId(), getDayOfWeekToday(), getHourNow()));
                    fetchTaskItems();
                }
            }).start();
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

    public String getDayOfWeek(LocalDate date){
        return date.getDayOfWeek()
                .getDisplayName(TextStyle.FULL, Locale.ENGLISH);
    }

    public String getDayOfWeekToday(){
        return getDayOfWeek(LocalDate.now());
    }

    public int getHourNow(){
        return LocalTime.now().getHour();
    }

    public LiveData<List<TaskItem>> getTasksItems() {
        return tasksItems;
    }
    public LiveData<String> getText() {
        return mText;
    }
}

