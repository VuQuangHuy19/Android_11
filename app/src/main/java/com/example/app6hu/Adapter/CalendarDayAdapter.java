package com.example.app6hu.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.example.app6hu.R;
import java.util.List;

public class CalendarDayAdapter extends BaseAdapter {

    private final Context context;
    private final List<Integer> days; // 0 => ô trống
    private final LayoutInflater inflater;

    public CalendarDayAdapter(Context context, List<Integer> days) {
        this.context = context;
        this.days = days;
        this.inflater = LayoutInflater.from(context);
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
    // Đây là nơi bind dữ liệu cho item_calendar_day.xml
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
        if (day == 0) {
            // ô trống
            holder.tvDayNumber.setText("");
            holder.tvIncome.setText("");
            holder.tvExpense.setText("");
        } else {
            int absDay = Math.abs(day);
            holder.tvDayNumber.setText(String.valueOf(absDay));


            if (day < 0) {
                holder.tvDayNumber.setTextColor(Color.parseColor("#AAAAAA"));
                holder.tvIncome.setText("");
                holder.tvExpense.setText("");
            } else {
                holder.tvDayNumber.setTextColor(Color.parseColor("#000000"));
                holder.tvIncome.setText("+0");
                holder.tvExpense.setText("-0");
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