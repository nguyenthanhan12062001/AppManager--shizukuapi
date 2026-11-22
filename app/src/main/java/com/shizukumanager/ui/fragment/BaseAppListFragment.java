package com.shizukumanager.app.ui.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.shizukumanager.app.R;
import com.shizukumanager.app.ShizukuManagerApplication;
import com.shizukumanager.app.databinding.FragmentAppListBinding;
import com.shizukumanager.app.di.AppContainer;
import com.shizukumanager.app.model.AppFilterOptions;
import com.shizukumanager.app.model.AppInfoModel;
import com.shizukumanager.app.model.OperationType;
import com.shizukumanager.app.ui.adapter.AppListAdapter;
import com.shizukumanager.app.ui.dialog.OperationDialogHelper;
import com.shizukumanager.app.util.LocalizedLabelMapper;
import com.shizukumanager.app.viewmodel.AppListViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import java.util.List;

// Lop co so dung chung cho man hinh All Apps, Disabled Apps va Suspended Apps
public abstract class BaseAppListFragment extends Fragment {

    protected FragmentAppListBinding binding;
    protected AppListViewModel viewModel;
    protected AppContainer appContainer;
    protected AppListAdapter adapter;
    private OperationDialogHelper.ProgressDialogController progressDialog;

    protected abstract AppListViewModel.ListMode getListMode();
    protected abstract AppListAdapter.QuickActionMode getQuickActionMode();
    protected abstract void configureHeader();

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        binding = FragmentAppListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        appContainer = ((ShizukuManagerApplication) requireActivity().getApplication()).getAppContainer();
        viewModel = new ViewModelProvider(this, appContainer.getViewModelFactory())
                .get(AppListViewModel.class);
        viewModel.initMode(getListMode());

        progressDialog = OperationDialogHelper.createProgressController(requireContext());

        adapter = new AppListAdapter(
                appContainer.getAppIconLoader(),
                getQuickActionMode(),
                new AppListAdapter.OnAppRowListener() {
                    @Override
                    public void onAppClicked(AppInfoModel app) {
                        Bundle args = new Bundle();
                        args.putString("packageName", app.getPackageName());
                        Navigation.findNavController(requireView()).navigate(R.id.action_to_appDetails, args);
                    }

                    @Override
                    public void onAppSelectionToggled(AppInfoModel app) {
                        viewModel.toggleSelection(app.getPackageName());
                    }

                    @Override
                    public void onQuickActionClicked(AppInfoModel app) {
                        handleQuickAction(app);
                    }
                }
        );

        binding.rvApps.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvApps.setAdapter(adapter);

        configureHeader();
        setupSearchAndSort();
        setupBulkButtons();
        observeViewModel();
    }

    private void setupSearchAndSort() {
        binding.swipeRefreshApps.setOnRefreshListener(() -> viewModel.refreshApps());
        binding.btnRefreshApps.setOnClickListener(v -> viewModel.refreshApps());

        binding.etSearchApps.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = s == null ? "" : s.toString();
                viewModel.setSearchQuery(text);
                binding.btnClearSearch.setVisibility(text.isEmpty() ? View.GONE : View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        binding.btnClearSearch.setOnClickListener(v -> binding.etSearchApps.setText(""));

        binding.btnSortApps.setOnClickListener(v -> {
            PopupMenu menu = new PopupMenu(requireContext(), v);
            menu.getMenu().add(0, 1, 0, R.string.sort_name_asc);
            menu.getMenu().add(0, 2, 1, R.string.sort_name_desc);
            menu.getMenu().add(0, 3, 2, R.string.sort_updated_desc);
            menu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == 1) {
                    viewModel.setSortOrder(AppFilterOptions.SortOrder.NAME_ASC);
                } else if (item.getItemId() == 2) {
                    viewModel.setSortOrder(AppFilterOptions.SortOrder.NAME_DESC);
                } else if (item.getItemId() == 3) {
                    viewModel.setSortOrder(AppFilterOptions.SortOrder.UPDATED_DESC);
                }
                return true;
            });
            menu.show();
        });
    }

    private void setupBulkButtons() {
        binding.btnSelectAll.setOnClickListener(v -> viewModel.selectAllVisible());
        binding.btnClearSelection.setOnClickListener(v -> viewModel.clearSelection());

        binding.btnBulkEnable.setOnClickListener(v -> confirmBulkOperation(OperationType.ENABLE));
        binding.btnBulkDisable.setOnClickListener(v -> confirmBulkOperation(OperationType.DISABLE));
        binding.btnBulkFreeze.setOnClickListener(v -> confirmBulkOperation(OperationType.FREEZE));
        binding.btnBulkUnfreeze.setOnClickListener(v -> confirmBulkOperation(OperationType.UNFREEZE));
        binding.btnBulkForceStop.setOnClickListener(v -> confirmBulkOperation(OperationType.FORCE_STOP));
    }

    private void handleQuickAction(AppInfoModel app) {
        OperationType type = getQuickActionMode() == AppListAdapter.QuickActionMode.UNSUSPEND
                ? OperationType.UNFREEZE
                : OperationType.ENABLE;
        if (appContainer.getPreferencesManager().isConfirmSingleAction()) {
            String opLabel = LocalizedLabelMapper.getOperationLabel(requireContext(), type);
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(getString(R.string.confirm_action_title, opLabel))
                    .setMessage(getString(R.string.confirm_action_message, opLabel, app.getAppName(), app.getPackageName()))
                    .setNegativeButton(R.string.action_cancel, null)
                    .setPositiveButton(R.string.action_confirm, (d, w) -> viewModel.executeSingleAction(app, type))
                    .show();
        } else {
            viewModel.executeSingleAction(app, type);
        }
    }

    private void confirmBulkOperation(OperationType type) {
        List<AppInfoModel> selected = viewModel.getSelectedAppModels();
        if (selected.isEmpty()) {
            return;
        }
        String opLabel = LocalizedLabelMapper.getOperationLabel(requireContext(), type);
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.confirm_bulk_title, opLabel))
                .setMessage(getString(R.string.confirm_bulk_message, opLabel, selected.size()))
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_confirm, (d, w) -> viewModel.executeBulkAction(type))
                .show();
    }

    private void observeViewModel() {
        viewModel.observeLoading().observe(getViewLifecycleOwner(), loading ->
                binding.swipeRefreshApps.setRefreshing(Boolean.TRUE.equals(loading)));

        viewModel.observeFilteredApps().observe(getViewLifecycleOwner(), apps -> {
            adapter.submitList(apps);
            boolean empty = apps == null || apps.isEmpty();
            binding.emptyAppsContainer.setVisibility(empty ? View.VISIBLE : View.GONE);
            binding.rvApps.setVisibility(empty ? View.GONE : View.VISIBLE);
        });

        viewModel.observeSelectedPackages().observe(getViewLifecycleOwner(), selected -> {
            adapter.setSelectedPackages(selected);
            int count = selected == null ? 0 : selected.size();
            if (count > 0) {
                binding.panelBulkActions.setVisibility(View.VISIBLE);
                binding.tvSelectedCount.setText(getString(R.string.selected_count_format, count));
            } else {
                binding.panelBulkActions.setVisibility(View.GONE);
            }
        });

        viewModel.observeBulkProgress().observe(getViewLifecycleOwner(), state -> {
            if (state == null) {
                return;
            }
            if (state.active) {
                if (state.completed == 0) {
                    progressDialog.show(state.operationType);
                } else {
                    progressDialog.update(state.completed, state.total, state.currentAppName);
                }
            } else {
                progressDialog.dismiss();
            }
        });

        viewModel.observeBulkResultEvent().observe(getViewLifecycleOwner(), summary -> {
            if (summary != null) {
                viewModel.consumeBulkResultEvent();
                OperationDialogHelper.showBulkSummaryDialog(requireContext(), summary);
            }
        });

        viewModel.observeSingleResultEvent().observe(getViewLifecycleOwner(), result -> {
            if (result != null) {
                viewModel.consumeSingleResultEvent();
                String opLabel = LocalizedLabelMapper.getOperationLabel(requireContext(), result.getOperationType());
                String msg = result.isSuccess()
                        ? getString(R.string.result_success_single, opLabel, result.getAppName())
                        : getString(R.string.result_failed_single, opLabel, result.getAppName(),
                                LocalizedLabelMapper.getErrorText(requireContext(), result.getErrorType(), result.getRawErrorDetail()));
                Snackbar.make(binding.getRoot(), msg, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
        binding = null;
    }
}
