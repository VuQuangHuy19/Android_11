package com.example.app6hu.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app6hu.R;
import com.example.app6hu.model.CourseFee;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CourseFeeAdapter extends RecyclerView.Adapter<CourseFeeAdapter.CourseFeeViewHolder> {

    private final List<CourseFee> courseFeeList;
    private final NumberFormat currencyFormatter;

    public CourseFeeAdapter(List<CourseFee> courseFeeList) {
        this.courseFeeList = courseFeeList;
        // Thiết lập định dạng tiền tệ (Ví dụ: VNĐ)
        currencyFormatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
    }

    @NonNull
    @Override
    public CourseFeeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Sử dụng layout item_course_fee.xml
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_course_entry, parent, false);
        return new CourseFeeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseFeeViewHolder holder, int position) {
        CourseFee course = courseFeeList.get(position);

        holder.nameTextView.setText(course.getName());

        // Định dạng số tiền và hiển thị
        String formattedPrice = currencyFormatter.format(course.getPrice()) + " VNĐ";
        holder.priceTextView.setText(formattedPrice);
    }

    @Override
    public int getItemCount() {
        return courseFeeList.size();
    }

    public static class CourseFeeViewHolder extends RecyclerView.ViewHolder {
        public TextView nameTextView;
        public TextView priceTextView;

        public CourseFeeViewHolder(View itemView) {
            super(itemView);
            // Ánh xạ các ID từ item_course_fee.xml
            nameTextView = itemView.findViewById(R.id.tv_course_name_item);
            priceTextView = itemView.findViewById(R.id.tv_course_price_item);
        }
    }
}
