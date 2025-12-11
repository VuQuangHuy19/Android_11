package com.example.app6hu.Adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app6hu.R;
import com.example.app6hu.model.Truong;

import java.util.ArrayList;

public class TruongAdapter extends RecyclerView.Adapter<TruongAdapter.ViewHolder> {

    private Context context;
    private ArrayList<Truong> list;

    public TruongAdapter(Context context, ArrayList<Truong> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_truong, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Truong t = list.get(position);

        h.tvTen.setText(t.getTenTruong());
        h.imgLogo.setImageResource(t.getLogo());

        h.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(t.getLink()));
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgLogo;
        TextView tvTen;

        public ViewHolder(@NonNull View v) {
            super(v);
            imgLogo = v.findViewById(R.id.imgLogo);
            tvTen = v.findViewById(R.id.tvTenTruong);
        }
    }
}
