package com.example.app6hu.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

public class ExpenseFragment extends Fragment {

    private List<DanhMuc> danhMucList = new ArrayList<>();
    private DanhMucAdapter adapter;
    private FirebasestoreManager fr = new FirebasestoreManager();

    public ExpenseFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_expense, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.rvDanhMuc);

        // Thiết lập RecyclerView
        setupRecycler(recyclerView);

        // Tải danh mục từ Firestore
        loadDanhMucFromFirestore();

        return view;
    }

    private void setupRecycler(RecyclerView recyclerView) {
        // Sử dụng GridLayoutManager với 4 cột
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 4);
        recyclerView.setLayoutManager(layoutManager);

        // Khởi tạo adapter - SỬA: Không cần Context trong constructor
        adapter = new DanhMucAdapter(danhMucList, new DanhMucAdapter.OnDanhMucClickListener() {
            @Override
            public void onCategoryClick(DanhMuc danhMuc) {
                // Xử lý khi click vào danh mục
                handleCategoryClick(danhMuc);
            }

            @Override
            public void onAddClick() {
                // Xử lý khi click vào nút "Thêm mới"
                handleAddClick();
            }
        });

        recyclerView.setAdapter(adapter);
    }

    private void loadDanhMucFromFirestore() {
        // THÊM requireContext() vào đây
        fr.getDanhMucByType("expense", requireContext(), new FirebasestoreManager.FirestoreCallback<List<DanhMuc>>() {
            @Override
            public void onSuccess(List<DanhMuc> data) {
                danhMucList.clear();

                if (data != null && !data.isEmpty()) {
                    danhMucList.addAll(data);
                    Log.d("ExpenseFragment", "Loaded " + data.size() + " expense categories");

                    // Debug: kiểm tra icon resource
                    for (DanhMuc dm : data) {
                        Log.d("ExpenseFragment", "Category: " + dm.getItemName() +
                                ", IconRes: " + dm.getResourcesID());
                    }
                }

                // Thêm item "Thêm mới"
                DanhMuc addItem = new DanhMuc(
                        "Thêm mới",
                        R.drawable.ic_add,  // Đảm bảo có drawable này
                        DanhMuc.TYPE_ADD,
                        0
                );
                danhMucList.add(addItem);

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getContext(),
                        "Lỗi tải danh mục: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        });
    }

    private void handleCategoryClick(DanhMuc danhMuc) {
        // Xử lý khi click vào một danh mục
        if (getActivity() != null) {
            // Tạo Intent để chọn danh mục
            Intent resultIntent = new Intent();
            resultIntent.putExtra("selectedDanhMucId", danhMuc.getId());
            resultIntent.putExtra("selectedDanhMucName", danhMuc.getItemName());

            // Trả về kết quả cho Activity cha (nếu cần)
            if (getActivity().getSupportFragmentManager().getBackStackEntryCount() > 0) {
                getActivity().getSupportFragmentManager().popBackStack();
            }

            // Hoặc mở ThemDanhMucActivity để chỉnh sửa
            Intent editIntent = new Intent(getActivity(), ThemDanhMucActivity.class);
            editIntent.putExtra("danhMucId", danhMuc.getId());
            editIntent.putExtra("danhMucName", danhMuc.getItemName());
            editIntent.putExtra("type", "expense");
            startActivity(editIntent);
        }
    }

    private void handleAddClick() {
        // Mở Activity thêm danh mục mới
        if (getActivity() != null) {
            Intent intent = new Intent(getActivity(), ThemDanhMucActivity.class);
            intent.putExtra("type", "expense"); // Truyền type để biết đây là danh mục chi tiêu
            startActivity(intent);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Tải lại danh sách khi quay lại fragment
        loadDanhMucFromFirestore();
    }
}