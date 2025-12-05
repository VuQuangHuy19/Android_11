package com.example.app6hu.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app6hu.Activities.ThemDanhMucActivity;
import com.example.app6hu.Adapter.DanhMucAdapter;
import com.example.app6hu.R;
import com.example.app6hu.firebase.FirebasestoreManager;
import com.example.app6hu.model.DanhMuc;

import java.util.ArrayList;
import java.util.List;

public class IncomeFragment extends Fragment {

    private List<DanhMuc> danhMucList = new ArrayList<>();
    private DanhMucAdapter adapter;
    private FirebasestoreManager fr = new FirebasestoreManager();

    public IncomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_income, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.rvDanhMuc);

        setupRecycler(recyclerView);
        loadDanhMucFromFirestore();

        return view;
    }

    private void setupRecycler(RecyclerView recyclerView) {
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 4);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new DanhMucAdapter(danhMucList, new DanhMucAdapter.OnDanhMucClickListener() {
            @Override
            public void onCategoryClick(DanhMuc danhMuc) {
                handleCategoryClick(danhMuc);
            }

            @Override
            public void onAddClick() {
                handleAddClick();
            }
        });

        recyclerView.setAdapter(adapter);
    }

    private void loadDanhMucFromFirestore() {
        // THÊM requireContext() vào đây
        fr.getDanhMucByType("income", requireContext(), new FirebasestoreManager.FirestoreCallback<List<DanhMuc>>() {
            @Override
            public void onSuccess(List<DanhMuc> data) {
                danhMucList.clear();

                if (data != null && !data.isEmpty()) {
                    danhMucList.addAll(data);
                }

                // Thêm item "Thêm mới"
                DanhMuc addItem = new DanhMuc(
                        "Thêm mới",
                        R.drawable.ic_add,
                        DanhMuc.TYPE_ADD,
                        0
                );
                danhMucList.add(addItem);

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getContext(),
                        "Lỗi tải danh mục thu nhập: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleCategoryClick(DanhMuc danhMuc) {
        if (getActivity() != null) {
            Intent intent = new Intent(getActivity(), ThemDanhMucActivity.class);
            intent.putExtra("danhMucId", danhMuc.getId());
            intent.putExtra("danhMucName", danhMuc.getItemName());
            intent.putExtra("type", "income");
            startActivity(intent);
        }
    }

    private void handleAddClick() {
        if (getActivity() != null) {
            Intent intent = new Intent(getActivity(), ThemDanhMucActivity.class);
            intent.putExtra("type", "income"); // Truyền type "income"
            startActivity(intent);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadDanhMucFromFirestore();
    }
}