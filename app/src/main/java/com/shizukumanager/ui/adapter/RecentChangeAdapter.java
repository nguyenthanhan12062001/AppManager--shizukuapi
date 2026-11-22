package com.shizukumanager.app.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.shizukumanager.app.R;
import com.shizukumanager.app.database.entity.OperationHistoryEntity;
import com.shizukumanager.app.databinding.ItemRecentChangeBinding;
import com.shizukumanager.app.model.AppState;
import com.shizukumanager.app.model.OperationType;
import com.shizukumanager.app.util.LocalizedLabelMapper;
import java.util.Collections;
import java.util.List;

// Adapter hien thi danh sach cac thay doi trang thai gan day tren Dashboard
public class RecentChangeAdapter extends RecyclerView.Adapter<RecentChangeAdapter.ViewHolder> {

    private List<OperationHistoryEntity> items = Collections.emptyList();

    public void submitList(List<OperationHistoryEntity> newItems) {
        this.items = newItems != null ? newItems : Collections.emptyList();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemRecentChangeBinding binding = ItemRecentChangeBinding.inflate(
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

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemRecentChangeBinding binding;

        ViewHolder(ItemRecentChangeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(OperationHistoryEntity item) {
            Context ctx = binding.getRoot().getContext();
            binding.tvRecentAppName.setText(item.appName);

            String prev = formatState(ctx, item.previousState);
            String next = formatState(ctx, item.newState);
            binding.tvRecentTransition.setText(ctx.getString(R.string.history_transition_format, prev, next));

            String opLabel = formatOp(ctx, item.operationType);
            binding.tvRecentOpBadge.setText(opLabel);
            if (item.success) {
                binding.tvRecentOpBadge.setBackgroundResource(R.drawable.pill_enabled);
                binding.tvRecentOpBadge.setTextColor(ContextCompat.getColor(ctx, R.color.state_enabled_fg));
            } else {
                binding.tvRecentOpBadge.setBackgroundResource(R.drawable.pill_disabled);
                binding.tvRecentOpBadge.setTextColor(ContextCompat.getColor(ctx, R.color.state_error_fg));
            }
        }

        private String formatState(Context ctx, String raw) {
            try {
                return LocalizedLabelMapper.getStateLabel(ctx, AppState.valueOf(raw));
            } catch (Exception e) {
                return raw;
            }
        }

        private String formatOp(Context ctx, String raw) {
            try {
                return LocalizedLabelMapper.getOperationLabel(ctx, OperationType.valueOf(raw));
            } catch (Exception e) {
                return raw;
            }
        }
    }
}
