package com.example.timetracker.ui.tasks.add_task;

import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.timetracker.R;
import com.example.timetracker.ui.tasks.TasksViewModel;
import com.example.timetracker.ui.tasks.TimeEditText;

public class AddTaskDialogFragment extends DialogFragment {

    private EditText taskNameEditText;
    private TimeEditText taskTimeEditText;
    private TasksViewModel tasksViewModel;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
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
        return inflater.inflate(R.layout.dialog_fragment_add_task, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Bind the EditText and Button views
        taskNameEditText = view.findViewById(R.id.taskNameAddText);
        taskTimeEditText = view.findViewById(R.id.taskTimeAddTask);
        Button saveButton = view.findViewById(R.id.saveButton);
        Button cancelButton = view.findViewById(R.id.cancelButton);

        // Set up button actions
        setUpButtons(saveButton, cancelButton);
    }

    private void setUpButtons(Button saveButton, Button cancelButton) {
        // Set up the Cancel button to dismiss the dialog
        cancelButton.setOnClickListener(v -> dismiss());

        //default
        int updateLeft = 15;
        int updateRight = 60;

        // Set up the Save button to add the task to the ViewModel
        saveButton.setOnClickListener(v -> {
            String taskName = taskNameEditText.getText().toString().trim();
            int time = taskTimeEditText.getTotalMinutes();

            // Validate input before adding task
            if (!taskName.isEmpty() && time >= 0) {
                // Create TaskItem and add it to the ViewModel
                tasksViewModel.addNewTask(taskName, time, tasksViewModel.getSelectedDate().getValue(), updateLeft, updateRight);
                Toast.makeText(getContext(), "Task added!", Toast.LENGTH_SHORT).show();

                // Dismiss the dialog after saving the task
                dismiss();

            } else {
                if (taskName.isEmpty()) {
                    taskNameEditText.setError("Please enter task name.");
                }
                else {
                    Toast.makeText(getContext(), "Please enter valid time", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
