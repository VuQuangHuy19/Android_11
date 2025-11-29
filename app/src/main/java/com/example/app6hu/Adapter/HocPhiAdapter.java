package com.example.app6hu.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app6hu.Activities.AddHocPhiActivity;
import com.example.app6hu.R;
import com.example.app6hu.model.HocPhi;
import com.example.app6hu.utils.MoneyUtils;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class HocPhiAdapter extends RecyclerView.Adapter<HocPhiAdapter.ViewHolder> {

    private ArrayList<HocPhi> list;
    private Context context;

    public HocPhiAdapter(Context context, ArrayList<HocPhi> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_hoc_phi, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        HocPhi hp = list.get(position);

        h.tvTen.setText(hp.getTenHocPhi());
        h.tvTien.setText(MoneyUtils.format(hp.getSoTien()) + " đ");
        h.tvNgay.setText(hp.getNgayDong());


        if ("DA_DONG".equals(hp.getTrangThai())) {
            h.tvTrangThai.setText("ĐÃ ĐÓNG");
            h.tvTrangThai.setTextColor(0xFF4CAF50); // Xanh
        } else {
            h.tvTrangThai.setText("CHƯA ĐÓNG");
            h.tvTrangThai.setTextColor(0xFFE53935); // Đỏ
        }


        h.itemView.setOnClickListener(v -> {

            if ("DA_DONG".equals(hp.getTrangThai())) {
                Toast.makeText(context,
                        "Môn này đã đóng rồi!", Toast.LENGTH_SHORT).show();
                return;
            }

            new AlertDialog.Builder(context)
                    .setTitle("Xác nhận đóng học phí")
                    .setMessage("Bạn có chắc muốn đóng học phí cho:\n" + hp.getTenHocPhi() + "?")
                    .setPositiveButton("ĐÓNG", (dialog, which) -> {

                        FirebaseDatabase.getInstance()
                                .getReference("hoc_phi")
                                .child(hp.getId())
                                .child("trangThai")
                                .setValue("DA_DONG");

                        Toast.makeText(context,
                                "✅ Đã đóng học phí!", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("HỦY", null)
                    .show();
        });

        h.itemView.setOnLongClickListener(v -> {

            String[] options = {"✏ Sửa", "🗑 Xóa"};

            new AlertDialog.Builder(context)
                    .setTitle("Chọn hành động")
                    .setItems(options, (dialog, which) -> {

                        // ✅ SỬA
                        if (which == 0) {
                            Intent intent = new Intent(context, AddHocPhiActivity.class);
                            intent.putExtra("id", hp.getId());
                            intent.putExtra("ten", hp.getTenHocPhi());
                            intent.putExtra("tien", hp.getSoTien());
                            intent.putExtra("ngay", hp.getNgayDong());
                            intent.putExtra("trangThai", hp.getTrangThai());
                            context.startActivity(intent);
                        }

                        // ✅ XÓA
                        if (which == 1) {
                            FirebaseDatabase.getInstance()
                                    .getReference("hoc_phi")
                                    .child(hp.getId())
                                    .removeValue();

                            Toast.makeText(context,
                                    "✅ Đã xóa học phí", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .show();

            return true;
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTen, tvTien, tvNgay, tvTrangThai;

        public ViewHolder(@NonNull View v) {
            super(v);
            tvTen = v.findViewById(R.id.tvTenHocPhi);
            tvTien = v.findViewById(R.id.tvSoTien);
            tvNgay = v.findViewById(R.id.tvNgayDong);
            tvTrangThai = v.findViewById(R.id.tvTrangThai);
        }
    }
}
