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

import java.text.DecimalFormat;
import java.util.List;

public class CalendarEntryAdapter extends BaseAdapter {

    private final Context context;
    private final List<Calendars> entries;
    private final LayoutInflater inflater;
    private final DecimalFormat df = new DecimalFormat("#,###");

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

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_day_calendar, parent, false);
            holder = new ViewHolder();
            holder.tvDayHeader = convertView.findViewById(R.id.tvDayHeader);
            holder.tvTotalIncome = convertView.findViewById(R.id.tvTotalIncome);
            holder.tvTotalExpense = convertView.findViewById(R.id.tvTotalExpense);
            holder.ivIcon = convertView.findViewById(R.id.ivIcon);
            holder.tvCategory = convertView.findViewById(R.id.tvCategory);
            holder.tvDescription = convertView.findViewById(R.id.tvDescription);
            holder.tvAmount = convertView.findViewById(R.id.tvAmount);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // Header ngày chỉ hiển thị lần đầu tiên
        holder.tvDayHeader.setVisibility(position == 0 ? View.VISIBLE : View.GONE);
        holder.tvTotalIncome.setVisibility(position == 0 ? View.VISIBLE : View.GONE);
        holder.tvTotalExpense.setVisibility(position == 0 ? View.VISIBLE : View.GONE);

        holder.ivIcon.setImageResource(entry.getIconResId());
        holder.tvCategory.setText(entry.getCategory());
        holder.tvDescription.setText(entry.getDescription());

        holder.tvAmount.setText((entry.isExpense() ? "-" : "+") + df.format(entry.getAmount()));
        holder.tvAmount.setTextColor(entry.isExpense() ?
                context.getColor(R.color.red) : context.getColor(R.color.teal_700));

        return convertView;
    }

    static class ViewHolder {
        TextView tvDayHeader, tvTotalIncome, tvTotalExpense;
        ImageView ivIcon;
        TextView tvCategory, tvDescription, tvAmount;
    }
}