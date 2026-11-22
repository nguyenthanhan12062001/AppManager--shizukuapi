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
import com.shizukumanager.app.database.entity.OperationHistoryEntity;
import com.shizukumanager.app.databinding.ItemHistoryRowBinding;
import com.shizukumanager.app.model.AppState;
import com.shizukumanager.app.model.OperationType;
import com.shizukumanager.app.util.DateTimeFormatterUtil;
import com.shizukumanager.app.util.LocalizedLabelMapper;

// Adapter hien thi lich su thao tac chi tiet kem ket qua va thong bao loi
public class HistoryAdapter extends ListAdapter<OperationHistoryEntity, HistoryAdapter.ViewHolder> {

    private static final DiffUtil.ItemCallback<OperationHistoryEntity> DIFF = new DiffUtil.ItemCallback<OperationHistoryEntity>() {
        @Override
        public boolean areItemsTheSame(@NonNull OperationHistoryEntity oldItem, @NonNull OperationHistoryEntity newItem) {
            return oldItem.id == newItem.id;
        }

        @Override
        public boolean areContentsTheSame(@NonNull OperationHistoryEntity oldItem, @NonNull OperationHistoryEntity newItem) {
            return oldItem.id == newItem.id
                    && oldItem.success == newItem.success
                    && oldItem.timestampMillis == newItem.timestampMillis;
        }
    };

    public HistoryAdapter() {
        super(DIFF);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemHistoryRowBinding binding = ItemHistoryRowBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemHistoryRowBinding binding;

        ViewHolder(ItemHistoryRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(OperationHistoryEntity item) {
            Context ctx = binding.getRoot().getContext();
            binding.tvHistoryAppName.setText(item.appName);
            binding.tvHistoryPackage.setText(item.packageName);
            binding.tvHistoryTimestamp.setText(DateTimeFormatterUtil.formatDateTime(item.timestampMillis));

            String opLabel;
            try {
                opLabel = LocalizedLabelMapper.getOperationLabel(ctx, OperationType.valueOf(item.operationType));
            } catch (Exception e) {
                opLabel = item.operationType;
            }
            binding.tvHistoryOperation.setText(opLabel);

            String prev = mapState(ctx, item.previousState);
            String next = mapState(ctx, item.newState);
            binding.tvHistoryTransition.setText(ctx.getString(R.string.history_transition_format, prev, next));

            if (item.success) {
                binding.tvHistoryResultPill.setText(R.string.history_status_success);
                binding.tvHistoryResultPill.setBackgroundResource(R.drawable.pill_enabled);
                binding.tvHistoryResultPill.setTextColor(ContextCompat.getColor(ctx, R.color.state_enabled_fg));
                binding.tvHistoryError.setVisibility(View.GONE);
            } else {
                binding.tvHistoryResultPill.setText(R.string.history_status_failed);
                binding.tvHistoryResultPill.setBackgroundResource(R.drawable.pill_disabled);
                binding.tvHistoryResultPill.setTextColor(ContextCompat.getColor(ctx, R.color.state_error_fg));
                if (item.errorMessage != null && !item.errorMessage.isEmpty()) {
                    binding.tvHistoryError.setVisibility(View.VISIBLE);
                    binding.tvHistoryError.setText(item.errorMessage);
                } else {
                    binding.tvHistoryError.setVisibility(View.GONE);
                }
            }
        }

        private String mapState(Context ctx, String raw) {
            try {
                return LocalizedLabelMapper.getStateLabel(ctx, AppState.valueOf(raw));
            } catch (Exception e) {
                return raw;
            }
        }
    }
}
