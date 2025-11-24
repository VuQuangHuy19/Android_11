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
    private OnItemClickListener listener;

    // Listener cho click
    public interface OnItemClickListener {
        void onCategoryClick(DanhMuc item);
        void onAddClick();
    }

    public DanhMucAdapter(List<DanhMuc> danhMucList, OnItemClickListener listener) {
        this.danhMucList = danhMucList;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return danhMucList.get(position).getViewType();
    }

    // Holder danh mục
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
            if (item.getColor() != 0) itemIcon.setColorFilter(item.getColor());
            itemName.setText(item.getItemName());
        }
    }

    // Holder nút thêm
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

        DanhMuc item = danhMucList.get(position);

        if (holder instanceof CategoryViewHolder) {
            CategoryViewHolder vh = (CategoryViewHolder) holder;
            vh.bind(item);

            vh.itemView.setOnClickListener(v -> {
                if (listener != null) listener.onCategoryClick(item);
            });

        } else if (holder instanceof AddViewHolder) {
            AddViewHolder vh = (AddViewHolder) holder;
            vh.bind(item);

            vh.itemView.setOnClickListener(v -> {
                if (listener != null) listener.onAddClick();
            });
        }
    }

    @Override
    public int getItemCount() {
        return danhMucList == null ? 0 : danhMucList.size();
    }

    public void removeItem(int position) {
        danhMucList.remove(position);
        notifyItemRemoved(position);
    }
}
