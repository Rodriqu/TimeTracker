package com.example.timetracker.ui.tasks.log_task;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.timetracker.R;
import com.example.timetracker.TaskItem;
import com.example.timetracker.ui.tasks.TasksViewModel;

public class LogTaskDialogFragment extends DialogFragment {

    private final TaskItem taskItem;
    private TasksViewModel tasksViewModel;

    private TimePicker timePicker;


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
    public void onStart()
    {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null)
        {
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

        // Set up button actions
        setUpButtons(saveButton, cancelButton);
    }

    private void setUpButtons(Button saveButton, Button cancelButton) {
        // Set up the Cancel button to dismiss the dialog
        cancelButton.setOnClickListener(v -> dismiss());

        // Set up the Save button to log the task to the ViewModel
        saveButton.setOnClickListener(v -> {
            int hour = timePicker.getHour();
            int minutes = timePicker.getMinute();
            int time = minutes + 60*hour;

            //date przekazywane na przyszlosc gdyby mozna bylo zmieniac datę
            tasksViewModel.addTimeToTask(taskItem, time, tasksViewModel.getSelectedDate().getValue());

            dismiss();
        });
    }
}
