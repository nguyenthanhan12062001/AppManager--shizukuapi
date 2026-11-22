package com.shizukumanager.app.ui.fragment;

import android.os.Bundle;
import android.view.View;
import com.shizukumanager.app.R;
import com.shizukumanager.app.model.AppFilterOptions;
import com.shizukumanager.app.ui.adapter.AppListAdapter;
import com.shizukumanager.app.viewmodel.AppListViewModel;

// Man hinh hien thi toan bo ung dung da cai dat voi bo loc System/User va trang thai
public class AllAppsFragment extends BaseAppListFragment {

    @Override
    protected AppListViewModel.ListMode getListMode() {
        return AppListViewModel.ListMode.ALL;
    }

    @Override
    protected AppListAdapter.QuickActionMode getQuickActionMode() {
        return AppListAdapter.QuickActionMode.NONE;
    }

    @Override
    protected void configureHeader() {
        binding.tvListTitle.setText(R.string.all_apps_title);
        binding.tvListSubtitle.setVisibility(View.GONE);
        binding.scrollFilters.setVisibility(View.VISIBLE);
        binding.tvEmptyAppsMessage.setText(R.string.empty_all_apps);

        Bundle args = getArguments();
        if (args != null && args.containsKey("initialStateFilter")) {
            try {
                AppFilterOptions.StateFilter initial = AppFilterOptions.StateFilter.valueOf(
                        args.getString("initialStateFilter", "ALL")
                );
                viewModel.setInitialStateFilter(initial);
                args.remove("initialStateFilter");
            } catch (IllegalArgumentException ignored) {
            }
        }

        syncFilterChipChecks();

        binding.chipGroupType.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                return;
            }
            int id = checkedIds.get(0);
            if (id == R.id.chipTypeUser) {
                viewModel.setTypeFilter(AppFilterOptions.TypeFilter.USER_ONLY);
            } else if (id == R.id.chipTypeSystem) {
                viewModel.setTypeFilter(AppFilterOptions.TypeFilter.SYSTEM_ONLY);
            } else {
                viewModel.setTypeFilter(AppFilterOptions.TypeFilter.ALL);
            }
        });

        binding.chipGroupState.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                return;
            }
            int id = checkedIds.get(0);
            if (id == R.id.chipStateEnabled) {
                viewModel.setStateFilter(AppFilterOptions.StateFilter.ENABLED);
            } else if (id == R.id.chipStateDisabled) {
                viewModel.setStateFilter(AppFilterOptions.StateFilter.DISABLED);
            } else if (id == R.id.chipStateSuspended) {
                viewModel.setStateFilter(AppFilterOptions.StateFilter.SUSPENDED);
            } else {
                viewModel.setStateFilter(AppFilterOptions.StateFilter.ALL);
            }
        });
    }

    private void syncFilterChipChecks() {
        AppFilterOptions.TypeFilter type = viewModel.getTypeFilter();
        if (type == AppFilterOptions.TypeFilter.USER_ONLY) {
            binding.chipTypeUser.setChecked(true);
        } else if (type == AppFilterOptions.TypeFilter.SYSTEM_ONLY) {
            binding.chipTypeSystem.setChecked(true);
        } else {
            binding.chipTypeAll.setChecked(true);
        }

        AppFilterOptions.StateFilter state = viewModel.getStateFilter();
        if (state == AppFilterOptions.StateFilter.ENABLED) {
            binding.chipStateEnabled.setChecked(true);
        } else if (state == AppFilterOptions.StateFilter.DISABLED) {
            binding.chipStateDisabled.setChecked(true);
        } else if (state == AppFilterOptions.StateFilter.SUSPENDED) {
            binding.chipStateSuspended.setChecked(true);
        } else {
            binding.chipStateAll.setChecked(true);
        }
    }
}
