package com.shizukumanager.app.ui.fragment;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.shizukumanager.app.BuildConfig;
import com.shizukumanager.app.R;
import com.shizukumanager.app.ShizukuManagerApplication;
import com.shizukumanager.app.databinding.FragmentSettingsBinding;
import com.shizukumanager.app.di.AppContainer;
import com.shizukumanager.app.model.ShizukuConnectionState;
import com.shizukumanager.app.model.ShizukuStatus;
import com.shizukumanager.app.util.AppPreferencesManager;
import com.shizukumanager.app.viewmodel.SettingsViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

// Man hinh Cai dat cho Shizuku, Ngon ngu (EN/VI), Giao dien, Hanh vi, Du lieu JSON va Thong tin
public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private SettingsViewModel viewModel;
    private String pendingExportJson = "";

    private final ActivityResultLauncher<String> exportJsonLauncher = registerForActivityResult(
            new ActivityResultContracts.CreateDocument("application/json"),
            uri -> {
                if (uri != null) {
                    writeJsonToUri(uri, pendingExportJson);
                }
            }
    );

    private final ActivityResultLauncher<String[]> importJsonLauncher = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(),
            uri -> {
                if (uri != null) {
                    readJsonFromUriAndImport(uri);
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        AppContainer container = ((ShizukuManagerApplication) requireActivity().getApplication()).getAppContainer();
        viewModel = new ViewModelProvider(this, container.getViewModelFactory())
                .get(SettingsViewModel.class);

        binding.btnBackSettings.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        setupShizukuSection();
        setupAppearanceSection();
        setupBehaviorSection();
        setupDataSection();
        setupAboutSection();
    }

    private void setupShizukuSection() {
        viewModel.observeShizukuStatus().observe(getViewLifecycleOwner(), this::renderShizukuStatus);
        binding.btnSettingsCheckShizuku.setOnClickListener(v -> viewModel.checkShizukuConnection());
        binding.btnSettingsGrantShizuku.setOnClickListener(v -> viewModel.requestShizukuPermission());
    }

    private void renderShizukuStatus(ShizukuStatus status) {
        if (status == null) {
            return;
        }
        ShizukuConnectionState state = status.getState();
        if (state == ShizukuConnectionState.CONNECTED) {
            binding.tvSettingsShizukuStatus.setText(R.string.shizuku_connected);
            binding.tvSettingsShizukuDetail.setText(getString(R.string.shizuku_connected_detail, status.getUid(), status.getApiVersion()));
            binding.btnSettingsGrantShizuku.setEnabled(false);
        } else if (state == ShizukuConnectionState.PERMISSION_REQUIRED) {
            binding.tvSettingsShizukuStatus.setText(R.string.shizuku_permission_required);
            binding.tvSettingsShizukuDetail.setText(R.string.shizuku_permission_required_detail);
            binding.btnSettingsGrantShizuku.setEnabled(true);
        } else if (state == ShizukuConnectionState.SERVICE_NOT_RUNNING) {
            binding.tvSettingsShizukuStatus.setText(R.string.shizuku_not_running);
            binding.tvSettingsShizukuDetail.setText(R.string.shizuku_not_running_detail);
            binding.btnSettingsGrantShizuku.setEnabled(false);
        } else {
            binding.tvSettingsShizukuStatus.setText(R.string.shizuku_not_installed);
            binding.tvSettingsShizukuDetail.setText(R.string.shizuku_not_installed_detail);
            binding.btnSettingsGrantShizuku.setEnabled(false);
        }
    }

    private void setupAppearanceSection() {
        AppPreferencesManager prefs = viewModel.getPreferencesManager();
        String currentLang = prefs.getLanguageTag();
        if ("vi".equals(currentLang)) {
            binding.chipLangVi.setChecked(true);
        } else if ("en".equals(currentLang)) {
            binding.chipLangEn.setChecked(true);
        } else {
            binding.chipLangSystem.setChecked(true);
        }

        binding.chipGroupLanguage.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                return;
            }
            int id = checkedIds.get(0);
            String targetTag = "";
            if (id == R.id.chipLangEn) {
                targetTag = "en";
            } else if (id == R.id.chipLangVi) {
                targetTag = "vi";
            }
            if (!targetTag.equals(prefs.getLanguageTag())) {
                prefs.setLanguageTag(targetTag);
            }
        });

        binding.switchAmbientOrbs.setChecked(prefs.isAmbientOrbsEnabled());
        binding.switchAmbientOrbs.setOnCheckedChangeListener((buttonView, isChecked) ->
                prefs.setAmbientOrbsEnabled(isChecked));
    }

    private void setupBehaviorSection() {
        AppPreferencesManager prefs = viewModel.getPreferencesManager();
        binding.switchConfirmSingle.setChecked(prefs.isConfirmSingleAction());
        binding.switchConfirmSingle.setOnCheckedChangeListener((btn, isChecked) ->
                prefs.setConfirmSingleAction(isChecked));

        binding.switchAutoRefresh.setChecked(prefs.isAutoRefreshOnResume());
        binding.switchAutoRefresh.setOnCheckedChangeListener((btn, isChecked) ->
                prefs.setAutoRefreshOnResume(isChecked));

        binding.switchShowSystemDefault.setChecked(prefs.isShowSystemAppsByDefault());
        binding.switchShowSystemDefault.setOnCheckedChangeListener((btn, isChecked) ->
                prefs.setShowSystemAppsByDefault(isChecked));
    }

    private void setupDataSection() {
        binding.btnExportProfiles.setOnClickListener(v ->
                viewModel.exportProfilesJson(json -> {
                    pendingExportJson = json;
                    exportJsonLauncher.launch("shizuku_manager_profiles.json");
                }));

        binding.btnImportProfiles.setOnClickListener(v ->
                importJsonLauncher.launch(new String[]{"application/json", "text/plain"}));

        binding.btnSettingsClearHistory.setOnClickListener(v ->
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.history_clear)
                        .setMessage(R.string.history_clear_confirm)
                        .setNegativeButton(R.string.action_cancel, null)
                        .setPositiveButton(R.string.action_confirm, (d, w) -> {
                            viewModel.clearHistory();
                            Snackbar.make(binding.getRoot(), R.string.history_cleared, Snackbar.LENGTH_SHORT).show();
                        })
                        .show());
    }

    private void writeJsonToUri(Uri uri, String json) {
        try (OutputStream out = requireContext().getContentResolver().openOutputStream(uri)) {
            if (out != null) {
                out.write(json.getBytes(StandardCharsets.UTF_8));
                Snackbar.make(binding.getRoot(), R.string.settings_export_success, Snackbar.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Snackbar.make(binding.getRoot(),
                    getString(R.string.settings_export_failed, e.getMessage()), Snackbar.LENGTH_LONG).show();
        }
    }

    private void readJsonFromUriAndImport(Uri uri) {
        try (InputStream in = requireContext().getContentResolver().openInputStream(uri);
             BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
            viewModel.importProfilesJson(sb.toString(), (success, count, error) -> {
                if (!isAdded() || binding == null) {
                    return;
                }
                if (success) {
                    Snackbar.make(binding.getRoot(),
                            getString(R.string.settings_import_success, count), Snackbar.LENGTH_SHORT).show();
                } else {
                    Snackbar.make(binding.getRoot(),
                            getString(R.string.settings_import_failed, error), Snackbar.LENGTH_LONG).show();
                }
            });
        } catch (Exception e) {
            Snackbar.make(binding.getRoot(),
                    getString(R.string.settings_import_failed, e.getMessage()), Snackbar.LENGTH_LONG).show();
        }
    }

    private void setupAboutSection() {
        binding.tvAboutVersion.setText(getString(
                R.string.settings_version_label,
                BuildConfig.VERSION_NAME,
                BuildConfig.VERSION_CODE
        ));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
