package com.shizukumanager.app.ui.fragment;

import android.view.View;
import com.shizukumanager.app.R;
import com.shizukumanager.app.ui.adapter.AppListAdapter;
import com.shizukumanager.app.viewmodel.AppListViewModel;

// Man hinh tu dong phat hien va hien thi cac ung dung dang bi Suspend / Dong bang
public class SuspendedAppsFragment extends BaseAppListFragment {

    @Override
    protected AppListViewModel.ListMode getListMode() {
        return AppListViewModel.ListMode.SUSPENDED;
    }

    @Override
    protected AppListAdapter.QuickActionMode getQuickActionMode() {
        return AppListAdapter.QuickActionMode.UNSUSPEND;
    }

    @Override
    protected void configureHeader() {
        binding.tvListTitle.setText(R.string.suspended_apps_title);
        binding.tvListSubtitle.setVisibility(View.VISIBLE);
        binding.tvListSubtitle.setText(R.string.suspended_apps_subtitle);
        binding.scrollFilters.setVisibility(View.GONE);
        binding.tvEmptyAppsMessage.setText(R.string.empty_suspended_apps);

        binding.btnBulkFreeze.setVisibility(View.GONE);
    }
}
