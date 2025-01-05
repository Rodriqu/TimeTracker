package com.example.timetracker.ui.tasks;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timetracker.R;
import com.example.timetracker.TaskItem;
import com.example.timetracker.databinding.FragmentTasksBinding;

import java.util.ArrayList;
import java.util.List;

public class TasksFragment extends Fragment {

    private FragmentTasksBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentTasksBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicjalizacja ViewModel
        TasksViewModel tasksViewModel = new ViewModelProvider(this).get(TasksViewModel.class);

        final TextView textView = binding.textTasks;
        tasksViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);


        // Initialize RecyclerView
        TaskAdapter adapter = new TaskAdapter();
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(adapter);

        // Obserwowanie danych
        tasksViewModel.getTasks().observe(getViewLifecycleOwner(), taskItems -> {
            // Check if taskItems is not null before setting to the adapter
            if (taskItems != null && !taskItems.isEmpty()) {
                adapter.updateTasks(taskItems);
            } else {
                // Handle empty list case
                Toast.makeText(getContext(), "No tasks available", Toast.LENGTH_SHORT).show();
            }
        });

        adapter.setOnItemClickListener(position -> {
            List<TaskItem> tasksList = tasksViewModel.getTasks().getValue();
            if (tasksList != null && position >= 0 && position < tasksList.size()) {
                TaskItem clickedTask = tasksList.get(position);
                Toast.makeText(getContext(), "Clicked: " + clickedTask.getName() + " Position: " + position, Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}