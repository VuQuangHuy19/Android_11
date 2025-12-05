package com.example.app6hu.Fragments;

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
        return view;
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
    }
    private void setupRecycler(RecyclerView recyclerView) {
        Log.d("ExpenseFragment", "setupRecycler được gọi");

        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 4);
        recyclerView.setLayoutManager(layoutManager);

        Log.d("ExpenseFragment", "DanhMucList size khi setup: " + danhMucList.size());
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

//    private void loadDanhMucFromFirestore() {
//        // THÊM requireContext() vào đây
//        Log.d("ExpenseFragment", "Bắt đầu load danh mục từ Firestore...");
//        danhMucList.clear();
//
//        // Thêm một vài danh mục test
//        danhMucList.add(new DanhMuc("Ăn uống", R.drawable.ic_food, DanhMuc.TYPE_CATEGORY, 0));
//        danhMucList.add(new DanhMuc("Di chuyển", R.drawable.ic_transport, DanhMuc.TYPE_CATEGORY, 0));
//        danhMucList.add(new DanhMuc("Mua sắm", R.drawable.ic_allowance, DanhMuc.TYPE_CATEGORY, 0));
//
//        // Thêm item "Thêm mới"
//        DanhMuc addItem = new DanhMuc(
//                "Thêm mới",
//                R.drawable.ic_add,
//                DanhMuc.TYPE_ADD,
//                0
//        );
//        danhMucList.add(addItem);
//
//        Log.d("ExpenseFragment", "Test data: " + danhMucList.size() + " items");
//
//        adapter.notifyDataSetChanged();
////        fr.getDanhMucByType("EXPENSE", requireContext(), new FirebasestoreManager.FirestoreCallback<List<DanhMuc>>() {
////            @Override
////            public void onSuccess(List<DanhMuc> data) {
////                danhMucList.clear();
////
////                if (data != null && !data.isEmpty()) {
////                    danhMucList.addAll(data);
////                    Log.d("ExpenseFragment", "Loaded " + data.size() + " expense categories");
////
////                    // Debug: kiểm tra icon resource
////                    for (DanhMuc dm : data) {
////                        Log.d("ExpenseFragment", "Category: " + dm.getItemName() +
////                                ", IconRes: " + dm.getResourcesID());
////                    }
////                }
////
////                // Thêm item "Thêm mới"
////                DanhMuc addItem = new DanhMuc(
////                        "Thêm mới",
////                        R.drawable.ic_add,  // Đảm bảo có drawable này
////                        DanhMuc.TYPE_ADD,
////                        0
////                );
////                danhMucList.add(addItem);
////
////                adapter.notifyDataSetChanged();
////            }
////
////            @Override
////            public void onFailure(Exception e) {
////                Toast.makeText(getContext(),
////                        "Lỗi tải danh mục: " + e.getMessage(),
////                        Toast.LENGTH_SHORT).show();
////                e.printStackTrace();
////            }
////        });
//    }

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