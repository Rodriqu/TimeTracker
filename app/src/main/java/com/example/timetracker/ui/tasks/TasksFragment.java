package com.example.timetracker.ui.tasks;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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
import com.example.timetracker.ui.tasks.edit_task.EditTaskDialogFragment;
import com.example.timetracker.ui.tasks.log_task.LogTaskDialogFragment;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class TasksFragment extends Fragment {

    private FragmentTasksBinding binding;

    private TasksViewModel tasksViewModel;

    private Toast currentToastUpdate;

    private Toast currentToastSort;

    private TextView selectedDateTextView;

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
            } else {
                // Handle empty list case
                Toast.makeText(getContext(), "No tasks available", Toast.LENGTH_SHORT).show();
            }
        });

        adapter.setOnItemClickListener(position -> {
            String dialogTag = "LogTaskDialog";
            if(requireActivity().getSupportFragmentManager().findFragmentByTag(dialogTag) == null) {
                List<TaskItem> tasksList = tasksViewModel.getTasksItems().getValue();
                if (tasksList != null && position >= 0 && position < tasksList.size()) {
                    LogTaskDialogFragment logTaskDialogFragment = new LogTaskDialogFragment(tasksViewModel.getTasksItems().getValue().get(position));
                    logTaskDialogFragment.show(requireActivity().getSupportFragmentManager(), dialogTag);
                }
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
            if (currentToastUpdate != null){
                currentToastUpdate.cancel();
            }
            currentToastUpdate = Toast.makeText(getContext(), "Task time updated!", Toast.LENGTH_SHORT);
            currentToastUpdate.show();
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
        selectedDateTextView = requireActivity().findViewById(R.id.date_toolbar_text);
        LinearLayout currentDateBtn = requireActivity().findViewById(R.id.date_toolbar_btn);

        buttonPreviousDate.setOnClickListener(v -> changeDate(-1));
        buttonNextDate.setOnClickListener(v -> changeDate(1));

        // Observe the selected date
        // Update UI
        tasksViewModel.getSelectedDate().observe(getViewLifecycleOwner(), this::updateDateDisplay);

        requireActivity().findViewById(R.id.fab_sort).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String sortBy = tasksViewModel.setNextSortOrder();
                if (currentToastSort != null){
                    currentToastSort.cancel();
                }
                currentToastSort = Toast.makeText(getContext(), sortBy, Toast.LENGTH_SHORT);
                currentToastSort.show();
            }
        });

        selectedDateTextView.setOnClickListener(v -> {
            int year = tasksViewModel.getSelectedDate().getValue().getYear();
            int month = tasksViewModel.getSelectedDate().getValue().getMonthValue() - 1; // Month is 0-based in DatePickerDialog
            int day = tasksViewModel.getSelectedDate().getValue().getDayOfMonth();

            // Show the DatePickerDialog
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    requireActivity(),
                    (datePicker, selectedYear, selectedMonth, selectedDay) -> {
                        // Create a new LocalDate from the selected values
                        LocalDate selectedDate = LocalDate.of(selectedYear, selectedMonth + 1, selectedDay);
                        // Update the TextView with the formatted date
                        tasksViewModel.setSelectedDate(selectedDate);
                    },
                    year, month, day
            );
            datePickerDialog.show();
        });
    }

    private void updateDateDisplay(LocalDate date) {
        if (date.equals(LocalDate.now().minusDays(1))){
            selectedDateTextView.setText("YESTERDAY");
        }
        else if (date.equals(LocalDate.now())) {
            selectedDateTextView.setText("TODAY");
        }
        else if (date.equals(LocalDate.now().plusDays(1))) {
            selectedDateTextView.setText("TOMORROW");
        }
        else{
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            selectedDateTextView.setText(date.format(formatter));
        }
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
            new Thread(() -> tasksViewModel.sortTasksAsync()).start();
        }
    }
}