package com.example.app6hu.Activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app6hu.Adapter.FindTransAdapter;
import com.example.app6hu.R;
import com.example.app6hu.firebase.FirebasestoreManager;
import com.example.app6hu.model.Transaction;
import com.example.app6hu.utils.FormatUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FindTransctionActivity extends AppCompatActivity {
    private EditText searchEditText;
    private TextView incomeAmount, expenseAmount, totalAmount, txtTotalTrans; // Thêm txtTotalTrans
    private RecyclerView transactionRecyclerView;
    private FindTransAdapter transactionAdapter;
    private ImageView backIcon, searchIcon;

    private List<Transaction> allTransactions = new ArrayList<>();
    private List<Transaction> filteredTransactions = new ArrayList<>();

    private FirebasestoreManager firestoreManager;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    private static final String TAG = "FindTransactionActivity"; // TAG cho log

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_calendar_tim_kiem);

        Log.d(TAG, "onCreate: Activity đang khởi tạo");

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        firestoreManager = new FirebasestoreManager();

        // Initialize views
        initViews();

        // Setup RecyclerView
        setupRecyclerView();

        // Setup search functionality
        setupSearch();

        // Setup click listeners
        setupClickListeners();

        // Load data từ Firestore
        loadTransactionsFromFirestore();
    }

    private void initViews() {
        searchEditText = findViewById(R.id.searchEditText);
        incomeAmount = findViewById(R.id.incomeAmount);
        expenseAmount = findViewById(R.id.expenseAmount);
        totalAmount = findViewById(R.id.totalAmount);
        transactionRecyclerView = findViewById(R.id.transactionRecyclerView);
        backIcon = findViewById(R.id.backicon);
        searchIcon = findViewById(R.id.searchicon);
        txtTotalTrans = findViewById(R.id.txttotaltrans); // Ánh xạ TextView

        // Ban đầu hiển thị "Đang tải..."
        txtTotalTrans.setText("Đang tải dữ liệu...");
        txtTotalTrans.setVisibility(View.VISIBLE);
        transactionRecyclerView.setVisibility(View.GONE); // Ẩn RecyclerView khi chưa có dữ liệu
    }

    private void setupRecyclerView() {
        transactionAdapter = new FindTransAdapter(this, filteredTransactions);
        transactionRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        transactionRecyclerView.setAdapter(transactionAdapter);
    }

    private void setupSearch() {
        // TextWatcher để tìm kiếm khi gõ
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Tự động tìm kiếm khi gõ (có thể bật/tắt tùy ý)
                // filterTransactions(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Xử lý nút "OK" trên bàn phím (IME_ACTION_DONE)
        searchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE ||
                        actionId == EditorInfo.IME_ACTION_SEARCH ||
                        (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                    // Ẩn bàn phím
                    hideKeyboard();
                    // Thực hiện tìm kiếm
                    performSearch();
                    return true;
                }
                return false;
            }
        });

        // Xử lý nhấn Enter trên bàn phím
        searchEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Ẩn bàn phím
                    hideKeyboard();
                    // Thực hiện tìm kiếm
                    performSearch();
                    return true;
                }
                return false;
            }
        });
    }

    private void setupClickListeners() {
        // Nhấn nút quay lại
        backIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Trở lại CalendarFragment
            }
        });

        // Nhấn nút tìm kiếm
        searchIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Ẩn bàn phím
                hideKeyboard();
                // Thực hiện tìm kiếm
                performSearch();
            }
        });
    }

    // Ẩn bàn phím
    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            android.view.inputmethod.InputMethodManager imm =
                    (android.view.inputmethod.InputMethodManager) getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void loadTransactionsFromFirestore() {
        Log.d(TAG, "loadTransactionsFromFirestore: Bắt đầu tải từ Firestore");

        firestoreManager.getAllTransactions(new FirebasestoreManager.FirestoreCallback<List<Transaction>>() {
            @Override
            public void onSuccess(List<Transaction> transactions) {
                Log.d(TAG, "onSuccess: Nhận được " + transactions.size() + " giao dịch từ Firestore");

                allTransactions.clear();

                for (Transaction transaction : transactions) {
                    if (transaction != null) {
                        // Xử lý ID
                        String docId = transaction.getDocumentId();
                        if (docId != null) {
                            try {
                                transaction.setId(Integer.parseInt(docId));
                            } catch (NumberFormatException e) {
                                transaction.setId(docId.hashCode());
                            }
                        } else {
                            transaction.setId((int) System.currentTimeMillis());
                        }

                        // Đảm bảo date không null
                        if (transaction.getDate() == null) {
                            transaction.setDate(new Date());
                        }

                        allTransactions.add(transaction);

                        // Log chi tiết transaction
                        Log.v(TAG, String.format("Transaction: %s - %s - %.0f",
                                transaction.getCategory(),
                                transaction.getType(),
                                transaction.getAmount()));
                    }
                }

                // Update UI
                updateStatistics(allTransactions);

                // Hiển thị tất cả ban đầu
                filteredTransactions.clear();
                filteredTransactions.addAll(allTransactions);
                transactionAdapter.updateList(filteredTransactions);

                // CẬP NHẬT HIỂN THỊ TỔNG GIAO DỊCH
                updateTotalTransactionsDisplay();

                // Hiển thị thông báo
                Toast.makeText(FindTransctionActivity.this,
                        "Đã tải " + allTransactions.size() + " giao dịch từ Firestore",
                        Toast.LENGTH_SHORT).show();

                Log.i(TAG, "Tải dữ liệu thành công: " + allTransactions.size() + " giao dịch");
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "onFailure: Lỗi tải dữ liệu từ Firestore", e);

                // Hiển thị thông báo lỗi
                txtTotalTrans.setText("Lỗi tải dữ liệu: " + e.getMessage());
                txtTotalTrans.setVisibility(View.VISIBLE);
                transactionRecyclerView.setVisibility(View.GONE);

                Toast.makeText(FindTransctionActivity.this,
                        "Lỗi tải dữ liệu từ Firestore: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateTotalTransactionsDisplay() {
        Log.d(TAG, "updateTotalTransactionsDisplay: Số lượng giao dịch = " + filteredTransactions.size());

        if (filteredTransactions.isEmpty()) {
            // Nếu không có giao dịch
            txtTotalTrans.setText("Không có giao dịch nào");
            txtTotalTrans.setVisibility(View.VISIBLE);
            transactionRecyclerView.setVisibility(View.GONE);
        } else {
            // Nếu có giao dịch
            String searchText = searchEditText.getText().toString().trim();

            if (searchText.isEmpty()) {
                // Hiển thị tổng số giao dịch
                txtTotalTrans.setText("Tổng số giao dịch: " + filteredTransactions.size());
            } else {
                // Hiển thị số giao dịch tìm thấy
                txtTotalTrans.setText("Tìm thấy " + filteredTransactions.size() + " giao dịch");
            }

            // Hiển thị RecyclerView và ẩn thông báo
            txtTotalTrans.setVisibility(View.VISIBLE);
            transactionRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void filterTransactions(String searchText) {
        Log.d(TAG, "filterTransactions: Tìm kiếm với từ khóa: '" + searchText + "'");
        Log.d(TAG, "Số giao dịch trước filter: " + allTransactions.size());

        filteredTransactions.clear();

        if (searchText.isEmpty()) {
            // Hiển thị tất cả nếu search trống
            filteredTransactions.addAll(allTransactions);
            Log.d(TAG, "Hiển thị tất cả giao dịch");
        } else {
            String searchLower = searchText.toLowerCase(Locale.getDefault());
            int matchCount = 0;

            for (Transaction transaction : allTransactions) {
                boolean matches = false;

                // Tìm trong category
                if (transaction.getCategory() != null &&
                        transaction.getCategory().toLowerCase(Locale.getDefault()).contains(searchLower)) {
                    matches = true;
                }

                // Tìm trong detail
                if (!matches && transaction.getDetail() != null &&
                        transaction.getDetail().toLowerCase(Locale.getDefault()).contains(searchLower)) {
                    matches = true;
                }

                // Tìm trong type
                if (!matches && transaction.getType() != null &&
                        transaction.getType().toLowerCase(Locale.getDefault()).contains(searchLower)) {
                    matches = true;
                }

                // Tìm trong amount (dạng số)
                if (!matches && String.valueOf((int)transaction.getAmount()).contains(searchText)) {
                    matches = true;
                }

                // Tìm trong amount (dạng chữ đã format)
                if (!matches) {
                    String formattedAmount = FormatUtils.formatCurrency(transaction.getAmount());
                    if (formattedAmount.contains(searchText)) {
                        matches = true;
                    }
                }

                // Tìm trong ngày
                if (!matches && transaction.getDate() != null) {
                    String formattedDate = FormatUtils.formatDate(transaction.getDate());
                    if (formattedDate.toLowerCase(Locale.getDefault()).contains(searchLower)) {
                        matches = true;
                    }
                }

                if (matches) {
                    filteredTransactions.add(transaction);
                    matchCount++;
                }
            }
            Log.d(TAG, "Tìm thấy " + matchCount + " giao dịch phù hợp");
        }

        // Cập nhật adapter
        transactionAdapter.updateList(filteredTransactions);

        // Cập nhật thống kê
        updateStatistics(filteredTransactions);

        // Cập nhật hiển thị tổng số giao dịch
        updateTotalTransactionsDisplay();

        // Hiển thị thông báo nếu không tìm thấy
        if (!searchText.isEmpty() && filteredTransactions.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy kết quả cho: " + searchText, Toast.LENGTH_LONG).show();
        }
    }

    // Thực hiện tìm kiếm
    private void performSearch() {
        String searchText = searchEditText.getText().toString().trim();
        Log.d(TAG, "performSearch: " + searchText);
        filterTransactions(searchText);

        // Hiển thị thông báo
        if (searchText.isEmpty()) {
            Toast.makeText(this, "Hiển thị tất cả giao dịch", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Đang tìm kiếm: " + searchText, Toast.LENGTH_SHORT).show();
        }
    }

    private void updateStatistics(List<Transaction> transactions) {
        double totalIncome = 0;
        double totalExpense = 0;

        for (Transaction transaction : transactions) {
            if ("INCOME".equalsIgnoreCase(transaction.getType())) {
                totalIncome += transaction.getAmount();
            } else if ("EXPENSE".equalsIgnoreCase(transaction.getType())) {
                totalExpense += transaction.getAmount();
            }
        }

        double total = totalIncome - totalExpense;

        // Update UI với FormatUtils
        incomeAmount.setText(FormatUtils.formatCurrencyWithSign(totalIncome));
        expenseAmount.setText(FormatUtils.formatCurrencyWithSign(-totalExpense));
        totalAmount.setText(FormatUtils.formatCurrencyWithSign(total));

        // Đặt màu sắc
        if (total > 0) {
            totalAmount.setTextColor(getResources().getColor(android.R.color.holo_green_dark, null));
        } else if (total < 0) {
            totalAmount.setTextColor(getResources().getColor(android.R.color.holo_red_dark, null));
        } else {
            totalAmount.setTextColor(getResources().getColor(android.R.color.black, null));
        }

        Log.d(TAG, String.format("Thống kê: Thu=%.0f, Chi=%.0f, Tổng=%.0f",
                totalIncome, totalExpense, total));
    }

    
}