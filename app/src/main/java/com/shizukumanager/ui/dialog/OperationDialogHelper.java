package com.shizukumanager.app.ui.dialog;

import android.content.Context;
import android.view.LayoutInflater;
import androidx.appcompat.app.AlertDialog;
import com.shizukumanager.app.R;
import com.shizukumanager.app.databinding.DialogBulkProgressBinding;
import com.shizukumanager.app.model.BulkOperationSummary;
import com.shizukumanager.app.model.OperationResult;
import com.shizukumanager.app.model.OperationType;
import com.shizukumanager.app.util.LocalizedLabelMapper;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

// Tien ich hien thi hop thoai xac nhan, tien trinh va tong ket ket qua thao tac hang loat
public final class OperationDialogHelper {

    public static class ProgressDialogController {
        private final AlertDialog dialog;
        private final DialogBulkProgressBinding binding;
        private final Context context;

        ProgressDialogController(Context context) {
            this.context = context;
            this.binding = DialogBulkProgressBinding.inflate(LayoutInflater.from(context));
            this.dialog = new MaterialAlertDialogBuilder(context)
                    .setView(binding.getRoot())
                    .setCancelable(false)
                    .create();
        }

        public void show(OperationType type) {
            String opLabel = LocalizedLabelMapper.getOperationLabel(context, type);
            binding.tvProgressTitle.setText(context.getString(R.string.progress_executing, opLabel));
            binding.progressBarBulk.setIndeterminate(true);
            binding.tvProgressDetail.setText("");
            dialog.show();
        }

        public void update(int completed, int total, String appName) {
            if (total > 0) {
                binding.progressBarBulk.setIndeterminate(false);
                binding.progressBarBulk.setMax(total);
                binding.progressBarBulk.setProgress(completed);
                binding.tvProgressDetail.setText(
                        context.getString(R.string.progress_item_format, completed, total, appName)
                );
            }
        }

        public void dismiss() {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }
    }

    private OperationDialogHelper() {
    }

    public static ProgressDialogController createProgressController(Context context) {
        return new ProgressDialogController(context);
    }

    // Hien thi ket qua trung thuc, phan biet thanh cong toan bo, mot phan va that bai
    public static void showBulkSummaryDialog(Context context, BulkOperationSummary summary) {
        if (summary == null) {
            return;
        }
        StringBuilder message = new StringBuilder();
        if (summary.isAllSuccess()) {
            message.append(context.getString(R.string.bulk_result_all_success, summary.getSuccessCount()));
        } else if (summary.isPartialSuccess()) {
            message.append(context.getString(
                    R.string.bulk_result_partial,
                    summary.getSuccessCount(),
                    summary.getFailureCount()
            ));
        } else {
            message.append(context.getString(R.string.bulk_result_all_failed, summary.getFailureCount()));
        }

        if (summary.getFailureCount() > 0) {
            message.append("\n\n");
            for (OperationResult r : summary.getResults()) {
                if (!r.isSuccess()) {
                    message.append("• ")
                            .append(r.getAppName())
                            .append(": ")
                            .append(LocalizedLabelMapper.getErrorText(context, r.getErrorType(), r.getRawErrorDetail()))
                            .append("\n");
                }
            }
        }

        new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.bulk_result_title)
                .setMessage(message.toString().trim())
                .setPositiveButton(R.string.action_ok, null)
                .show();
    }
}
