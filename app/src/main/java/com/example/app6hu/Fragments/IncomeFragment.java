package com.example.app6hu.Fragments;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.app6hu.Activities.ThemDanhMucActivity;
import com.example.app6hu.Adapter.DanhMucAdapter;
import com.example.app6hu.R;
import com.example.app6hu.model.DanhMuc;

import java.util.ArrayList;
import java.util.List;

public class IncomeFragment extends Fragment {

    public IncomeFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_income, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recycle_view_danh_muc);

        setupRecycler(recyclerView);

        return view;
    }

    private void setupRecycler(RecyclerView recyclerView) {

        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));

        DanhMucAdapter adapter = new DanhMucAdapter(
                prepareDummyData(),
                new DanhMucAdapter.OnItemClickListener() {
                    @Override
                    public void onCategoryClick(DanhMuc item) {
                        // Xử lý nhấp vào danh mục Thu nhập
                    }

                    @Override
                    public void onAddClick() {
                        Intent intent = new Intent(getActivity(), ThemDanhMucActivity.class);
                        startActivity(intent);
                    }
                }
        );

        recyclerView.setAdapter(adapter);
    }
    private List<DanhMuc> prepareDummyData() {
        List<DanhMuc> list = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            list.add(new DanhMuc("Chi tiêu " + i, R.drawable.ic_logo, DanhMuc.TYPE_CATEGORY));
        }
        list.add(new DanhMuc("Thêm", R.drawable.ic_plus, DanhMuc.TYPE_ADD));
        return list;
    }

}
