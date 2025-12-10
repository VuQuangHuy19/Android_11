package com.example.app6hu.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app6hu.Activities.EditTransactionActivity;
import com.example.app6hu.R;
import com.example.app6hu.model.Transaction;
import com.example.app6hu.utils.FormatUtils;

import java.util.List;

public class FindTransAdapter extends RecyclerView.Adapter<FindTransAdapter.ViewHolder> {

    private Context context;
    private List<Transaction> transactionList;
    private LayoutInflater inflater;

    public FindTransAdapter(Context context, List<Transaction> transactionList) {
        this.context = context;
        this.transactionList = transactionList;
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.display_transaction_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Transaction transaction = transactionList.get(position);

        // Hiển thị thông tin
        holder.tvCategory.setText(transaction.getCategory());
        holder.tvDetail.setText(transaction.getDetail());
        holder.tvDate.setText(FormatUtils.formatDate(transaction.getDate()));

        // Hiển thị số tiền với FormatUtils
        if ("INCOME".equalsIgnoreCase(transaction.getType())) {
            holder.tvAmount.setText(FormatUtils.formatCurrencyWithSign(transaction.getAmount()));
                holder.tvAmount.setTextColor(context.getColor(android.R.color.holo_green_dark));
            holder.ivIcon.setImageResource(R.drawable.ic_allowance); // Icon thu nhập
        } else {
            holder.tvAmount.setText(FormatUtils.formatCurrencyWithSign(-transaction.getAmount()));
            holder.tvAmount.setTextColor(context.getColor(android.R.color.holo_red_dark));
            holder.ivIcon.setImageResource(R.drawable.ic_bill); // Icon chi tiêu
        }

        // Xử lý click
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openEditTransaction(transaction);
            }
        });
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    public void updateList(List<Transaction> newList) {
        transactionList = newList;
        notifyDataSetChanged();
    }

    private void openEditTransaction(Transaction transaction) {
        Intent intent = new Intent(context, EditTransactionActivity.class);

        // Truyền toàn bộ thông tin transaction
        intent.putExtra("transaction_id", transaction.getId());
        intent.putExtra("transaction_type", transaction.getType());
        intent.putExtra("transaction_amount", transaction.getAmount());
        intent.putExtra("transaction_category", transaction.getCategory());
        intent.putExtra("transaction_detail", transaction.getDetail());
        intent.putExtra("transaction_date", transaction.getDate().getTime());

        context.startActivity(intent);

        Toast.makeText(context, "Mở chỉnh sửa: " + transaction.getCategory(), Toast.LENGTH_SHORT).show();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvCategory, tvDetail, tvDate, tvAmount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.ivIcon);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvDetail = itemView.findViewById(R.id.tvDetail);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvAmount = itemView.findViewById(R.id.tvAmount);
        }
    }
}