package com.example.app6hu.Fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app6hu.Activities.AddHocPhiActivity;
import com.example.app6hu.Activities.ChonTruongActivity;
import com.example.app6hu.Adapter.HocPhiAdapter;
import com.example.app6hu.R;
import com.example.app6hu.firebase.RealtimeDatabaseManager;
import com.example.app6hu.model.HocPhi;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class HocPhiFragment extends Fragment {

    private RecyclerView recyclerView;
    private HocPhiAdapter adapter;
    private ArrayList<HocPhi> list = new ArrayList<>();

    private Button btnTrangSinhVien, btnThemHocPhi;
    private TextView tvTongTien;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_hoc_phi, container, false);

        recyclerView = view.findViewById(R.id.recyclerHocPhi);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        btnTrangSinhVien = view.findViewById(R.id.btnTrangSinhVien);
        btnThemHocPhi = view.findViewById(R.id.btnThemHocPhi);
        tvTongTien = view.findViewById(R.id.tvTongTien);

        adapter = new HocPhiAdapter(getContext(), list);
        recyclerView.setAdapter(adapter);

        btnTrangSinhVien.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), ChonTruongActivity.class));
        });

        btnThemHocPhi.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddHocPhiActivity.class);
            startActivity(intent);
        });

        loadHocPhi();

        return view;
    }

    private void loadHocPhi() {
        DatabaseReference ref = RealtimeDatabaseManager.db()
                .child("hoc_phi");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                double tongTien = 0;

                for (DataSnapshot s : snapshot.getChildren()) {
                    HocPhi hp = s.getValue(HocPhi.class);
                    if (hp != null) {
                        hp.setId(s.getKey()); // ✅ DÒNG QUAN TRỌNG
                        list.add(hp);

                        if (!"DA_DONG".equals(hp.getTrangThai())) {
                            tongTien += hp.getSoTien();
                        }
                    }
                }

                adapter.notifyDataSetChanged();
                tvTongTien.setText("Tổng tiền cần đóng: " + tongTien + " đ");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}
