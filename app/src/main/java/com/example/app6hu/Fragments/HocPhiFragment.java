package com.example.app6hu.Fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app6hu.Activities.AddHocPhiActivity;
import com.example.app6hu.Activities.ChonTruongActivity;
import com.example.app6hu.Adapter.HocPhiAdapter;
import com.example.app6hu.R;
import com.example.app6hu.firebase.FirestoreHocPhiManager;
import com.example.app6hu.model.HocPhi;
import com.example.app6hu.utils.MoneyUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class HocPhiFragment extends Fragment {

    private EditText find;
    private RecyclerView recyclerView;
    private HocPhiAdapter adapter;
    private ArrayList<HocPhi> list = new ArrayList<>();
    private LinearLayout root;
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
        root=view.findViewById(R.id.trangChinh);
        adapter = new HocPhiAdapter(getContext(), list);
        recyclerView.setAdapter(adapter);
        find = view.findViewById(R.id.find);
        btnTrangSinhVien.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), ChonTruongActivity.class));
        });

        btnThemHocPhi.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddHocPhiActivity.class);
            startActivity(intent);
        });
        root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                find.clearFocus();
            }
        });
        loadHocPhi();

        find.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }
        });

        return view;
    }

    private void filter(String keyword){
        ArrayList<HocPhi> newList= new ArrayList<>();
        for(HocPhi i:list){
            if(i.getTenHocPhi().toLowerCase().contains(keyword.toLowerCase())){
                newList.add(i);
            }
        }
        adapter.upDateList(newList);
    }
    private void loadHocPhi() {
        new FirestoreHocPhiManager().listenAll((data, tongTien) -> {
            list.clear();
            list.addAll(data);


            adapter.notifyDataSetChanged();
            String tienFormat= MoneyUtils.format(tongTien);
            tvTongTien.setText("Tổng tiền cần đóng: " + tienFormat + " đ");
        });
    }

}
