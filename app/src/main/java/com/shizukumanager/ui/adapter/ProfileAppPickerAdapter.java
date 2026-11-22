package com.shizukumanager.app.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.shizukumanager.app.R;
import com.shizukumanager.app.database.entity.ProfileAppEntity;
import com.shizukumanager.app.databinding.ItemProfileAppPickerBinding;
import com.shizukumanager.app.model.AppInfoModel;
import com.shizukumanager.app.model.ProfileActionType;
import com.shizukumanager.app.util.AppIconLoader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

// Adapter cho phep chon ung dung va hanh dong muc tieu (Disable/Suspend/Enable) trong Profile
public class ProfileAppPickerAdapter extends RecyclerView.Adapter<ProfileAppPickerAdapter.ViewHolder> {

    private final AppIconLoader iconLoader;
    private final long profileId;
    private final List<AppInfoModel> allApps;
    private final List<AppInfoModel> filteredApps = new ArrayList<>();
    private final Map<String, ProfileAppEntity> selectedMap = new LinkedHashMap<>();

    public ProfileAppPickerAdapter(
            AppIconLoader iconLoader,
            long profileId,
            List<AppInfoModel> allApps,
            List<ProfileAppEntity> existingSelection
    ) {
        this.iconLoader = iconLoader;
        this.profileId = profileId;
        this.allApps = allApps != null ? new ArrayList<>(allApps) : new ArrayList<>();
        if (existingSelection != null) {
            for (ProfileAppEntity item : existingSelection) {
                selectedMap.put(item.packageName, new ProfileAppEntity(
                        profileId, item.packageName, item.appName, item.targetAction
                ));
            }
        }
        filter("");
    }

    public void filter(String query) {
        String q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        filteredApps.clear();
        for (AppInfoModel app : allApps) {
            if (app.isCriticalSystemPackage()) {
                continue;
            }
            if (q.isEmpty()
                    || app.getAppName().toLowerCase(Locale.ROOT).contains(q)
                    || app.getPackageName().toLowerCase(Locale.ROOT).contains(q)) {
                filteredApps.add(app);
            }
        }
        filteredApps.sort((a, b) -> {
            boolean sa = selectedMap.containsKey(a.getPackageName());
            boolean sb = selectedMap.containsKey(b.getPackageName());
            if (sa != sb) {
                return sa ? -1 : 1;
            }
            return a.getAppName().compareToIgnoreCase(b.getAppName());
        });
        notifyDataSetChanged();
    }

    public List<ProfileAppEntity> getSelectedEntries() {
        return new ArrayList<>(selectedMap.values());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemProfileAppPickerBinding binding = ItemProfileAppPickerBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(filteredApps.get(position));
    }

    @Override
    public int getItemCount() {
        return filteredApps.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemProfileAppPickerBinding binding;

        ViewHolder(ItemProfileAppPickerBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(AppInfoModel app) {
            Context ctx = binding.getRoot().getContext();
            binding.tvProfilePickerName.setText(app.getAppName());
            binding.tvProfilePickerPkg.setText(app.getPackageName());
            iconLoader.loadIcon(app.getPackageName(), binding.ivProfilePickerIcon);

            ProfileAppEntity selected = selectedMap.get(app.getPackageName());
            binding.cbProfileAppInclude.setOnCheckedChangeListener(null);
            binding.cbProfileAppInclude.setChecked(selected != null);

            if (selected != null) {
                binding.btnProfileTargetAction.setVisibility(View.VISIBLE);
                binding.btnProfileTargetAction.setText(getActionLabel(ctx, selected.targetAction));
            } else {
                binding.btnProfileTargetAction.setVisibility(View.INVISIBLE);
            }

            binding.cbProfileAppInclude.setOnClickListener(v -> toggleApp(app));
            binding.getRoot().setOnClickListener(v -> toggleApp(app));
            binding.btnProfileTargetAction.setOnClickListener(v -> cycleAction(app));
        }

        private void toggleApp(AppInfoModel app) {
            if (selectedMap.containsKey(app.getPackageName())) {
                selectedMap.remove(app.getPackageName());
            } else {
                selectedMap.put(app.getPackageName(), new ProfileAppEntity(
                        profileId,
                        app.getPackageName(),
                        app.getAppName(),
                        ProfileActionType.SUSPEND.name()
                ));
            }
            int pos = getBindingAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                notifyItemChanged(pos);
            }
        }

        private void cycleAction(AppInfoModel app) {
            ProfileAppEntity entry = selectedMap.get(app.getPackageName());
            if (entry == null) {
                return;
            }
            if (ProfileActionType.SUSPEND.name().equals(entry.targetAction)) {
                entry.targetAction = ProfileActionType.DISABLE.name();
            } else if (ProfileActionType.DISABLE.name().equals(entry.targetAction)) {
                entry.targetAction = ProfileActionType.ENABLE.name();
            } else {
                entry.targetAction = ProfileActionType.SUSPEND.name();
            }
            int pos = getBindingAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                notifyItemChanged(pos);
            }
        }

        private String getActionLabel(Context ctx, String action) {
            if (ProfileActionType.DISABLE.name().equals(action)) {
                return ctx.getString(R.string.profile_target_disable);
            }
            if (ProfileActionType.ENABLE.name().equals(action)) {
                return ctx.getString(R.string.profile_target_enable);
            }
            return ctx.getString(R.string.profile_target_suspend);
        }
    }
}
