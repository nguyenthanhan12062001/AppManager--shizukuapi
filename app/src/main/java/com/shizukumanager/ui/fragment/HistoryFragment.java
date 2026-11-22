package com.shizukumanager.app.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.shizukumanager.app.R;
import com.shizukumanager.app.ShizukuManagerApplication;
import com.shizukumanager.app.databinding.FragmentHistoryBinding;
import com.shizukumanager.app.di.AppContainer;
import com.shizukumanager.app.ui.adapter.HistoryAdapter;
import com.shizukumanager.app.viewmodel.HistoryViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

// Man hinh Lich su thao tac kem bo loc thanh cong / that bai va xoa lich su
public class HistoryFragment extends Fragment {

    private FragmentHistoryBinding binding;
    private HistoryViewModel viewModel;
    private HistoryAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        binding = FragmentHistoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        AppContainer container = ((ShizukuManagerApplication) requireActivity().getApplication()).getAppContainer();
        viewModel = new ViewModelProvider(this, container.getViewModelFactory())
                .get(HistoryViewModel.class);

        adapter = new HistoryAdapter();
        binding.rvHistory.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvHistory.setAdapter(adapter);

        binding.btnBackHistory.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        binding.btnClearHistory.setOnClickListener(v ->
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.history_clear)
                        .setMessage(R.string.history_clear_confirm)
                        .setNegativeButton(R.string.action_cancel, null)
                        .setPositiveButton(R.string.action_confirm, (d, w) -> {
                            viewModel.clearAllHistory();
                            Snackbar.make(binding.getRoot(), R.string.history_cleared, Snackbar.LENGTH_SHORT).show();
                        })
                        .show());

        binding.chipGroupHistoryFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                return;
            }
            int id = checkedIds.get(0);
            if (id == R.id.chipHistorySuccess) {
                viewModel.setFilter(HistoryViewModel.HistoryFilter.SUCCESS_ONLY);
            } else if (id == R.id.chipHistoryFailed) {
                viewModel.setFilter(HistoryViewModel.HistoryFilter.FAILED_ONLY);
            } else {
                viewModel.setFilter(HistoryViewModel.HistoryFilter.ALL);
            }
        });

        viewModel.observeFilteredHistory().observe(getViewLifecycleOwner(), list -> {
            adapter.submitList(list);
            boolean empty = list == null || list.isEmpty();
            binding.emptyHistoryContainer.setVisibility(empty ? View.VISIBLE : View.GONE);
            binding.rvHistory.setVisibility(empty ? View.GONE : View.VISIBLE);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
