package com.example.app6hu.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app6hu.R;
import com.example.app6hu.model.DanhMuc;

import java.util.List;

public class ThemDanhMucAdapter extends RecyclerView.Adapter<ThemDanhMucAdapter.ViewHolder> {

    private final List<DanhMuc> danhMucList;
    private final Context context;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(DanhMuc item, int position);
    }

    public ThemDanhMucAdapter(Context context, List<DanhMuc> danhMucList, OnItemClickListener listener) {
        this.context = context;
        this.danhMucList = danhMucList;
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgIcon;
        TextView tvTenDanhMuc;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgIcon = itemView.findViewById(R.id.imgIcon);
            tvTenDanhMuc = itemView.findViewById(R.id.tvTenDanhMuc);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_danh_muc_1, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DanhMuc danhMuc = danhMucList.get(position);

        // Hiển thị tên danh mục
        holder.tvTenDanhMuc.setText(danhMuc.getItemName());

        // Hiển thị icon với màu
        if (danhMuc.getResourcesID() != 0) {
            holder.imgIcon.setImageResource(danhMuc.getResourcesID());
        } else {
            holder.imgIcon.setImageResource(R.drawable.ic_logo);
        }

        // Áp dụng màu nếu có
        if (danhMuc.getColor() != 0) {
            holder.imgIcon.setColorFilter(danhMuc.getColor());
        } else {
            holder.imgIcon.clearColorFilter();
        }

        // Xử lý click
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(danhMuc, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return danhMucList == null ? 0 : danhMucList.size();
    }

    // Xóa danh mục
    public void removeItem(int position) {
        if (position >= 0 && position < danhMucList.size()) {
            danhMucList.remove(position);
            notifyItemRemoved(position);

            // Cập nhật các item sau vị trí xóa
            if (position < danhMucList.size()) {
                notifyItemRangeChanged(position, danhMucList.size() - position);
            }
        }
    }
}