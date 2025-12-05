package com.example.app6hu.Fragments;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

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

public class ExpenseFragment extends Fragment {

    private FirebasestoreManager firestoreManager;
    private Calendar selectedDate;
    private EditText edtDate, edtNote, edtAmount;
    private Button btnAddExpense;
    private String selectedCategory = "";
    private DanhMucAdapter adapter;
    private List<DanhMuc> categoryList;

    public ExpenseFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_expense, container, false);

        firestoreManager = new FirebasestoreManager();
        selectedDate = Calendar.getInstance();

        // Ánh xạ các view
        edtDate = view.findViewById(R.id.chartDateSet);
        edtNote = view.findViewById(R.id.chartWriteSet);
        edtAmount = view.findViewById(R.id.chartMoneySet);
        btnAddExpense = view.findViewById(R.id.add_over);
        
        RecyclerView recyclerView = view.findViewById(R.id.recycle_view_danh_muc);

        setupDatePicker();
        setupRecycler(recyclerView);
        setupAddButton();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Tải lại danh mục khi quay lại fragment
        loadCategoriesFromFirebase();
    }
    
    private void setupDatePicker() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        edtDate.setText(sdf.format(selectedDate.getTime()));
        
        View.OnClickListener dateClickListener = v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    selectedDate.set(year, month, dayOfMonth);
                    edtDate.setText(sdf.format(selectedDate.getTime()));
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        };
        
        edtDate.setOnClickListener(dateClickListener);
        edtDate.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                dateClickListener.onClick(v);
            }
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
                firestoreManager.addTransaction(transaction);
                
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
    }

    private void setupRecycler(RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));

        categoryList = new ArrayList<>();
        adapter = new DanhMucAdapter(
                categoryList,
                new DanhMucAdapter.OnItemClickListener() {
                    @Override
                    public void onCategoryClick(DanhMuc item) {
                        // Lưu danh mục đã chọn
                        selectedCategory = item.getItemName();
                        Toast.makeText(getContext(), "Đã chọn: " + selectedCategory, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onAddClick() {
                        Intent intent = new Intent(getActivity(), ThemDanhMucActivity.class);
                        startActivity(intent);
                    }
                }
        );

        recyclerView.setAdapter(adapter);
        
        // Lấy danh mục từ Firebase
        loadCategoriesFromFirebase();
    }
    
    private void loadCategoriesFromFirebase() {
        firestoreManager.getCategories(new FirebasestoreManager.OnCategoriesLoadedListener() {
            @Override
            public void onCategoriesLoaded(List<DanhMuc> categories) {
                categoryList.clear();
                categoryList.addAll(categories);
                // Thêm nút "Thêm" vào cuối danh sách
                categoryList.add(new DanhMuc("Thêm", R.drawable.ic_plus, DanhMuc.TYPE_ADD));
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(getContext(), "Lỗi tải danh mục: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
