package com.shizukumanager.app.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.shizukumanager.app.R;
import com.shizukumanager.app.databinding.ItemProfileCardBinding;
import com.shizukumanager.app.model.ProfileWithApps;
import com.shizukumanager.app.util.DateTimeFormatterUtil;
import java.util.Collections;
import java.util.List;

// Adapter hien thi cac the App Profile va thao tac quan ly/ap dung/khoi phuc
public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ViewHolder> {

    public interface OnProfileActionListener {
        void onManageApps(ProfileWithApps profile);
        void onApplyProfile(ProfileWithApps profile);
        void onRestoreProfile(ProfileWithApps profile);
        void onRenameProfile(ProfileWithApps profile);
        void onDeleteProfile(ProfileWithApps profile);
    }

    private final OnProfileActionListener listener;
    private List<ProfileWithApps> items = Collections.emptyList();

    public ProfileAdapter(OnProfileActionListener listener) {
        this.listener = listener;
    }

    public void submitList(List<ProfileWithApps> list) {
        this.items = list != null ? list : Collections.emptyList();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemProfileCardBinding binding = ItemProfileCardBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemProfileCardBinding binding;

        ViewHolder(ItemProfileCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(ProfileWithApps item) {
            Context ctx = binding.getRoot().getContext();
            binding.tvProfileName.setText(item.getProfile().name);
            if (item.getProfile().description.isEmpty()) {
                binding.tvProfileDesc.setVisibility(View.GONE);
            } else {
                binding.tvProfileDesc.setVisibility(View.VISIBLE);
                binding.tvProfileDesc.setText(item.getProfile().description);
            }

            binding.tvProfileAppsCount.setText(ctx.getString(R.string.profile_apps_count, item.getApps().size()));
            if (item.hasSavedStates()) {
                long savedAt = item.getSavedStates().get(0).savedAt;
                binding.tvProfileSavedState.setText(ctx.getString(
                        R.string.profile_saved_state_available,
                        DateTimeFormatterUtil.formatShortTime(savedAt)
                ));
                binding.btnProfileRestore.setEnabled(true);
            } else {
                binding.tvProfileSavedState.setText(R.string.profile_no_saved_state);
                binding.btnProfileRestore.setEnabled(false);
            }

            binding.btnProfileManageApps.setOnClickListener(v -> listener.onManageApps(item));
            binding.btnProfileApply.setOnClickListener(v -> listener.onApplyProfile(item));
            binding.btnProfileRestore.setOnClickListener(v -> listener.onRestoreProfile(item));
            binding.btnProfileRename.setOnClickListener(v -> listener.onRenameProfile(item));
            binding.btnProfileDelete.setOnClickListener(v -> listener.onDeleteProfile(item));
        }
    }
}
