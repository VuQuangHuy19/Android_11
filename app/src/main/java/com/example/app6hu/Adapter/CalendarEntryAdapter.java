package com.example.app6hu.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.app6hu.Activities.EditTransactionActivity;
import com.example.app6hu.R;
import com.example.app6hu.model.Calendars;
import com.example.app6hu.model.Transaction;
import com.example.app6hu.utils.FormatUtils;

import java.util.List;

public class CalendarEntryAdapter extends BaseAdapter {

    private final Context context;
    private final List<Calendars> entries;
    private final LayoutInflater inflater;
    private long lastClickTime = 0;
    private int lastClickPosition = -1;

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

        String formattedAmount = FormatUtils.formatCurrency(entry.getAmount());
        if (entry.isExpense()) {
            holder.tvAmount.setText("-" + formattedAmount);
            holder.tvAmount.setTextColor(context.getColor(R.color.red));
        } else {
            holder.tvAmount.setText("+" + formattedAmount);
            holder.tvAmount.setTextColor(context.getColor(android.R.color.holo_green_dark));
        }

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long clickTime = System.currentTimeMillis();

                // Kiểm tra double click
                if (lastClickPosition == position && clickTime - lastClickTime < 400) {
                    // Double click - chuyển sang EditTransaction
                    openEditTransaction(entry);
                } else {
                    // Single click - có thể thêm hiệu ứng hoặc xử lý khác
                    Toast.makeText(context, "Click: " + entry.getDescription(), Toast.LENGTH_SHORT).show();
                }

                lastClickTime = clickTime;
                lastClickPosition = position;
            }
        });

        return convertView;
    }

    private boolean shouldShowHeader(int position, int currentDay) {
        if (position == 0) return true;

        Calendars previousEntry = entries.get(position - 1);
        return previousEntry.getDay() != currentDay;
    }

    private void openEditTransaction(Calendars calendarEntry) {
        // Tạo Transaction object từ Calendars
        Transaction transaction = new Transaction();
        transaction.setType(calendarEntry.isExpense() ? "EXPENSE" : "INCOME");
        transaction.setAmount(calendarEntry.getAmount());
        transaction.setCategory(calendarEntry.getCategory());
        transaction.setDetail(calendarEntry.getDescription());

        // QUAN TRỌNG: Truyền thêm các thông tin cần thiết
        Intent intent = new Intent(context, EditTransactionActivity.class);

        // TRUYỀN ĐẦY ĐỦ THÔNG TIN NHƯ TRONG CalendarFragment
        intent.putExtra("transaction_document_id", calendarEntry.getDocumentId());
        intent.putExtra("transaction_id", calendarEntry.getTransactionId());
        intent.putExtra("transaction_type", calendarEntry.isExpense() ? "EXPENSE" : "INCOME");
        intent.putExtra("transaction_amount", calendarEntry.getAmount());
        intent.putExtra("transaction_category", calendarEntry.getCategory());
        intent.putExtra("transaction_detail", calendarEntry.getDescription());
        intent.putExtra("transaction_day", calendarEntry.getDay());

        // Thêm transaction_date nếu có
        if (calendarEntry.getTransactionDate() != null) {
            intent.putExtra("transaction_date", calendarEntry.getTransactionDate().getTime());
        }

        // Thêm thông tin tháng/năm hiện tại nếu cần
        // (Có thể lấy từ Fragment hoặc truyền qua constructor)

        context.startActivity(intent);

        // Thông báo double click
        Toast.makeText(context, "Double click: Edit transaction", Toast.LENGTH_SHORT).show();
    }

    static class ViewHolder {
        TextView tvDayHeader, tvTotalIncome, tvTotalExpense;
        View layoutEntry;
        ImageView ivIcon;
        TextView tvCategory, tvDescription, tvAmount;
    }
}