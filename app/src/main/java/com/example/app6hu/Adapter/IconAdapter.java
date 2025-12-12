package com.example.app6hu.Adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app6hu.R;

import java.util.List;

public class IconAdapter extends RecyclerView.Adapter<IconAdapter.IconViewHolder> {

    private Context context;
    private List<String> iconList;
    private String selectedIcon;
    private OnIconClickListener listener;

    public interface OnIconClickListener {
        void onIconClick(String iconName);
    }

    public IconAdapter(Context context, List<String> iconList, String selectedIcon, OnIconClickListener listener) {
        this.context = context;
        this.iconList = iconList;
        this.selectedIcon = selectedIcon;
        this.listener = listener;
    }

    @NonNull
    @Override
    public IconViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_icon, parent, false);
        return new IconViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IconViewHolder holder, int position) {
        String iconName = iconList.get(position);

        // Get resource ID from icon name
        int resId = context.getResources().getIdentifier(iconName, "drawable", context.getPackageName());
        if (resId != 0) {
            holder.imgIcon.setImageResource(resId);
        }

        // Highlight selected icon
        if (iconName.equals(selectedIcon)) {
            holder.imgIcon.setBackgroundResource(R.drawable.bg_selected_icon);
        } else {
            holder.imgIcon.setBackgroundResource(R.drawable.bg_unselected_icon);
        }

        holder.itemView.setOnClickListener(v -> {
            selectedIcon = iconName;
            notifyDataSetChanged();
            if (listener != null) {
                listener.onIconClick(iconName);
            }
        });
    }

    @Override
    public int getItemCount() {
        return iconList.size();
    }

    public static class IconViewHolder extends RecyclerView.ViewHolder {
        ImageView imgIcon;

        public IconViewHolder(@NonNull View itemView) {
            super(itemView);
            imgIcon = itemView.findViewById(R.id.imgIcon);
        }
    }
}