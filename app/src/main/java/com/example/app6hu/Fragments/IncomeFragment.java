package com.example.app6hu.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app6hu.Activities.ThemDanhMucActivity;
import com.example.app6hu.Adapter.DanhMucAdapter;
import com.example.app6hu.R;
import com.example.app6hu.firebase.FirebasestoreManager;
import com.example.app6hu.model.DanhMuc;
import com.example.app6hu.model.Transaction;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class IncomeFragment extends Fragment {

    private List<DanhMuc> danhMucList = new ArrayList<>();
    private DanhMucAdapter adapter;
    private Calendar selectedDate;
    private EditText edtDate, edtNote, edtAmount;
    private String selectedCategory = "";

    private Button btnAddIncome;

    private FirebasestoreManager fr = new FirebasestoreManager();

    public IncomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_income, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.rvDanhMuc);
        edtDate = view.findViewById(R.id.chartDateSet);
        edtNote = view.findViewById(R.id.chartWriteSet);
        edtAmount = view.findViewById(R.id.chartMoneySet);
        btnAddIncome = view.findViewById(R.id.add_over);

        setupRecycler(recyclerView);
        loadDanhMucFromFirestore();

        return view;
    }

    private void setupRecycler(RecyclerView recyclerView) {
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 4);
        recyclerView.setLayoutManager(layoutManager);

        // SỬA Ở ĐÂY: Context trước, danhMucList sau
        adapter = new DanhMucAdapter(requireContext(), danhMucList, new DanhMucAdapter.OnDanhMucClickListener() {
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
    private void setupAddButton() {
        btnAddIncome.setOnClickListener(v -> {
            String amountStr = edtAmount.getText().toString().trim();
            String note = edtNote.getText().toString().trim();

            // Kiểm tra danh mục đã chọn
            if (selectedCategory.isEmpty()) {
                Toast.makeText(getContext(), "⚠️ Vui lòng chọn danh mục", Toast.LENGTH_SHORT).show();
                return;
            }

            // Kiểm tra số tiền
            if (amountStr.isEmpty()) {
                Toast.makeText(getContext(), "⚠️ Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show();
                edtAmount.requestFocus();
                return;
            }

            try {
                double amount = Double.parseDouble(amountStr);

                if (amount <= 0) {
                    Toast.makeText(getContext(), "⚠️ Số tiền phải lớn hơn 0", Toast.LENGTH_SHORT).show();
                    edtAmount.requestFocus();
                    return;
                }

                // Tạo transaction mới
                Transaction transaction = new Transaction();
                transaction.setType("INCOME");
                transaction.setAmount(amount);
                transaction.setCategory(selectedCategory);
                transaction.setDetail(note.isEmpty() ? "Không có ghi chú" : note);
                transaction.setDate(selectedDate.getTime());
                transaction.setIcon("💰");

                // Hiển thị loading toast
                Toast.makeText(getContext(), "⏳ Đang lưu...", Toast.LENGTH_SHORT).show();

                // Lưu lên Firebase
                fr.addTransaction(transaction);

                // Thông báo thành công
                String formattedAmount = String.format("%,.0f đ", amount);
                Toast.makeText(getContext(),
                        "✅ Đã thêm khoản thu " + formattedAmount + " thành công!",
                        Toast.LENGTH_LONG).show();

                // Reset form
                clearForm();

            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "⚠️ Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
                edtAmount.requestFocus();
            }
        });
    }

    private void clearForm() {
        edtAmount.setText("");
        edtNote.setText("");
        selectedDate = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        edtDate.setText(sdf.format(selectedDate.getTime()));
        selectedCategory = "";
    }

    private List<DanhMuc> prepareDummyData() {
        List<DanhMuc> list = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            list.add(new DanhMuc("Chi tiêu " + i, R.drawable.ic_logo, DanhMuc.TYPE_CATEGORY));
        }
        list.add(new DanhMuc("Thêm", R.drawable.ic_plus, DanhMuc.TYPE_ADD));
        return list;
    }
    @Override
    public void onResume() {
        super.onResume();
        loadDanhMucFromFirestore();
    }
}