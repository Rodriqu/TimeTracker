package com.example.timetracker.ui.tasks.add_task;

import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.timetracker.R;
import com.example.timetracker.ui.tasks.TasksViewModel;
import com.example.timetracker.TaskItem;

public class AddTaskDialogFragment extends DialogFragment {


    private EditText taskNameEditText;
    private EditText taskTimeEditText;
    private TasksViewModel tasksViewModel;

    public AddTaskDialogFragment() {

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

        return inflater.inflate(R.layout.fragment_add_task, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Bind the EditText and Button views
        taskNameEditText = view.findViewById(R.id.taskNameEditText);
        taskTimeEditText = view.findViewById(R.id.taskTimeEditText);
        Button saveButton = view.findViewById(R.id.saveButton);
        Button cancelButton = view.findViewById(R.id.cancelButton);

        // Set up button actions
        setUpButtons(saveButton, cancelButton);
    }

    private void setUpButtons(Button saveButton, Button cancelButton) {
        // Set up the Cancel button to dismiss the dialog
        cancelButton.setOnClickListener(v -> dismiss());

        // Set up the Save button to add the task to the ViewModel
        saveButton.setOnClickListener(v -> {
            String taskName = taskNameEditText.getText().toString().trim();
            String taskTime = taskTimeEditText.getText().toString().trim();

            // Validate input before adding task
            if (!taskName.isEmpty() && !taskTime.isEmpty()) {
                try {
                    int timeInMinutes = Integer.parseInt(taskTime);

                    // Create TaskItem and add it to the ViewModel
                    TaskItem newTask = new TaskItem(taskName, timeInMinutes);
                    tasksViewModel.addTask(newTask);
                    Toast.makeText(getContext(), "Task added!", Toast.LENGTH_SHORT).show();

                    // Dismiss the dialog after saving the task
                    dismiss();
                } catch (NumberFormatException e) {
                    taskTimeEditText.setError("Please enter a valid time in minutes.");
                }
            } else {
                if (taskName.isEmpty()) {
                    taskNameEditText.setError("Please enter task name.");
                }
                if (taskTime.isEmpty()) {
                    taskTimeEditText.setError("Please enter task time.");
                }
            }
        });
    }
}
