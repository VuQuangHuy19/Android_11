package com.example.app6hu.Adapter;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app6hu.Activities.ReportDetail;
import com.example.app6hu.R;
import com.example.app6hu.model.ReportItem;

import java.text.DecimalFormat;
import java.util.List;
public class ReportItemAdapter extends RecyclerView.Adapter<ReportItemAdapter.ViewHolder> {

    private List<ReportItem> items;
    private boolean isExpense;
    private Context context;
    private OnItemClickListener listener;

    // Giao diện callback
    public interface OnItemClickListener {
        void onItemClick(ReportItem item);
    }

    public ReportItemAdapter(List<ReportItem> items, boolean isExpense, Context context, OnItemClickListener listener) {
        this.items = items;
        this.isExpense = isExpense;
        this.context = context;
        this.listener = listener;
    }

    public void updateData(List<ReportItem> newItems, boolean isExpense){
        this.items = newItems;
        this.isExpense = isExpense;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_report, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ReportItem item = items.get(position);
        DecimalFormat df = new DecimalFormat("#,###");

        // Hiển thị dữ liệu
        holder.tvName.setText(item.getName());
        holder.tvAmount.setText((isExpense ? "- " : "+ ") + df.format(item.getAmount()) + "đ");
        holder.tvAmount.setTextColor(isExpense ? Color.RED : Color.parseColor("#2E7D32"));

        // Chấm màu bên trái
        try {
            holder.colorDot.setBackgroundColor(Color.parseColor(item.getColor()));
        } catch (Exception e) {
            holder.colorDot.setBackgroundColor(Color.GRAY);
        }

        // Xử lý click
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // ViewHolder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvAmount;
        View colorDot;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            colorDot = itemView.findViewById(R.id.colorDot);
        }
    }
}