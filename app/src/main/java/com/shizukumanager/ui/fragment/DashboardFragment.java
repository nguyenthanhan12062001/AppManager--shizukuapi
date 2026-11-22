package com.shizukumanager.app.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.shizukumanager.app.R;
import com.shizukumanager.app.ShizukuManagerApplication;
import com.shizukumanager.app.databinding.FragmentDashboardBinding;
import com.shizukumanager.app.di.AppContainer;
import com.shizukumanager.app.model.ShizukuConnectionState;
import com.shizukumanager.app.model.ShizukuStatus;
import com.shizukumanager.app.ui.adapter.RecentChangeAdapter;
import com.shizukumanager.app.viewmodel.DashboardViewModel;

// Man hinh Tong quan hien thi thong ke thuc te, trang thai Shizuku va thay doi gan day
public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private DashboardViewModel viewModel;
    private AppContainer appContainer;
    private RecentChangeAdapter recentAdapter;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        appContainer = ((ShizukuManagerApplication) requireActivity().getApplication()).getAppContainer();
        viewModel = new ViewModelProvider(this, appContainer.getViewModelFactory())
                .get(DashboardViewModel.class);

        recentAdapter = new RecentChangeAdapter();
        binding.rvRecentChanges.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvRecentChanges.setAdapter(recentAdapter);

        setupClickListeners();
        observeViewModel();
    }

    private void setupClickListeners() {
        binding.swipeRefreshDashboard.setOnRefreshListener(() -> viewModel.refreshAll());

        binding.btnHeaderHistory.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_dashboard_to_history));
        binding.btnHeaderSettings.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_dashboard_to_settings));
        binding.tvSeeAllHistory.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_dashboard_to_history));

        binding.btnCheckShizuku.setOnClickListener(v -> viewModel.checkShizukuConnection());

        binding.cardStatTotal.setOnClickListener(v -> navigateToAllApps("ALL"));
        binding.cardStatEnabled.setOnClickListener(v -> navigateToAllApps("ENABLED"));
        binding.cardStatDisabled.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_dashboard_to_disabledApps));
        binding.cardStatSuspended.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_dashboard_to_suspendedApps));

        binding.btnQuickDisabled.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_dashboard_to_disabledApps));
        binding.btnQuickSuspended.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_dashboard_to_suspendedApps));
        binding.btnQuickProfiles.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_dashboard_to_profiles));
        binding.btnQuickHistory.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_dashboard_to_history));
    }

    private void navigateToAllApps(String initialFilter) {
        Bundle args = new Bundle();
        args.putString("initialStateFilter", initialFilter);
        Navigation.findNavController(requireView()).navigate(R.id.action_dashboard_to_allApps, args);
    }

    private void observeViewModel() {
        viewModel.observeLoading().observe(getViewLifecycleOwner(), loading ->
                binding.swipeRefreshDashboard.setRefreshing(Boolean.TRUE.equals(loading)));

        viewModel.observeStats().observe(getViewLifecycleOwner(), stats -> {
            if (stats == null) {
                return;
            }
            binding.tvStatTotal.setText(String.valueOf(stats.getTotalApps()));
            binding.tvStatEnabled.setText(String.valueOf(stats.getEnabledApps()));
            binding.tvStatDisabled.setText(String.valueOf(stats.getDisabledApps()));
            binding.tvStatSuspended.setText(String.valueOf(stats.getSuspendedApps()));
        });

        viewModel.observeShizukuStatus().observe(getViewLifecycleOwner(), this::renderShizukuStatus);

        viewModel.observeRecentChanges().observe(getViewLifecycleOwner(), list -> {
            recentAdapter.submitList(list);
            boolean empty = list == null || list.isEmpty();
            binding.tvNoRecentChanges.setVisibility(empty ? View.VISIBLE : View.GONE);
            binding.rvRecentChanges.setVisibility(empty ? View.GONE : View.VISIBLE);
        });
    }

    private void renderShizukuStatus(ShizukuStatus status) {
        if (status == null) {
            return;
        }
        ShizukuConnectionState state = status.getState();
        if (state == ShizukuConnectionState.CONNECTED) {
            binding.tvShizukuState.setText(R.string.shizuku_connected);
            binding.tvShizukuPill.setText(R.string.shizuku_connected);
            binding.tvShizukuPill.setBackgroundResource(R.drawable.pill_enabled);
            binding.tvShizukuPill.setTextColor(ContextCompat.getColor(requireContext(), R.color.state_enabled_fg));
            binding.tvShizukuDetail.setText(getString(R.string.shizuku_connected_detail, status.getUid(), status.getApiVersion()));
            binding.btnShizukuPrimaryAction.setVisibility(View.GONE);
        } else if (state == ShizukuConnectionState.PERMISSION_REQUIRED) {
            binding.tvShizukuState.setText(R.string.shizuku_permission_required);
            binding.tvShizukuPill.setText(R.string.shizuku_permission_required);
            binding.tvShizukuPill.setBackgroundResource(R.drawable.pill_warning);
            binding.tvShizukuPill.setTextColor(ContextCompat.getColor(requireContext(), R.color.state_warning_fg));
            binding.tvShizukuDetail.setText(R.string.shizuku_permission_required_detail);
            binding.btnShizukuPrimaryAction.setVisibility(View.VISIBLE);
            binding.btnShizukuPrimaryAction.setText(R.string.shizuku_action_grant);
            binding.btnShizukuPrimaryAction.setOnClickListener(v -> viewModel.requestShizukuPermission());
        } else if (state == ShizukuConnectionState.SERVICE_NOT_RUNNING) {
            binding.tvShizukuState.setText(R.string.shizuku_not_running);
            binding.tvShizukuPill.setText(R.string.shizuku_not_running);
            binding.tvShizukuPill.setBackgroundResource(R.drawable.pill_warning);
            binding.tvShizukuPill.setTextColor(ContextCompat.getColor(requireContext(), R.color.state_warning_fg));
            binding.tvShizukuDetail.setText(R.string.shizuku_not_running_detail);
            binding.btnShizukuPrimaryAction.setVisibility(View.VISIBLE);
            binding.btnShizukuPrimaryAction.setText(R.string.shizuku_action_open);
            binding.btnShizukuPrimaryAction.setOnClickListener(v ->
                    appContainer.getShizukuConnectionManager().openShizukuApp(requireContext()));
        } else {
            binding.tvShizukuState.setText(R.string.shizuku_not_installed);
            binding.tvShizukuPill.setText(R.string.shizuku_not_installed);
            binding.tvShizukuPill.setBackgroundResource(R.drawable.pill_disabled);
            binding.tvShizukuPill.setTextColor(ContextCompat.getColor(requireContext(), R.color.state_disabled_fg));
            binding.tvShizukuDetail.setText(R.string.shizuku_not_installed_detail);
            binding.btnShizukuPrimaryAction.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
