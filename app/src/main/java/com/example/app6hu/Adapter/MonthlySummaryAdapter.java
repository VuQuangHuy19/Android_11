package com.example.app6hu.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app6hu.R;
import com.example.app6hu.model.MonthlySummary;

import java.text.DecimalFormat;
import java.util.List;


public class MonthlySummaryAdapter extends RecyclerView.Adapter<MonthlySummaryAdapter.ViewHolder> {

    private final List<MonthlySummary> data;
    private final DecimalFormat df = new DecimalFormat("#,###");

    public MonthlySummaryAdapter(List<MonthlySummary> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_monthly_summary, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MonthlySummary item = data.get(position);
        holder.tvMonth.setText(item.month);
        holder.tvAmount.setText(df.format(item.amount) + "đ");
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMonth, tvAmount;
        ViewHolder(View v) {
            super(v);
            tvMonth = v.findViewById(R.id.tvMonth);
            tvAmount = v.findViewById(R.id.tvAmount);
        }
    }
}