package com.example.app6hu.Fragments;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ExpenseFragment extends Fragment {

    private List<DanhMuc> danhMucList = new ArrayList<>();
    private DanhMucAdapter adapter;
    private Calendar selectedDate;
    private EditText edtDate, edtNote, edtAmount;
    private Button btnAddExpense;
    private String selectedCategory = "";
    private String selectedCategoryId = "";
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

        edtDate = view.findViewById(R.id.chartDateSet);
        edtNote = view.findViewById(R.id.chartWriteSet);
        edtAmount = view.findViewById(R.id.chartMoneySet);
        btnAddExpense = view.findViewById(R.id.add_over);

        // Setup các nút
        setupDatePicker();
        setupAddButton();

        return view;
    }

    private void setupDatePicker() {
        // Khởi tạo ngày mặc định là hôm nay
        selectedDate = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        edtDate.setText(sdf.format(selectedDate.getTime()));

        edtDate.setOnClickListener(v -> {
            // Material Date Picker
            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Chọn ngày")
                    .setSelection(selectedDate.getTimeInMillis())
                    .setTheme(R.style.MaterialDatePickerTheme)
                    .build();

            datePicker.addOnPositiveButtonClickListener(selection -> {
                selectedDate.setTimeInMillis(selection);
                edtDate.setText(sdf.format(selectedDate.getTime()));
            });

            datePicker.show(getParentFragmentManager(), "DATE_PICKER");
            Toast.makeText(getContext(), "Chọn ngày", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupAddButton() {
        btnAddExpense.setOnClickListener(v -> {
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
                transaction.setType("EXPENSE");
                transaction.setAmount(amount);
                transaction.setCategory(selectedCategory);


                transaction.setDetail(note.isEmpty() ? "Không có ghi chú" : note);
                transaction.setDate(selectedDate.getTime());
                transaction.setIcon("💸");

                // Hiển thị loading toast
                Toast.makeText(getContext(), "⏳ Đang lưu...", Toast.LENGTH_SHORT).show();

                // Lưu lên Firebase
                fr.addTransaction(transaction);

                // Thông báo thành công
                String formattedAmount = String.format("%,.0f đ", amount);
                Toast.makeText(getContext(),
                        "✅ Đã thêm khoản chi " + formattedAmount + " thành công!",
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
        selectedCategoryId = "";

        // Reset selection trong adapter
        if (adapter != null) {
            adapter.clearSelection();
        }
    }

    private void setupRecycler(RecyclerView recyclerView) {
        Log.d("ExpenseFragment", "setupRecycler được gọi");

        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 4);
        recyclerView.setLayoutManager(layoutManager);

        Log.d("ExpenseFragment", "DanhMucList size khi setup: " + danhMucList.size());

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
        Log.d("ExpenseFragment", "Bắt đầu load danh mục từ Firestore...");

        fr.getDanhMucByType("expense", requireContext(), new FirebasestoreManager.FirestoreCallback<List<DanhMuc>>() {
            @Override
            public void onSuccess(List<DanhMuc> data) {
                Log.d("ExpenseFragment", "onSuccess được gọi");
                Log.d("ExpenseFragment", "Số lượng data nhận được: " + (data == null ? "null" : data.size()));

                danhMucList.clear();

                if (data != null && !data.isEmpty()) {
                    Log.d("ExpenseFragment", "Có dữ liệu, thêm vào list");
                    danhMucList.addAll(data);

                    // Debug chi tiết từng item
                    for (int i = 0; i < data.size(); i++) {
                        DanhMuc dm = data.get(i);
                        Log.d("ExpenseFragment", "Item " + i + ": " +
                                "Name=" + dm.getItemName() +
                                ", IconRes=" + dm.getResourcesID() +
                                ", ID=" + dm.getId() +
                                ", Type=" + dm.getType());
                    }
                } else {
                    Log.d("ExpenseFragment", "Không có dữ liệu hoặc data rỗng");
                }

                // Thêm item "Thêm mới"
                DanhMuc addItem = new DanhMuc(
                        "Thêm mới",
                        R.drawable.ic_add,
                        DanhMuc.TYPE_ADD,
                        0
                );
                danhMucList.add(addItem);

                Log.d("ExpenseFragment", "Tổng số item trong danhMucList: " + danhMucList.size());

                adapter.notifyDataSetChanged();
                Log.d("ExpenseFragment", "Đã gọi notifyDataSetChanged");
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("ExpenseFragment", "Lỗi tải danh mục: " + e.getMessage(), e);
                Toast.makeText(getContext(),
                        "Lỗi tải danh mục: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        });
    }

    private void handleCategoryClick(DanhMuc danhMuc) {
        // Xử lý khi click vào một danh mục
        if (danhMuc.getViewType() == DanhMuc.TYPE_CATEGORY) {
            // Lưu thông tin danh mục được chọn
            selectedCategory = danhMuc.getItemName();
            selectedCategoryId = danhMuc.getId();

            // Hiển thị feedback cho người dùng
            Toast.makeText(getContext(),
                    "Đã chọn: " + danhMuc.getItemName(),
                    Toast.LENGTH_SHORT).show();

            Log.d("ExpenseFragment", "Selected category: " + selectedCategory + ", ID: " + selectedCategoryId);

            // Cập nhật giao diện nếu cần
            // Không cần gọi adapter.setSelectedCategory() vì adapter tự xử lý highlight
        }
    }

    private void handleAddClick() {
        // Mở Activity thêm danh mục mới
        if (getActivity() != null) {
            Intent intent = new Intent(getActivity(), ThemDanhMucActivity.class);
            intent.putExtra("fromFragment", "expense");
            startActivity(intent);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Tải lại danh sách khi quay lại fragment
        if (adapter != null) {
            loadDanhMucFromFirestore();
        }
    }
}