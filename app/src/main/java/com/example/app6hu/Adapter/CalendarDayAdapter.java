package com.example.app6hu.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.app6hu.Fragments.CalendarFragment;
import com.example.app6hu.R;
import com.example.app6hu.utils.FormatUtils;

import java.util.List;
import java.util.Map;

public class CalendarDayAdapter extends BaseAdapter {

    private final Context context;
    private final List<Integer> days;
    private final LayoutInflater inflater;
    private Map<String, CalendarFragment.DaySummary> dailySummary;

    public CalendarDayAdapter(Context context, List<Integer> days) {
        this.context = context;
        this.days = days;
        this.inflater = LayoutInflater.from(context);
    }

    public void setDailySummary(Map<String, CalendarFragment.DaySummary> map) {
        this.dailySummary = map;
    }

    @Override
    public Object getItem(int position) {
        return days == null ? null : days.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getCount() {
        return days == null ? 0 : days.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_calendar_day, parent, false);
            holder = new ViewHolder();
            holder.tvDayNumber = convertView.findViewById(R.id.tvDayNumber);
            holder.tvIncome = convertView.findViewById(R.id.tvIncome);
            holder.tvExpense = convertView.findViewById(R.id.tvExpense);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        int day = days.get(position);
        int absDay = Math.abs(day);
        holder.tvDayNumber.setText(String.valueOf(absDay));

        if (day < 0) {
            holder.tvDayNumber.setTextColor(Color.GRAY);
            holder.tvIncome.setText("");
            holder.tvExpense.setText("");
            holder.tvIncome.setTextColor(Color.GRAY);
            holder.tvExpense.setTextColor(Color.GRAY);
        } else {
            holder.tvDayNumber.setTextColor(Color.BLACK);

            CalendarFragment.DaySummary sum =
                    dailySummary != null ? dailySummary.get(String.valueOf(absDay)) : null;

            if (sum != null) {
                // Sử dụng FormatUtils để format tiền
                if (sum.income > 0) {
                    holder.tvIncome.setText("+" + FormatUtils.formatCurrency(sum.income));
                    holder.tvIncome.setTextColor(Color.parseColor("#FF4CAF50"));
                } else {
                    holder.tvIncome.setText("");
                }

                if (sum.expense > 0) {
                    holder.tvExpense.setText("-" + FormatUtils.formatCurrency(sum.expense));
                    holder.tvExpense.setTextColor(Color.parseColor("#FFF44336"));
                } else {
                    holder.tvExpense.setText("");
                }
            } else {
                holder.tvIncome.setText("");
                holder.tvExpense.setText("");
                holder.tvIncome.setTextColor(Color.GRAY);
                holder.tvExpense.setTextColor(Color.GRAY);
            }
        }

        return convertView;
    }

    private static class ViewHolder {
        TextView tvDayNumber;
        TextView tvIncome;
        TextView tvExpense;
    }
}