package com.shizukumanager.app.ui.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.shizukumanager.app.R;
import com.shizukumanager.app.ShizukuManagerApplication;
import com.shizukumanager.app.databinding.FragmentAppDetailsBinding;
import com.shizukumanager.app.databinding.ItemMetadataRowBinding;
import com.shizukumanager.app.di.AppContainer;
import com.shizukumanager.app.model.AppInfoModel;
import com.shizukumanager.app.model.AppState;
import com.shizukumanager.app.model.OperationType;
import com.shizukumanager.app.util.DateTimeFormatterUtil;
import com.shizukumanager.app.util.LocalizedLabelMapper;
import com.shizukumanager.app.viewmodel.AppDetailsViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

// Man hinh Chi tiet ung dung hien thi metadata day du va cac nut thao tac theo trang thai
public class AppDetailsFragment extends Fragment {

    private FragmentAppDetailsBinding binding;
    private AppDetailsViewModel viewModel;
    private AppContainer appContainer;
    private String packageName;
    private AppInfoModel currentApp;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        binding = FragmentAppDetailsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        appContainer = ((ShizukuManagerApplication) requireActivity().getApplication()).getAppContainer();
        viewModel = new ViewModelProvider(this, appContainer.getViewModelFactory())
                .get(AppDetailsViewModel.class);

        packageName = getArguments() != null ? getArguments().getString("packageName", "") : "";

        binding.btnBackDetails.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        binding.btnRefreshDetails.setOnClickListener(v -> viewModel.loadPackage(packageName));

        setupActionButtons();
        observeViewModel();
        viewModel.loadPackage(packageName);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (packageName != null && !packageName.isEmpty()) {
            viewModel.loadPackage(packageName);
        }
    }

    private void setupActionButtons() {
        binding.btnDetailEnable.setOnClickListener(v -> requestOperation(OperationType.ENABLE));
        binding.btnDetailDisable.setOnClickListener(v -> requestOperation(OperationType.DISABLE));
        binding.btnDetailFreeze.setOnClickListener(v -> requestOperation(OperationType.FREEZE));
        binding.btnDetailUnfreeze.setOnClickListener(v -> requestOperation(OperationType.UNFREEZE));
        binding.btnDetailForceStop.setOnClickListener(v -> requestOperation(OperationType.FORCE_STOP));

        binding.btnDetailOpenInfo.setOnClickListener(v -> {
            if (currentApp == null) {
                return;
            }
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.fromParts("package", currentApp.getPackageName(), null));
            startActivity(intent);
        });

        binding.btnDetailUninstall.setOnClickListener(v -> {
            if (currentApp == null || currentApp.isSystemApp()) {
                return;
            }
            Intent intent = new Intent(Intent.ACTION_DELETE);
            intent.setData(Uri.fromParts("package", currentApp.getPackageName(), null));
            startActivity(intent);
        });
    }

    private void requestOperation(OperationType type) {
        if (currentApp == null) {
            return;
        }
        String opLabel = LocalizedLabelMapper.getOperationLabel(requireContext(), type);
        if (currentApp.isCriticalSystemPackage() && type != OperationType.ENABLE && type != OperationType.UNFREEZE) {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(getString(R.string.confirm_action_title, opLabel))
                    .setMessage(getString(R.string.confirm_critical_message, currentApp.getAppName(), currentApp.getPackageName()))
                    .setNegativeButton(R.string.action_cancel, null)
                    .setPositiveButton(R.string.action_confirm, (d, w) -> viewModel.executeAction(type))
                    .show();
            return;
        }

        if (appContainer.getPreferencesManager().isConfirmSingleAction() || type == OperationType.DISABLE || type == OperationType.FORCE_STOP) {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(getString(R.string.confirm_action_title, opLabel))
                    .setMessage(getString(R.string.confirm_action_message, opLabel, currentApp.getAppName(), currentApp.getPackageName()))
                    .setNegativeButton(R.string.action_cancel, null)
                    .setPositiveButton(R.string.action_confirm, (d, w) -> viewModel.executeAction(type))
                    .show();
        } else {
            viewModel.executeAction(type);
        }
    }

    private void observeViewModel() {
        viewModel.observeBusy().observe(getViewLifecycleOwner(), busy -> {
            boolean isBusy = Boolean.TRUE.equals(busy);
            binding.progressDetailAction.setVisibility(isBusy ? View.VISIBLE : View.GONE);
            updateButtonAvailability(currentApp, !isBusy);
        });

        viewModel.observeAppInfo().observe(getViewLifecycleOwner(), app -> {
            currentApp = app;
            if (app == null) {
                if (isAdded()) {
                    Snackbar.make(binding.getRoot(), R.string.err_package_not_found, Snackbar.LENGTH_SHORT).show();
                    Navigation.findNavController(binding.getRoot()).navigateUp();
                }
                return;
            }
            renderAppDetails(app);
        });

        viewModel.observeOperationResultEvent().observe(getViewLifecycleOwner(), result -> {
            if (result != null) {
                viewModel.consumeOperationResultEvent();
                String opLabel = LocalizedLabelMapper.getOperationLabel(requireContext(), result.getOperationType());
                String message = result.isSuccess()
                        ? getString(R.string.result_success_single, opLabel, result.getAppName())
                        : getString(R.string.result_failed_single, opLabel, result.getAppName(),
                                LocalizedLabelMapper.getErrorText(requireContext(), result.getErrorType(), result.getRawErrorDetail()));
                Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void renderAppDetails(AppInfoModel app) {
        appContainer.getAppIconLoader().loadIcon(app.getPackageName(), binding.ivDetailIcon);
        binding.tvDetailAppName.setText(app.getAppName());
        binding.tvDetailPackageName.setText(app.getPackageName());

        if (app.isSystemApp()) {
            binding.tvDetailTypePill.setText(R.string.type_system);
            binding.tvDetailTypePill.setBackgroundResource(R.drawable.pill_system);
            binding.tvDetailTypePill.setTextColor(ContextCompat.getColor(requireContext(), R.color.state_system_fg));
        } else {
            binding.tvDetailTypePill.setText(R.string.type_user);
            binding.tvDetailTypePill.setBackgroundResource(R.drawable.pill_user);
            binding.tvDetailTypePill.setTextColor(ContextCompat.getColor(requireContext(), R.color.state_user_fg));
        }

        AppState state = app.getAppState();
        binding.tvDetailStatePill.setText(LocalizedLabelMapper.getStateLabel(requireContext(), state));
        if (state == AppState.ENABLED) {
            binding.tvDetailStatePill.setBackgroundResource(R.drawable.pill_enabled);
            binding.tvDetailStatePill.setTextColor(ContextCompat.getColor(requireContext(), R.color.state_enabled_fg));
        } else if (state == AppState.SUSPENDED) {
            binding.tvDetailStatePill.setBackgroundResource(R.drawable.pill_suspended);
            binding.tvDetailStatePill.setTextColor(ContextCompat.getColor(requireContext(), R.color.state_suspended_fg));
        } else {
            binding.tvDetailStatePill.setBackgroundResource(R.drawable.pill_disabled);
            binding.tvDetailStatePill.setTextColor(ContextCompat.getColor(requireContext(), R.color.state_disabled_fg));
        }

        boolean isSelf = appContainer.getSafetyGuard().isSelfPackage(app.getPackageName());
        if (isSelf) {
            binding.tvProtectedNotice.setVisibility(View.VISIBLE);
            binding.tvProtectedNotice.setText(R.string.details_self_protected);
        } else if (app.isCriticalSystemPackage()) {
            binding.tvProtectedNotice.setVisibility(View.VISIBLE);
            binding.tvProtectedNotice.setText(R.string.details_protected_notice);
        } else {
            binding.tvProtectedNotice.setVisibility(View.GONE);
        }

        updateButtonAvailability(app, !Boolean.TRUE.equals(viewModel.observeBusy().getValue()));
        renderMetadataRows(app);
    }

    // Chi kich hoat nhung thao tac hop le va duoc he thong ho tro cho ung dung hien tai
    private void updateButtonAvailability(@Nullable AppInfoModel app, boolean notBusy) {
        if (app == null) {
            return;
        }
        boolean isSelf = appContainer.getSafetyGuard().isSelfPackage(app.getPackageName());
        boolean suspendSupported = appContainer.getSafetyGuard().isSuspendSupportedPolicy(
                app.getPackageName(),
                app.isCriticalSystemPackage()
        );

        binding.btnDetailEnable.setEnabled(notBusy && !app.isEnabled());
        binding.btnDetailDisable.setEnabled(notBusy && !isSelf && app.isEnabled());
        binding.btnDetailFreeze.setEnabled(notBusy && !isSelf && suspendSupported && !app.isSuspended());
        binding.btnDetailUnfreeze.setEnabled(notBusy && app.isSuspended());
        binding.btnDetailForceStop.setEnabled(notBusy && !isSelf);
        binding.btnDetailUninstall.setVisibility(app.isSystemApp() || isSelf ? View.GONE : View.VISIBLE);
        binding.btnDetailUninstall.setEnabled(notBusy && !app.isSystemApp() && !isSelf);
    }

    private void renderMetadataRows(AppInfoModel app) {
        binding.containerMetadataRows.removeAllViews();
        addMetaRow(R.string.label_package_name, app.getPackageName());
        addMetaRow(R.string.label_version_name, app.getVersionName().isEmpty() ? getString(R.string.value_unknown) : app.getVersionName());
        addMetaRow(R.string.label_version_code, String.valueOf(app.getVersionCode()));
        addMetaRow(R.string.label_uid, String.valueOf(app.getUid()));
        addMetaRow(R.string.label_apk_path, app.getApkPath().isEmpty() ? getString(R.string.value_unknown) : app.getApkPath());
        addMetaRow(R.string.label_install_date, DateTimeFormatterUtil.formatDateTime(app.getInstallTimeMillis()));
        addMetaRow(R.string.label_update_date, DateTimeFormatterUtil.formatDateTime(app.getLastUpdateTimeMillis()));
        addMetaRow(R.string.label_app_type, getString(app.isSystemApp() ? R.string.type_system : R.string.type_user));
        addMetaRow(R.string.label_enabled_state, getString(app.isEnabled() ? R.string.state_enabled : R.string.state_disabled));
        addMetaRow(R.string.label_suspended_state, getString(app.isSuspended() ? R.string.value_yes : R.string.value_no));
    }

    private void addMetaRow(int labelRes, String value) {
        ItemMetadataRowBinding row = ItemMetadataRowBinding.inflate(
                getLayoutInflater(), binding.containerMetadataRows, false
        );
        row.tvMetaLabel.setText(labelRes);
        row.tvMetaValue.setText(value);
        binding.containerMetadataRows.addView(row.getRoot());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
