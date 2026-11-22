package com.shizukumanager.app.ui.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.shizukumanager.app.R;
import com.shizukumanager.app.ShizukuManagerApplication;
import com.shizukumanager.app.database.entity.AppProfileEntity;
import com.shizukumanager.app.databinding.DialogProfileAppsBinding;
import com.shizukumanager.app.databinding.DialogProfileEditorBinding;
import com.shizukumanager.app.databinding.FragmentProfilesBinding;
import com.shizukumanager.app.di.AppContainer;
import com.shizukumanager.app.model.AppInfoModel;
import com.shizukumanager.app.model.ProfileWithApps;
import com.shizukumanager.app.ui.adapter.ProfileAdapter;
import com.shizukumanager.app.ui.adapter.ProfileAppPickerAdapter;
import com.shizukumanager.app.ui.dialog.OperationDialogHelper;
import com.shizukumanager.app.viewmodel.ProfilesViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import java.util.Collections;
import java.util.List;

// Man hinh quan ly App Profiles (Gaming, Daily, Minimal) kem luu/khoi phuc trang thai
public class ProfilesFragment extends Fragment {

    private FragmentProfilesBinding binding;
    private ProfilesViewModel viewModel;
    private AppContainer appContainer;
    private ProfileAdapter adapter;
    private List<AppInfoModel> latestAllApps = Collections.emptyList();
    private OperationDialogHelper.ProgressDialogController progressDialog;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        binding = FragmentProfilesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        appContainer = ((ShizukuManagerApplication) requireActivity().getApplication()).getAppContainer();
        viewModel = new ViewModelProvider(this, appContainer.getViewModelFactory())
                .get(ProfilesViewModel.class);

        progressDialog = OperationDialogHelper.createProgressController(requireContext());

        adapter = new ProfileAdapter(new ProfileAdapter.OnProfileActionListener() {
            @Override
            public void onManageApps(ProfileWithApps profile) {
                showManageAppsDialog(profile);
            }

            @Override
            public void onApplyProfile(ProfileWithApps profile) {
                if (profile.getApps().isEmpty()) {
                    Snackbar.make(binding.getRoot(), R.string.profile_no_apps_warning, Snackbar.LENGTH_SHORT).show();
                    return;
                }
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(profile.getProfile().name)
                        .setMessage(getString(R.string.confirm_bulk_message, getString(R.string.profile_action_apply), profile.getApps().size()))
                        .setNegativeButton(R.string.action_cancel, null)
                        .setPositiveButton(R.string.action_confirm, (d, w) ->
                                viewModel.applyProfile(profile.getProfile().id, profile.getApps().size()))
                        .show();
            }

            @Override
            public void onRestoreProfile(ProfileWithApps profile) {
                if (!profile.hasSavedStates()) {
                    return;
                }
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.profile_action_restore)
                        .setMessage(getString(R.string.profile_restore_confirm, profile.getSavedStates().size(), profile.getProfile().name))
                        .setNegativeButton(R.string.action_cancel, null)
                        .setPositiveButton(R.string.action_confirm, (d, w) ->
                                viewModel.restoreProfile(profile.getProfile().id, profile.getSavedStates().size()))
                        .show();
            }

            @Override
            public void onRenameProfile(ProfileWithApps profile) {
                showProfileEditorDialog(profile.getProfile());
            }

            @Override
            public void onDeleteProfile(ProfileWithApps profile) {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.profile_action_delete)
                        .setMessage(getString(R.string.profile_delete_confirm, profile.getProfile().name))
                        .setNegativeButton(R.string.action_cancel, null)
                        .setPositiveButton(R.string.profile_action_delete, (d, w) ->
                                viewModel.deleteProfile(profile.getProfile()))
                        .show();
            }
        });

        binding.rvProfiles.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvProfiles.setAdapter(adapter);
        binding.btnCreateProfile.setOnClickListener(v -> showProfileEditorDialog(null));

        observeViewModel();
    }

    private void observeViewModel() {
        viewModel.observeAllApps().observe(getViewLifecycleOwner(), apps ->
                latestAllApps = apps != null ? apps : Collections.emptyList());

        viewModel.observeProfiles().observe(getViewLifecycleOwner(), profiles -> {
            adapter.submitList(profiles);
            boolean empty = profiles == null || profiles.isEmpty();
            binding.emptyProfilesContainer.setVisibility(empty ? View.VISIBLE : View.GONE);
            binding.rvProfiles.setVisibility(empty ? View.GONE : View.VISIBLE);
        });

        viewModel.observeProgress().observe(getViewLifecycleOwner(), state -> {
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

        viewModel.observeExecutionSummaryEvent().observe(getViewLifecycleOwner(), summary -> {
            if (summary != null) {
                viewModel.consumeExecutionSummaryEvent();
                OperationDialogHelper.showBulkSummaryDialog(requireContext(), summary);
            }
        });
    }

    private void showProfileEditorDialog(@Nullable AppProfileEntity existing) {
        DialogProfileEditorBinding dialogBinding = DialogProfileEditorBinding.inflate(getLayoutInflater());
        if (existing != null) {
            dialogBinding.scrollPresetChips.setVisibility(View.GONE);
            dialogBinding.etProfileName.setText(existing.name);
            dialogBinding.etProfileDesc.setText(existing.description);
        } else {
            dialogBinding.chipPresetGaming.setOnClickListener(v -> {
                dialogBinding.etProfileName.setText(R.string.profile_preset_gaming);
                dialogBinding.etProfileDesc.setText(R.string.profile_preset_gaming_desc);
            });
            dialogBinding.chipPresetDaily.setOnClickListener(v -> {
                dialogBinding.etProfileName.setText(R.string.profile_preset_daily);
                dialogBinding.etProfileDesc.setText(R.string.profile_preset_daily_desc);
            });
            dialogBinding.chipPresetMinimal.setOnClickListener(v -> {
                dialogBinding.etProfileName.setText(R.string.profile_preset_minimal);
                dialogBinding.etProfileDesc.setText(R.string.profile_preset_minimal_desc);
            });
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(existing == null ? R.string.profile_create : R.string.profile_action_rename)
                .setView(dialogBinding.getRoot())
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_save, (d, w) -> {
                    String name = dialogBinding.etProfileName.getText() != null
                            ? dialogBinding.etProfileName.getText().toString().trim() : "";
                    String desc = dialogBinding.etProfileDesc.getText() != null
                            ? dialogBinding.etProfileDesc.getText().toString().trim() : "";
                    if (name.isEmpty()) {
                        return;
                    }
                    if (existing == null) {
                        viewModel.createProfile(name, desc);
                    } else {
                        viewModel.renameProfile(existing, name, desc);
                    }
                })
                .show();
    }

    private void showManageAppsDialog(ProfileWithApps profile) {
        DialogProfileAppsBinding dialogBinding = DialogProfileAppsBinding.inflate(getLayoutInflater());
        ProfileAppPickerAdapter pickerAdapter = new ProfileAppPickerAdapter(
                appContainer.getAppIconLoader(),
                profile.getProfile().id,
                latestAllApps,
                profile.getApps()
        );
        dialogBinding.rvProfileAppsPicker.setLayoutManager(new LinearLayoutManager(requireContext()));
        dialogBinding.rvProfileAppsPicker.setAdapter(pickerAdapter);

        dialogBinding.etSearchPicker.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                pickerAdapter.filter(s == null ? "" : s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.profile_manage_apps_title)
                .setView(dialogBinding.getRoot())
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_save, (d, w) ->
                        viewModel.updateProfileApps(profile.getProfile().id, pickerAdapter.getSelectedEntries()))
                .show();
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
