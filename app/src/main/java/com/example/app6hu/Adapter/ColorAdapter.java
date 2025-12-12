package com.example.app6hu.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app6hu.R;

import java.util.List;

public class ColorAdapter extends RecyclerView.Adapter<ColorAdapter.ColorViewHolder> {

    private Context context;
    private List<String> colorList;
    private String selectedColor;
    private OnColorClickListener listener;

    public interface OnColorClickListener {
        void onColorClick(String color);
    }

    public ColorAdapter(Context context, List<String> colorList, String selectedColor, OnColorClickListener listener) {
        this.context = context;
        this.colorList = colorList;
        this.selectedColor = selectedColor;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ColorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_color, parent, false);
        return new ColorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ColorViewHolder holder, int position) {
        String color = colorList.get(position);

        // Set color
        holder.colorView.setBackgroundColor(Color.parseColor(color));

        // Show/hide checkmark for selected color
        if (color.equals(selectedColor)) {
            holder.imgCheck.setVisibility(View.VISIBLE);
        } else {
            holder.imgCheck.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            selectedColor = color;
            notifyDataSetChanged();
            if (listener != null) {
                listener.onColorClick(color);
            }
        });
    }

    @Override
    public int getItemCount() {
        return colorList.size();
    }

    public static class ColorViewHolder extends RecyclerView.ViewHolder {
        View colorView;
        ImageView imgCheck;

        public ColorViewHolder(@NonNull View itemView) {
            super(itemView);
            colorView = itemView.findViewById(R.id.colorView);
            imgCheck = itemView.findViewById(R.id.imgCheck);
        }
    }
}