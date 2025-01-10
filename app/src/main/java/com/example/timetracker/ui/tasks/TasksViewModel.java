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
import java.util.List;
import java.util.Locale;

public class TasksViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    private final MutableLiveData<List<TaskItem>> tasksItems;

    private final MutableLiveData<LocalDate> selectedDate = new MutableLiveData<>(LocalDate.now());

    private final AppDatabase db;

    private TaskComparators selectedComparator = TaskComparators.BY_SMART;

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
                }
            }).start();
        }
    }

    public void setComparator(TaskComparators comparator){
        selectedComparator = comparator;
        // reload tasks and sort
        this.fetchTaskItems();
    }

    public String setNextSortOrder(){
        TaskComparators taskComparators = selectedComparator.getNextComparator();
        setComparator(taskComparators);
        return taskComparators.toString();
    }

    public void sortTasksAsync(){
        List<TaskItem> currentTasks = tasksItems.getValue();
        if (currentTasks != null) {
            if (selectedComparator == TaskComparators.BY_SMART) {
                for (TaskItem taskItem : currentTasks) {
                    taskItem.setSmartPoints(calculatePointsForSmartSortForTask(taskItem.getId()));
                }
            }
            Collections.sort(currentTasks, selectedComparator.getComparator());
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

    public int calculatePointsForSmartSortForTask(long taskId){
        List<TaskLog> taskLogs = db.taskLogDao().getLogsForTask(taskId);
        int calcPoints = 0;

        if (!taskLogs.isEmpty()){
            int hourNow = this.getHourNow();
            for (TaskLog taskLog : taskLogs){
                calcPoints += taskLog.calculatePointsForSmartSort(hourNow);
            }

            // Adjust based on the spread of logs
            // If logs are clustered together in a small time range that's in hourNow range, this should be rewarded
            int logSpreadReward = calculateLogSpreadReward(taskLogs, hourNow);
            calcPoints += logSpreadReward;

            // Dynamic frequency penalty
            // Use a logarithmic function to penalize tasks with high log counts gently
            if(taskLogs.size() > 5) {
                double logFrequencyPenalty = Math.log(taskLogs.size());
                calcPoints = (int) (calcPoints / logFrequencyPenalty);
            }
        }

        Log.d("SmartSort", calcPoints + " " + taskId);

        return -calcPoints; //lower number goes first in comparator
    }

    // Reward tasks with clusters of logs close to the current time
    private static int calculateLogSpreadReward(List<TaskLog> taskLogs, int currentHour) {
        int reward = 0;

        // Group logs into time buckets (e.g., hourly)
        int[] timeBuckets = new int[24];  // 24 hours in a day

        // Count how many logs fall into each bucket
        for (TaskLog log : taskLogs) {
            timeBuckets[log.hourLoggedAt]++;  // Increment the count for the log's hour
        }

        // Now calculate the reward based on clusters around `currentHour`
        for (int i = 0; i < timeBuckets.length; i++) {
            int distance = Math.abs(i - currentHour);  // Distance from current hour
            int clusterSize = timeBuckets[i];  // How many logs in this hour

            // Reward for clusters centered around `currentHour`
            if (clusterSize > 0) {
                if (distance == 0) {
                    reward += clusterSize * 10;  // Strong reward for exact match (closer clusters)
                } else if (distance == 1) {
                    reward += clusterSize * 5;   // Medium reward for nearby clusters
                } else if (distance <= 3) {
                    reward += clusterSize * 2;   // Small reward for slightly farther clusters
                }
            }
        }

        return reward;
    }

    public LiveData<List<TaskItem>> getTasksItems() {
        return tasksItems;
    }
    public LiveData<String> getText() {
        return mText;
    }
}

