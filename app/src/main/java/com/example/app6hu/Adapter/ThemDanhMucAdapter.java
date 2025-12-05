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

        public void bind(DanhMuc item, OnItemClickListener listener, int position) {
            imgIcon.setImageResource(item.getResourcesID());
            if (item.getColor() != 0) imgIcon.setColorFilter(item.getColor());
            tvTenDanhMuc.setText(item.getItemName());

            itemView.setOnClickListener(v -> listener.onItemClick(item, position));
        }
    }

    @NonNull
    @Override
    public ThemDanhMucAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_danh_muc_1, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ThemDanhMucAdapter.ViewHolder holder, int position) {
        holder.bind(danhMucList.get(position), listener, position);
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
        }
    }
}
