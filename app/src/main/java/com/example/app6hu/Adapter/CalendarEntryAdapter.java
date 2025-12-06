package com.example.app6hu.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.app6hu.R;
import com.example.app6hu.model.Calendars;
import com.example.app6hu.utils.FormatUtils;

import java.util.List;

public class CalendarEntryAdapter extends BaseAdapter {

    private final Context context;
    private final List<Calendars> entries;
    private final LayoutInflater inflater;

    public CalendarEntryAdapter(Context context, List<Calendars> entries) {
        this.context = context;
        this.entries = entries;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() { return entries.size(); }

    @Override
    public Object getItem(int position) { return entries.get(position); }

    @Override
    public long getItemId(int position) { return position; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        Calendars entry = entries.get(position);

        boolean showHeader = shouldShowHeader(position, entry.getDay());

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_day_calendar, parent, false);
            holder = new ViewHolder();
            holder.tvDayHeader = convertView.findViewById(R.id.tvDayHeader);
            holder.tvTotalIncome = convertView.findViewById(R.id.tvTotalIncome);
            holder.tvTotalExpense = convertView.findViewById(R.id.tvTotalExpense);
            holder.layoutEntry = convertView.findViewById(R.id.layoutEntry);
            holder.ivIcon = convertView.findViewById(R.id.ivIcon);
            holder.tvCategory = convertView.findViewById(R.id.tvCategory);
            holder.tvDescription = convertView.findViewById(R.id.tvDescription);
            holder.tvAmount = convertView.findViewById(R.id.tvAmount);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // Hiển thị header nếu cần
        if (showHeader) {
            holder.tvDayHeader.setVisibility(View.VISIBLE);
            holder.tvTotalIncome.setVisibility(View.VISIBLE);
            holder.tvTotalExpense.setVisibility(View.VISIBLE);

            holder.tvDayHeader.setText(entry.getDayName());

            // Tính tổng thu/chi cho ngày này
            double dayIncome = 0;
            double dayExpense = 0;
            for (Calendars e : entries) {
                if (e.getDay() == entry.getDay()) {
                    if (e.isExpense()) {
                        dayExpense += e.getAmount();
                    } else {
                        dayIncome += e.getAmount();
                    }
                }
            }

            holder.tvTotalIncome.setText("+" + FormatUtils.formatCurrency(dayIncome));
            holder.tvTotalExpense.setText("-" + FormatUtils.formatCurrency(dayExpense));
        } else {
            holder.tvDayHeader.setVisibility(View.GONE);
            holder.tvTotalIncome.setVisibility(View.GONE);
            holder.tvTotalExpense.setVisibility(View.GONE);
        }

        // Hiển thị thông tin giao dịch
        holder.ivIcon.setImageResource(entry.getIconResId());
        holder.tvCategory.setText(entry.getCategory());
        holder.tvDescription.setText(entry.getDescription());

        if (entry.isExpense()) {
            holder.tvAmount.setText("-" + FormatUtils.formatCurrency(entry.getAmount()));
            holder.tvAmount.setTextColor(context.getColor(R.color.red));
        } else {
            holder.tvAmount.setText("+" + FormatUtils.formatCurrency(entry.getAmount()));
            holder.tvAmount.setTextColor(context.getColor(R.color.teal_700));
        }

        return convertView;
    }

    private boolean shouldShowHeader(int position, int currentDay) {
        if (position == 0) return true;

        Calendars previousEntry = entries.get(position - 1);
        return previousEntry.getDay() != currentDay;
    }

    static class ViewHolder {
        TextView tvDayHeader, tvTotalIncome, tvTotalExpense;
        View layoutEntry;
        ImageView ivIcon;
        TextView tvCategory, tvDescription, tvAmount;
    }
}