package com.example.timetracker.ui.tasks;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timetracker.R;
import com.example.timetracker.TaskItem;
import com.example.timetracker.databinding.FragmentTasksBinding;
import com.example.timetracker.ui.tasks.add_task.AddTaskDialogFragment;
import com.example.timetracker.ui.tasks.edit_task.EditTaskDialogFragment;
import com.example.timetracker.ui.tasks.log_task.LogTaskDialogFragment;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class TasksFragment extends Fragment {

    private FragmentTasksBinding binding;

    private TasksViewModel tasksViewModel;

    private TextView currentDateTextView;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentTasksBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicjalizacja ViewModel
        tasksViewModel = new ViewModelProvider(requireActivity()).get(TasksViewModel.class);

        final TextView textView = binding.textTasks;
        tasksViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);


        // Initialize RecyclerView
        TaskAdapter adapter = new TaskAdapter();
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(adapter);

        // Obserwowanie danych
        tasksViewModel.getTasksItems().observe(getViewLifecycleOwner(), taskItems -> {
            // Check if taskItems is not null before setting to the adapter

            if (taskItems != null && !taskItems.isEmpty()) {
                adapter.updateTasks(taskItems);
                Log.d("TasksFragment", "LiveData updated. Task count: " + taskItems.size());
                Log.i("TAG", "This is an info message");

            } else {
                // Handle empty list case
                Toast.makeText(getContext(), "No tasks available", Toast.LENGTH_SHORT).show();
            }
        });

        adapter.setOnItemClickListener(position -> {
            List<TaskItem> tasksList = tasksViewModel.getTasksItems().getValue();
            if (tasksList != null && position >= 0 && position < tasksList.size()) {
                LogTaskDialogFragment logTaskDialogFragment = new LogTaskDialogFragment(tasksViewModel.getTasksItems().getValue().get(position));
                logTaskDialogFragment.show(requireActivity().getSupportFragmentManager(), "LogTaskDialog");
            }
        });

        adapter.setOnItemLongClickListener(position -> {
            List<TaskItem> tasksList = tasksViewModel.getTasksItems().getValue();
            if (tasksList != null && position >= 0 && position < tasksList.size()) {
                EditTaskDialogFragment editTaskDialogFragment = new EditTaskDialogFragment(tasksViewModel.getTasksItems().getValue().get(position));
                editTaskDialogFragment.show(requireActivity().getSupportFragmentManager(), "EditTaskDialog");
            }
        });

        adapter.setOnSwipeListener((position, direction) -> {
            tasksViewModel.updateTaskTime(position, direction);
            Toast.makeText(getContext(), "Task time updated!", Toast.LENGTH_SHORT).show();
        });

        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(0,  ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;  // No move functionality needed
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // Get the swiped position and trigger the swipe listener
                int position = viewHolder.getAdapterPosition();
                if (direction == ItemTouchHelper.RIGHT) {
                    adapter.onSwipe(position, 1);  // Trigger swipe action in Adapter
                } else if(direction == ItemTouchHelper.LEFT){
                    adapter.onSwipe(position, -1);
                }
            }
        };



        // Attach swipe gesture functionality to the RecyclerView
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchHelperCallback);
        itemTouchHelper.attachToRecyclerView(binding.recyclerView);


        ImageButton buttonPreviousDate = requireActivity().findViewById(R.id.previous_date_btn);
        ImageButton buttonNextDate = requireActivity().findViewById(R.id.next_date_btn);
        currentDateTextView = requireActivity().findViewById(R.id.date_toolbar_text);
        LinearLayout currentDateBtn = requireActivity().findViewById(R.id.date_toolbar_btn);

        buttonPreviousDate.setOnClickListener(v -> changeDate(-1));
        buttonNextDate.setOnClickListener(v -> changeDate(1));

        // Observe the selected date
        // Update UI
        tasksViewModel.getSelectedDate().observe(getViewLifecycleOwner(), this::updateDateDisplay);
    }

    private void updateDateDisplay(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        currentDateTextView.setText(date.format(formatter));
    }

    public void changeDate(int numberOfDays){
        LocalDate currentDate = tasksViewModel.getSelectedDate().getValue();
        if (currentDate != null) {
            tasksViewModel.setSelectedDate(currentDate.plusDays(numberOfDays));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onResume(){
        super.onResume();

        if(tasksViewModel != null){
            tasksViewModel.sortTasksAsync();
        }
    }
}