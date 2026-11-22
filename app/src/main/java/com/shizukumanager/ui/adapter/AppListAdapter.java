package com.shizukumanager.app.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.shizukumanager.app.R;
import com.shizukumanager.app.databinding.ItemAppRowBinding;
import com.shizukumanager.app.model.AppInfoModel;
import com.shizukumanager.app.model.AppState;
import com.shizukumanager.app.util.AppIconLoader;
import com.shizukumanager.app.util.LocalizedLabelMapper;
import java.util.Collections;
import java.util.Set;

// Adapter hien thi danh sach ung dung voi DiffUtil, tai icon bat dong bo va chon nhieu
public class AppListAdapter extends ListAdapter<AppInfoModel, AppListAdapter.AppViewHolder> {

    public enum QuickActionMode {
        NONE,
        ENABLE,
        UNSUSPEND
    }

    public interface OnAppRowListener {
        void onAppClicked(AppInfoModel app);
        void onAppSelectionToggled(AppInfoModel app);
        void onQuickActionClicked(AppInfoModel app);
    }

    private static final DiffUtil.ItemCallback<AppInfoModel> DIFF_CALLBACK = new DiffUtil.ItemCallback<AppInfoModel>() {
        @Override
        public boolean areItemsTheSame(@NonNull AppInfoModel oldItem, @NonNull AppInfoModel newItem) {
            return oldItem.getPackageName().equals(newItem.getPackageName());
        }

        @Override
        public boolean areContentsTheSame(@NonNull AppInfoModel oldItem, @NonNull AppInfoModel newItem) {
            return oldItem.equals(newItem);
        }
    };

    private final AppIconLoader iconLoader;
    private final QuickActionMode quickActionMode;
    private final OnAppRowListener listener;
    private Set<String> selectedPackages = Collections.emptySet();

    public AppListAdapter(
            AppIconLoader iconLoader,
            QuickActionMode quickActionMode,
            OnAppRowListener listener
    ) {
        super(DIFF_CALLBACK);
        this.iconLoader = iconLoader;
        this.quickActionMode = quickActionMode;
        this.listener = listener;
    }

    public void setSelectedPackages(Set<String> selected) {
        this.selectedPackages = selected != null ? selected : Collections.emptySet();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAppRowBinding binding = ItemAppRowBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new AppViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AppViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class AppViewHolder extends RecyclerView.ViewHolder {
        private final ItemAppRowBinding binding;

        AppViewHolder(ItemAppRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(AppInfoModel app) {
            Context ctx = binding.getRoot().getContext();
            binding.tvAppName.setText(app.getAppName());
            binding.tvPackageName.setText(app.getPackageName());
            iconLoader.loadIcon(app.getPackageName(), binding.ivAppIcon);

            // Hien thi loai ung dung System / User
            if (app.isSystemApp()) {
                binding.tvAppTypePill.setText(R.string.type_system);
                binding.tvAppTypePill.setBackgroundResource(R.drawable.pill_system);
                binding.tvAppTypePill.setTextColor(ContextCompat.getColor(ctx, R.color.state_system_fg));
            } else {
                binding.tvAppTypePill.setText(R.string.type_user);
                binding.tvAppTypePill.setBackgroundResource(R.drawable.pill_user);
                binding.tvAppTypePill.setTextColor(ContextCompat.getColor(ctx, R.color.state_user_fg));
            }

            // Hien thi trang thai thuc te cua ung dung
            AppState state = app.getAppState();
            binding.tvAppStatePill.setText(LocalizedLabelMapper.getStateLabel(ctx, state));
            if (state == AppState.ENABLED) {
                binding.tvAppStatePill.setBackgroundResource(R.drawable.pill_enabled);
                binding.tvAppStatePill.setTextColor(ContextCompat.getColor(ctx, R.color.state_enabled_fg));
            } else if (state == AppState.SUSPENDED) {
                binding.tvAppStatePill.setBackgroundResource(R.drawable.pill_suspended);
                binding.tvAppStatePill.setTextColor(ContextCompat.getColor(ctx, R.color.state_suspended_fg));
            } else {
                binding.tvAppStatePill.setBackgroundResource(R.drawable.pill_disabled);
                binding.tvAppStatePill.setTextColor(ContextCompat.getColor(ctx, R.color.state_disabled_fg));
            }

            // Dong bo trang thai checkbox ma khong kich hoat callback lap
            boolean checked = selectedPackages.contains(app.getPackageName());
            binding.cbSelectApp.setOnCheckedChangeListener(null);
            binding.cbSelectApp.setChecked(checked);
            binding.cbSelectApp.setOnClickListener(v -> listener.onAppSelectionToggled(app));

            binding.getRoot().setOnClickListener(v -> listener.onAppClicked(app));
            binding.getRoot().setOnLongClickListener(v -> {
                listener.onAppSelectionToggled(app);
                return true;
            });

            if (quickActionMode == QuickActionMode.ENABLE) {
                binding.btnQuickRowAction.setVisibility(View.VISIBLE);
                binding.btnQuickRowAction.setText(R.string.op_enable);
                binding.btnQuickRowAction.setBackgroundResource(R.drawable.btn_grad_enable);
                binding.btnQuickRowAction.setOnClickListener(v -> listener.onQuickActionClicked(app));
            } else if (quickActionMode == QuickActionMode.UNSUSPEND) {
                binding.btnQuickRowAction.setVisibility(View.VISIBLE);
                binding.btnQuickRowAction.setText(R.string.op_unfreeze);
                binding.btnQuickRowAction.setBackgroundResource(R.drawable.btn_grad_unfreeze);
                binding.btnQuickRowAction.setOnClickListener(v -> listener.onQuickActionClicked(app));
            } else {
                binding.btnQuickRowAction.setVisibility(View.GONE);
            }
        }
    }
}
