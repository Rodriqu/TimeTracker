package com.example.timetracker.ui.tasks.edit_task;

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
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.timetracker.R;
import com.example.timetracker.TaskItem;
import com.example.timetracker.ui.tasks.TasksViewModel;

public class EditTaskDialogFragment extends DialogFragment {

    private final TaskItem taskItem;
    private TasksViewModel tasksViewModel;

    private TextView taskName;

    private TextView taskTime;

    public EditTaskDialogFragment(TaskItem task) {
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

        return inflater.inflate(R.layout.dialog_fragment_edit_task, container, false);
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        taskName = view.findViewById(R.id.taskNameEditText);

        taskName.setText(taskItem.getName());

        taskTime = view.findViewById(R.id.taskTimeEditTask);


        taskTime.setText(Integer.toString(taskItem.getTime()));


        Button saveButton = view.findViewById(R.id.saveButtonEditTask);
        Button cancelButton = view.findViewById(R.id.cancelButtonEditTask);

        // Set up button actions
        setUpButtons(saveButton, cancelButton);
    }

    private void setUpButtons(Button saveButton, Button cancelButton) {
        // Set up the Cancel button to dismiss the dialog
        cancelButton.setOnClickListener(v -> dismiss());

        // Set up the Save button to log the task to the ViewModel
        saveButton.setOnClickListener(v -> {
            String taskNameText = taskName.getText().toString().trim();
            String taskTimeText = taskTime.getText().toString().trim();

            // Validate input before adding task
            if (!taskNameText.isEmpty() && !taskTimeText.isEmpty()) {
                try {
                    int timeInMinutes = Integer.parseInt(taskTimeText);
                    TaskItem editedTaskItem = new TaskItem(taskItem.getId(), taskNameText, timeInMinutes);

                    //data przekazywana na przyszlosc jesli mozna bedzie zmieniac date
                    tasksViewModel.editTaskDetails(editedTaskItem, tasksViewModel.getSelectedDate().getValue());

                    dismiss();
                } catch (NumberFormatException e) {
                    taskTime.setError("Please enter a valid time in minutes.");
                }
            } else {
                if (taskNameText.isEmpty()) {
                    taskName.setError("Please enter task name.");
                }
                if (taskTimeText.isEmpty()) {
                    taskTime.setError("Please enter task time.");
                }
            }
        });
    }
}
