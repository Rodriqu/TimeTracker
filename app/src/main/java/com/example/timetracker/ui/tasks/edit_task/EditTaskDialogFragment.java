package com.example.timetracker.ui.tasks.edit_task;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.timetracker.R;
import com.example.timetracker.TaskItem;
import com.example.timetracker.ui.tasks.TasksViewModel;
import com.example.timetracker.ui.tasks.TimeEditText;

public class EditTaskDialogFragment extends DialogFragment {

    private final TaskItem taskItem;
    private TasksViewModel tasksViewModel;
    private EditText taskName;
    private TimeEditText taskTime;
    private TimeEditText updateLeftText;
    private TimeEditText updateRightText;

    public EditTaskDialogFragment(TaskItem task) {
        taskItem = task;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        tasksViewModel = new ViewModelProvider(requireActivity()).get(TasksViewModel.class);
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null)
        {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setLayout(width, height);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_fragment_edit_task, container, false);
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        taskName = view.findViewById(R.id.taskNameEditText);
        taskName.setText(taskItem.getName());

        taskTime = view.findViewById(R.id.taskTimeEditTask);
        taskTime.setTextInMinutes(taskItem.getTime());

        updateLeftText = view.findViewById(R.id.leftEditTask);
        updateRightText = view.findViewById(R.id.rightEditTask);

        updateLeftText.setTextInMinutes(taskItem.getUpdateLeft());
        updateRightText.setTextInMinutes(taskItem.getUpdateRight());

        Button saveButton = view.findViewById(R.id.saveButtonEditTask);
        Button cancelButton = view.findViewById(R.id.cancelButtonEditTask);

        setUpButtons(saveButton, cancelButton);
    }

    private void setUpButtons(Button saveButton, Button cancelButton) {
        // Set up the Cancel button to dismiss the dialog
        cancelButton.setOnClickListener(v -> dismiss());

        // Set up the Save button to log the task to the ViewModel
        saveButton.setOnClickListener(v -> {
            String taskNameText = taskName.getText().toString().trim();
            int time = taskTime.getTotalMinutes();
            int updateLeft = updateLeftText.getTotalMinutes();
            int updateRight = updateRightText.getTotalMinutes();

            // Validate input before adding task
            if (!taskNameText.isEmpty() && time >= 0 && updateLeft >= 0 && updateRight >= 0) {
                TaskItem editedTaskItem = new TaskItem(taskItem.getId(), taskNameText, time, updateLeft, updateRight);

                tasksViewModel.editTaskDetails(editedTaskItem, tasksViewModel.getSelectedDate().getValue());

                dismiss();
            } else {
                if (taskNameText.isEmpty()) {
                    taskName.setError("Please enter task name.");
                }
                else {
                    Toast.makeText(getContext(), "Please enter valid time", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
