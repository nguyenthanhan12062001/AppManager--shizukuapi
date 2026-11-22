package com.shizukumanager.app.ui.fragment;

import android.view.View;
import com.shizukumanager.app.R;
import com.shizukumanager.app.ui.adapter.AppListAdapter;
import com.shizukumanager.app.viewmodel.AppListViewModel;

// Man hinh tu dong phat hien va hien thi cac ung dung dang bi Disabled tu PackageManager
public class DisabledAppsFragment extends BaseAppListFragment {

    @Override
    protected AppListViewModel.ListMode getListMode() {
        return AppListViewModel.ListMode.DISABLED;
    }

    @Override
    protected AppListAdapter.QuickActionMode getQuickActionMode() {
        return AppListAdapter.QuickActionMode.ENABLE;
    }

    @Override
    protected void configureHeader() {
        binding.tvListTitle.setText(R.string.disabled_apps_title);
        binding.tvListSubtitle.setVisibility(View.VISIBLE);
        binding.tvListSubtitle.setText(R.string.disabled_apps_subtitle);
        binding.scrollFilters.setVisibility(View.GONE);
        binding.tvEmptyAppsMessage.setText(R.string.empty_disabled_apps);

        // Hien thi ro nut Enable hang loat cho man hinh Disabled Apps
        binding.btnBulkDisable.setVisibility(View.GONE);
    }
}
