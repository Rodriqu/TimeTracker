package com.example.timetracker.ui.tasks.log_task;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;


import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.MutableLiveData;

import com.example.timetracker.AppDatabaseInstance;
import com.example.timetracker.R;
import com.example.timetracker.TaskItem;
import com.example.timetracker.TaskLog;
import com.example.timetracker.ui.tasks.TasksViewModel;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LogTaskDialogFragment extends DialogFragment {

    private final TaskItem taskItem;
    private TasksViewModel tasksViewModel;

    private TimePicker timePicker;

    private MutableLiveData<String> smartTextLiveData = new MutableLiveData<>();
    private int smartTime;

    public LogTaskDialogFragment(TaskItem task) {
        taskItem = task;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Initialize the ViewModel when the fragment is attached
        tasksViewModel = new ViewModelProvider(requireActivity()).get(TasksViewModel.class);
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for the dialog fragment

        return inflater.inflate(R.layout.dialog_fragment_log_task, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        timePicker = view.findViewById(R.id.time_picker_log_task);

        timePicker.setIs24HourView(true);
        timePicker.setHour(0);
        timePicker.setMinute(0);

        TextView taskName = view.findViewById(R.id.TaskNameLogTask);
        taskName.setText(taskItem.getName());
        TextView title = view.findViewById(R.id.NameTextLogTask);
        title.setText("Add Time on " + tasksViewModel.dateToStringForDB());

        Button saveButton = view.findViewById(R.id.saveButtonLogTask);
        Button cancelButton = view.findViewById(R.id.cancelButtonLogTask);

        TextView smartTimeTextView = view.findViewById(R.id.smart_time_log_task);
        smartTextLiveData.observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                smartTimeTextView.setText(s);
            }
        });
        this.setSmartTime(15);

        // Set up button actions
        setUpButtons(saveButton, cancelButton);
        this.calculateAndUpdateMostFrequentLoggedTime(taskItem.getId());
    }

    private void setUpButtons(Button saveButton, Button cancelButton) {
        // Set up the Cancel button to dismiss the dialog
        cancelButton.setOnClickListener(v -> dismiss());

        // Set up the Save button to log the task to the ViewModel
        saveButton.setOnClickListener(v -> {
            int hour = timePicker.getHour();
            int minutes = timePicker.getMinute();
            int time = minutes + 60 * hour;

            //date przekazywane na przyszlosc gdyby mozna bylo zmieniac datę
            tasksViewModel.addTimeToTask(taskItem, time, tasksViewModel.getSelectedDate().getValue());

            dismiss();
        });
    }

    private void calculateAndUpdateMostFrequentLoggedTime(long taskId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<TaskLog> taskLogs = AppDatabaseInstance.getInstance().taskLogDao().getLogsForTask(taskId);

                if (taskLogs.isEmpty()) return;

                // Step 1: Count the frequency of each hourLoggedAt value
                Map<Integer, Integer> frequencyMap = new HashMap<>();

                for (TaskLog taskLog : taskLogs) {
                    // Round the hourLoggedAt to the nearest multiple of 5
                    if (taskLog.timeLogged != 0) {
                        int roundedTime = (taskLog.timeLogged / 5) * 5;
                        frequencyMap.put(roundedTime, frequencyMap.getOrDefault(roundedTime, 0) + 1);
                    }
                }

                if (frequencyMap.isEmpty()) return;

                // Step 2: Find the most frequent rounded time
                int mostFrequentTime = -1;
                int highestFrequency = 0;

                for (Map.Entry<Integer, Integer> entry : frequencyMap.entrySet()) {
                    if (entry.getValue() > highestFrequency && entry.getKey() != 0) {
                        mostFrequentTime = entry.getKey();
                        highestFrequency = entry.getValue();
                    }
                }

                if (mostFrequentTime <= 0) return;

                setSmartTime(mostFrequentTime);
            }
        }).start();
    }

    private void setSmartTime(int time){
        smartTime = time;
        String newSmartText;

        int hours = time / 60;
        int minutes = time % 60;

        if (hours == 0) {
            newSmartText = minutes + "m";
        }
        else if (minutes == 0) {
            newSmartText = hours + "h";
        }
        else if (minutes < 10) {
            newSmartText = hours + "h 0" + minutes + "m";
        }
        else{
            newSmartText = hours + "h " + minutes + "m";
        }
        setSmartText(newSmartText);
    }

    public void setSmartText(String newText) {
        smartTextLiveData.postValue(newText);
    }

}
