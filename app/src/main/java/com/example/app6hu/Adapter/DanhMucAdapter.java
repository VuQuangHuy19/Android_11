package com.example.app6hu.Adapter;

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

public class DanhMucAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<DanhMuc> danhMucList;

    public DanhMucAdapter(List<DanhMuc> danhMucList) {
        this.danhMucList = danhMucList;
    }

    // --- Xác định loại view ---
    @Override
    public int getItemViewType(int position) {
        return danhMucList.get(position).getViewType();
    }

    // --- ViewHolder cho loại danh mục bình thường ---
    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        ImageView itemIcon;
        TextView itemName;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            itemIcon = itemView.findViewById(R.id.item_icon);
            itemName = itemView.findViewById(R.id.item_name);
        }

        public void bind(DanhMuc item) {
            itemIcon.setImageResource(item.getResourcesID());
            itemName.setText(item.getItemName());
        }
    }

    // --- ViewHolder cho loại "Thêm" ---
    public static class AddViewHolder extends RecyclerView.ViewHolder {
        ImageView itemIcon;
        TextView itemName;

        public AddViewHolder(@NonNull View itemView) {
            super(itemView);
            itemIcon = itemView.findViewById(R.id.item_icon);
            itemName = itemView.findViewById(R.id.item_name);
        }

        public void bind(DanhMuc item) {
            itemIcon.setImageResource(item.getResourcesID());
            itemName.setText(item.getItemName());
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view;
        if (viewType == DanhMuc.TYPE_CATEGORY) {
            view = inflater.inflate(R.layout.item_danh_muc, parent, false);
            return new CategoryViewHolder(view);
        } else {
            view = inflater.inflate(R.layout.item_add, parent, false);
            return new AddViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        DanhMuc hienTai = danhMucList.get(position);
        if (holder instanceof CategoryViewHolder) {
            ((CategoryViewHolder) holder).bind(hienTai);
        } else if (holder instanceof AddViewHolder) {
            ((AddViewHolder) holder).bind(hienTai);
        }
    }

    @Override
    public int getItemCount() {
        return danhMucList != null ? danhMucList.size() : 0;
    }
}

