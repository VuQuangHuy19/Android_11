package com.example.app6hu.Adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app6hu.R;
import com.example.app6hu.model.Goal;
import com.example.app6hu.utils.FormatUtils;

import java.text.DecimalFormat;
import java.util.List;

public class GoalAdapter extends RecyclerView.Adapter<GoalAdapter.ViewHolder> {

    private List<Goal> goals;
    private OnGoalClickListener listener;

    public interface OnGoalClickListener {
        void onGoalClick(Goal goal);
        void onGoalLongClick(Goal goal);
    }

    public GoalAdapter(List<Goal> goals, OnGoalClickListener listener) {
        this.goals = goals;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_goal, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Goal goal = goals.get(position);
        
        // Tên mục tiêu
        holder.tvGoalName.setText(goal.getName());
        
        // Deadline
        if (goal.getDeadline() != null && !goal.getDeadline().isEmpty()) {
            holder.tvDeadline.setText("Hạn: " + goal.getDeadline());
            holder.tvDeadline.setVisibility(View.VISIBLE);
        } else {
            holder.tvDeadline.setVisibility(View.GONE);
        }
        
        // Trạng thái
        String status = goal.getStatus() != null ? goal.getStatus() : "active";
        if ("completed".equals(status)) {
            holder.tvStatus.setText("Hoàn thành");
            holder.tvStatus.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.green));
            holder.tvStatus.setTextColor(Color.WHITE);
        } else {
            holder.tvStatus.setText("Đang thực hiện");
            holder.tvStatus.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.teal_200));
            holder.tvStatus.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.teal_700));
        }
        
        // Màu indicator
        try {
            if (goal.getColor() != null && !goal.getColor().isEmpty()) {
                holder.colorIndicator.setBackgroundColor(Color.parseColor(goal.getColor()));
            } else {
                holder.colorIndicator.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.teal_700));
            }
        } catch (Exception e) {
            holder.colorIndicator.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.teal_700));
        }
        
        // Số tiền
        DecimalFormat df = new DecimalFormat("#,###");
        long saved = goal.getSavedAmount();
        long target = goal.getTargetAmount();
        
        holder.tvSavedAmount.setText("Đã tiết kiệm: " + df.format(saved) + "đ");
        holder.tvTargetAmount.setText("/ " + df.format(target) + "đ");
        
        // Progress bar
        int progress = 0;
        if (target > 0) {
            progress = (int) ((saved * 100) / target);
            if (progress > 100) progress = 100;
        }
        holder.progressGoal.setProgress(progress);
        
        // Ghi chú
        if (goal.getNote() != null && !goal.getNote().isEmpty()) {
            holder.tvNote.setText("Ghi chú: " + goal.getNote());
            holder.tvNote.setVisibility(View.VISIBLE);
        } else {
            holder.tvNote.setVisibility(View.GONE);
        }
        
        // Click listeners
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onGoalClick(goal);
            }
        });
        
        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onGoalLongClick(goal);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return goals != null ? goals.size() : 0;
    }

    public void updateGoals(List<Goal> newGoals) {
        this.goals = newGoals;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvGoalName, tvDeadline, tvStatus, tvSavedAmount, tvTargetAmount, tvNote;
        ProgressBar progressGoal;
        View colorIndicator;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvGoalName = itemView.findViewById(R.id.tvGoalName);
            tvDeadline = itemView.findViewById(R.id.tvDeadline);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvSavedAmount = itemView.findViewById(R.id.tvSavedAmount);
            tvTargetAmount = itemView.findViewById(R.id.tvTargetAmount);
            tvNote = itemView.findViewById(R.id.tvNote);
            progressGoal = itemView.findViewById(R.id.progressGoal);
            colorIndicator = itemView.findViewById(R.id.colorIndicator);
        }
    }
}
