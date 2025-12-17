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
import com.example.app6hu.Activities.EditHocPhiActivity;
import com.example.app6hu.R;
import com.example.app6hu.model.HocPhi;
import com.example.app6hu.utils.MoneyUtils;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class HocPhiAdapter extends RecyclerView.Adapter<HocPhiAdapter.ViewHolder> {

    private ArrayList<HocPhi> list;
    private Context context;

    public HocPhiAdapter(Context context, ArrayList<HocPhi> list) {
        this.context = context;
        this.list = list;
    }
    public void upDateList(ArrayList<HocPhi> newList){
        this.list= newList;
        notifyDataSetChanged();
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
            h.tvTrangThai.setTextColor(0xFF4CAF50);
        } else {
            h.tvTrangThai.setText("CHƯA ĐÓNG");
            h.tvTrangThai.setTextColor(0xFFE53935);
        }


        h.itemView.setOnClickListener(v -> {

            int pos = h.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;

            HocPhi currentHp = list.get(pos);

            if ("DA_DONG".equals(currentHp.getTrangThai())) {
                Toast.makeText(h.itemView.getContext(),
                        "Môn này đã đóng rồi!", Toast.LENGTH_SHORT).show();
                return;
            }

            new AlertDialog.Builder(h.itemView.getContext())
                    .setTitle("Xác nhận đóng học phí")
                    .setMessage("Bạn có chắc muốn đóng học phí cho:\n" + currentHp.getTenHocPhi() + "?")
                    .setPositiveButton("ĐÓNG", (dialog, which) -> {

                        FirebaseFirestore.getInstance()
                                .collection("hoc_phi")
                                .document(currentHp.getId())
                                .update("trangThai", "DA_DONG")
                                .addOnSuccessListener(unused -> {

                                    // ✅ Cập nhật dữ liệu local
                                    currentHp.setTrangThai("DA_DONG");

                                    // ✅ Cập nhật UI
                                    notifyItemChanged(pos);

                                    Toast.makeText(h.itemView.getContext(),
                                            "✅ Đã đóng học phí!", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(h.itemView.getContext(),
                                                "❌ Lỗi: " + e.getMessage(),
                                                Toast.LENGTH_SHORT).show()
                                );
                    })
                    .setNegativeButton("HỦY", null)
                    .show();
        });
        h.itemView.setOnLongClickListener(v -> {

            String[] options = {"Sửa", "Xóa"};

            new AlertDialog.Builder(context)
                    .setTitle("Chọn hành động")
                    .setItems(options, (dialog, which) -> {


                        if (which == 0) {
                            Intent intent = new Intent(context, EditHocPhiActivity.class);
                            intent.putExtra("DOC_ID", hp.getId()); // documentId Firestore
                            context.startActivity(intent);

                        }


                        if (which == 1) {
                            FirebaseFirestore.getInstance()
                                    .collection("hoc_phi")
                                    .document(hp.getId())
                                    .delete()
                                    .addOnSuccessListener(unused ->
                                            Toast.makeText(context,
                                                    "Đã xóa học phí",
                                                    Toast.LENGTH_SHORT).show()
                                    )
                                    .addOnFailureListener(e ->
                                            Toast.makeText(context,
                                                    "Xóa thất bại",
                                                    Toast.LENGTH_SHORT).show()
                                    );
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
