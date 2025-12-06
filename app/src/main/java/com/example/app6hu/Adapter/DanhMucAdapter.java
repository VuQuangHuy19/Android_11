package com.example.app6hu.Adapter;

import android.content.Context;
import android.util.Log;
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
    private static final String TAG = "DanhMucAdapter";

    private final List<DanhMuc> danhMucList;
    private final OnDanhMucClickListener listener;
    private int selectedPosition = -1;
    private final Context context;

    public interface OnDanhMucClickListener {
        void onCategoryClick(DanhMuc item);
        void onAddClick();
    }

    public DanhMucAdapter(Context context, List<DanhMuc> danhMucList, OnDanhMucClickListener listener) {
        this.context = context;
        this.danhMucList = danhMucList;
        this.listener = listener;
    }

    // Thêm method này để xóa selection
    public void clearSelection() {
        int prevPosition = selectedPosition;
        selectedPosition = -1;
        if (prevPosition != -1) {
            notifyItemChanged(prevPosition);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return danhMucList.get(position).getViewType();
    }

    // ========== CATEGORY HOLDER ==========
    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        ImageView itemIcon;
        TextView itemName;
        View cardView;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView;
            itemIcon = itemView.findViewById(R.id.item_icon);
            itemName = itemView.findViewById(R.id.item_name);
        }

        public void bind(Context ctx, DanhMuc item, boolean isSelected) {
            // Load icon từ resourcesID
            if (item.getResourcesID() != 0) {
                itemIcon.setImageResource(item.getResourcesID());
            } else {
                itemIcon.setImageResource(R.drawable.ic_logo);
            }

            // Màu icon
            if (item.getColor() != 0) {
                itemIcon.setColorFilter(item.getColor());
            } else {
                itemIcon.clearColorFilter();
            }

            itemName.setText(item.getItemName());

            // UI khi selected
            if (isSelected) {
                cardView.setBackgroundResource(R.drawable.bg_danh_muc_selected);
                itemName.setTextColor(ctx.getColor(R.color.primary_color));
            } else {
                cardView.setBackgroundResource(R.drawable.bg_danh_muc_item);
                itemName.setTextColor(ctx.getColor(android.R.color.black));
            }
        }
    }

    // ========== ADD HOLDER ==========
    public static class AddViewHolder extends RecyclerView.ViewHolder {
        ImageView itemIcon;
        TextView itemName;

        public AddViewHolder(@NonNull View itemView) {
            super(itemView);
            itemIcon = itemView.findViewById(R.id.item_icon);
            itemName = itemView.findViewById(R.id.item_name);
        }

        public void bind() {
            itemIcon.setImageResource(R.drawable.ic_add);
            itemName.setText("Thêm mới");
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == DanhMuc.TYPE_CATEGORY) {
            View view = inflater.inflate(R.layout.item_danh_muc, parent, false);
            return new CategoryViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_add, parent, false);
            return new AddViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Log.d("DanhMucAdapter", "onBindViewHolder position: " + position);
        DanhMuc item = danhMucList.get(position);

        if (holder instanceof CategoryViewHolder) {
            CategoryViewHolder vh = (CategoryViewHolder) holder;
            boolean isSelected = (selectedPosition == position);
            vh.bind(context, item, isSelected);

            vh.itemView.setOnClickListener(v -> {
                int pos = vh.getAdapterPosition();
                if (pos == RecyclerView.NO_POSITION) return;

                int prev = selectedPosition;
                selectedPosition = pos;

                if (prev != -1) notifyItemChanged(prev);
                notifyItemChanged(pos);

                listener.onCategoryClick(danhMucList.get(pos));
            });

        } else if (holder instanceof AddViewHolder) {
            AddViewHolder vh = (AddViewHolder) holder;
            vh.bind();

            vh.itemView.setOnClickListener(v -> listener.onAddClick());
        }
    }

    @Override
    public int getItemCount() {
        return danhMucList.size();
    }

    public void updateData(List<DanhMuc> newList) {
        danhMucList.clear();
        danhMucList.addAll(newList);
        selectedPosition = -1;
        notifyDataSetChanged();
    }
}