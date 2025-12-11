package com.example.app6hu.Activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app6hu.Adapter.DanhMucAdapter;
import com.example.app6hu.R;
import com.example.app6hu.firebase.FirebasestoreManager;
import com.example.app6hu.model.DanhMuc;
import com.example.app6hu.model.Transaction;
import com.example.app6hu.utils.FormatUtils;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EditTransactionActivity extends AppCompatActivity implements DanhMucAdapter.OnDanhMucClickListener {

    private static final String TAG = "EditTransactionActivity";

    // Views
    private ImageView btnBack, btnSave;
    private EditText etDate, etNote, etAmount;
    private TextView tvVND;
    private Button btnDelete, btnSaveChanges;
    private RecyclerView rvDanhMuc;

    // Transaction data
    private long transactionId;
    private String transactionType;
    private double transactionAmount;
    private String transactionCategory;
    private String transactionDetail;
    private int transactionDay;
    private Date transactionDate;
    private String transactionDocumentId; // Document ID từ Firestore

    private int currentMonth, currentYear;

    // For date picker
    private Calendar selectedDate;

    // Categories
    private List<DanhMuc> allCategories = new ArrayList<>();
    private List<DanhMuc> filteredCategories = new ArrayList<>();
    private DanhMucAdapter danhMucAdapter;
    private String selectedCategory = "";
    private String selectedCategoryId = "";


    private FirebasestoreManager firestoreManager;
    private SimpleDateFormat dateFormatter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chinh_sua_transaction);

        firestoreManager = new FirebasestoreManager();

        // Khởi tạo date formatter
        dateFormatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        getIntentData();
        initViews();
        setupDatePicker();
        setupClickListeners();
        setupRecyclerView();
        updateUI();
        loadCategoriesFromFirestore();
    }

    private void getIntentData() {
        Log.d(TAG, "=== EditTransactionActivity Data ===");

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            for (String key : extras.keySet()) {
                Object value = extras.get(key);
                Log.d(TAG, "Intent Extra -> " + key + " = " + value + " (type: " + (value != null ? value.getClass().getName() : "null") + ")");
            }
        }

        // SỬA CÁCH ĐỌC transaction_id
        Object idObj = getIntent().getExtras().get("transaction_id");
        if (idObj instanceof Integer) {
            transactionId = ((Integer) idObj).longValue(); // Convert Integer → long
            Log.d(TAG, "transaction_id is Integer, converted to long: " + transactionId);
        } else if (idObj instanceof Long) {
            transactionId = (Long) idObj;
            Log.d(TAG, "transaction_id is Long: " + transactionId);
        } else {
            transactionId = 0L;
            Log.w(TAG, "transaction_id is unknown type: " + (idObj != null ? idObj.getClass().getName() : "null"));
        }
        // Đọc các field khác
        transactionType = getIntent().getStringExtra("transaction_type");
        transactionAmount = getIntent().getDoubleExtra("transaction_amount", 0);
        transactionCategory = getIntent().getStringExtra("transaction_category");
        transactionDetail = getIntent().getStringExtra("transaction_detail");
        transactionDay = getIntent().getIntExtra("transaction_day", 1);
        selectedCategory = transactionCategory; // Gán vào selectedCategory

        // Đọc category ID nếu có
        selectedCategoryId = getIntent().getStringExtra("transaction_category_id");

        Log.d(TAG, "Category from intent: " + transactionCategory);
        Log.d(TAG, "Category ID from intent: " + selectedCategoryId);
        // Document ID - đọc đúng cách
        transactionDocumentId = getIntent().getStringExtra("transaction_document_id");

        // QUAN TRỌNG: Đọc documentId với nhiều key có thể có
        transactionDocumentId = getIntent().getStringExtra("transaction_document_id");
        if (transactionDocumentId == null) {
            // Thử với key khác (phòng trường hợp)
            transactionDocumentId = getIntent().getStringExtra("documentId");
        }
        if (transactionDocumentId == null) {
            transactionDocumentId = getIntent().getStringExtra("document_id");
        }

        // Get date from Intent
        long dateMillis = getIntent().getLongExtra("transaction_date", 0);
        if (dateMillis > 0) {
            transactionDate = new Date(dateMillis);
        } else {
            transactionDate = new Date();
            Log.w(TAG, "Không có date từ Intent, dùng ngày hiện tại");
        }

        // Get month/year từ Intent hoặc dùng từ date
        currentMonth = getIntent().getIntExtra("current_month",
                transactionDate != null ?
                        getMonthFromDate(transactionDate) :
                        Calendar.getInstance().get(Calendar.MONTH) + 1);

        currentYear = getIntent().getIntExtra("current_year",
                transactionDate != null ?
                        getYearFromDate(transactionDate) :
                        Calendar.getInstance().get(Calendar.YEAR));

        // Set selected category
        selectedCategory = transactionCategory;

        // Khởi tạo selectedDate từ transactionDate
        selectedDate = Calendar.getInstance();
        if (transactionDate != null) {
            selectedDate.setTime(transactionDate);

            if (selectedDate.get(Calendar.YEAR) < 1970) {
                Log.w(TAG, "Năm không hợp lệ: " + selectedDate.get(Calendar.YEAR) +
                        ", đặt lại thành năm hiện tại");
                selectedDate.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR));
                transactionDate = selectedDate.getTime();
            }
        }

        // BÂY GIỜ MỚI LOG CÁC GIÁ TRỊ ĐÃ ĐỌC
        Log.d(TAG, "ID: " + transactionId);
        Log.d(TAG, "Document ID: " + transactionDocumentId);
        Log.d(TAG, "Type: " + transactionType);
        Log.d(TAG, "Amount: " + transactionAmount);
        Log.d(TAG, "Category: " + transactionCategory);
        Log.d(TAG, "Detail: " + transactionDetail);
        Log.d(TAG, "Date: " + (transactionDate != null ? dateFormatter.format(transactionDate) : "null"));
        Log.d(TAG, "Month/Year: " + currentMonth + "/" + currentYear);
    }

    // Helper methods
    private int getMonthFromDate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.MONTH) + 1;
    }

    private int getYearFromDate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.YEAR);
    }

    private Date createDateFromComponents() {
        Calendar cal = Calendar.getInstance();
        cal.set(currentYear, currentMonth - 1, transactionDay, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnSave = findViewById(R.id.btnSave);
        etDate = findViewById(R.id.chartDateSet);
        etNote = findViewById(R.id.chartWriteSet);
        etAmount = findViewById(R.id.chartMoneySet);
        tvVND = findViewById(R.id.VND);
        btnDelete = findViewById(R.id.btnDelete);
        btnSaveChanges = findViewById(R.id.add_over);
        rvDanhMuc = findViewById(R.id.rvDanhMuc);

        Log.d(TAG, "Views initialized:");
        Log.d(TAG, "etDate: " + (etDate != null));
        Log.d(TAG, "etNote: " + (etNote != null));
        Log.d(TAG, "etAmount: " + (etAmount != null));
        Log.d(TAG, "rvDanhMuc: " + (rvDanhMuc != null));

        setupAmountFormatting();
    }

    private void setupDatePicker() {
        if (etDate != null && selectedDate != null) {
            etDate.setText(dateFormatter.format(selectedDate.getTime()));
        }

        etDate.setOnClickListener(v -> showDatePicker());
    }

    private void showDatePicker() {
        try {
            // Sử dụng DatePickerDialog
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, year, month, dayOfMonth) -> {
                        selectedDate.set(Calendar.YEAR, year);
                        selectedDate.set(Calendar.MONTH, month);
                        selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        transactionDate = selectedDate.getTime();
                        etDate.setText(dateFormatter.format(selectedDate.getTime()));

                        transactionDay = dayOfMonth;
                        currentMonth = month + 1;
                        currentYear = year;

                        Log.d(TAG, "Date selected: " + dateFormatter.format(selectedDate.getTime()) +
                                ", Year: " + year);
                    },
                    selectedDate.get(Calendar.YEAR),
                    selectedDate.get(Calendar.MONTH),
                    selectedDate.get(Calendar.DAY_OF_MONTH)
            );

            // Đặt tiêu đề
            datePickerDialog.setTitle("Chọn ngày");

            // Đặt min date là 1/1/1970
            Calendar minDate = Calendar.getInstance();
            minDate.set(1970, Calendar.JANUARY, 1);
            datePickerDialog.getDatePicker().setMinDate(minDate.getTimeInMillis());

            // Đặt max date là 31/12/2100
            Calendar maxDate = Calendar.getInstance();
            maxDate.set(2100, Calendar.DECEMBER, 31);
            datePickerDialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());

            datePickerDialog.show();

        } catch (Exception e) {
            Log.e(TAG, "Error showing date picker: " + e.getMessage(), e);
            Toast.makeText(this, "Lỗi hiển thị chọn ngày", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupAmountFormatting() {
        etAmount.addTextChangedListener(new TextWatcher() {
            private String current = "";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().equals(current)) {
                    etAmount.removeTextChangedListener(this);

                    String cleanString = s.toString().replaceAll("[^\\d]", "");

                    if (!cleanString.isEmpty()) {
                        try {
                            double parsed = Double.parseDouble(cleanString);
                            String formatted = FormatUtils.formatCurrencyNoSymbol(parsed);
                            current = formatted;
                            etAmount.setText(formatted);
                            etAmount.setSelection(formatted.length());
                        } catch (NumberFormatException e) {
                            Log.e(TAG, "Error parsing amount", e);
                        }
                    } else {
                        current = "";
                    }

                    etAmount.addTextChangedListener(this);
                }
            }
        });
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnSave.setOnClickListener(v -> saveTransaction());

        btnDelete.setOnClickListener(v -> deleteTransaction());

        btnSaveChanges.setOnClickListener(v -> saveTransaction());
    }

    private void setupRecyclerView() {
        GridLayoutManager layoutManager = new GridLayoutManager(this, 4);
        rvDanhMuc.setLayoutManager(layoutManager);

        // KHỞI TẠO ADAPTER VỚI DANH SÁCH RỖNG TRƯỚC
        danhMucAdapter = new DanhMucAdapter(this, filteredCategories, this); // <-- SỬA Ở ĐÂY
        rvDanhMuc.setAdapter(danhMucAdapter);

        // Debug RecyclerView
        Log.d(TAG, "RecyclerView setup:");
        Log.d(TAG, "filteredCategories size: " + filteredCategories.size());
        Log.d(TAG, "rvDanhMuc is null: " + (rvDanhMuc == null));

        // Đảm bảo RecyclerView hiển thị
        rvDanhMuc.setVisibility(View.VISIBLE);

        rvDanhMuc.post(() -> {
            Log.d(TAG, "RecyclerView post - Width: " + rvDanhMuc.getWidth());
            Log.d(TAG, "RecyclerView post - Height: " + rvDanhMuc.getHeight());
            Log.d(TAG, "RecyclerView post - Adapter: " + rvDanhMuc.getAdapter());
            Log.d(TAG, "RecyclerView post - Item count: " + danhMucAdapter.getItemCount());

            // Kiểm tra visibility
            if (rvDanhMuc.getVisibility() != View.VISIBLE) {
                Log.w(TAG, "RecyclerView is NOT VISIBLE! Setting to VISIBLE");
                rvDanhMuc.setVisibility(View.VISIBLE);
            }
        });
    }


    private void loadCategoriesFromFirestore() {
        Log.d(TAG, "Bắt đầu load danh mục từ Firestore...");

        // Lấy danh mục theo loại (income/expense)
        String typeToLoad = "EXPENSE".equals(transactionType) ? "expense" : "income";

        firestoreManager.getDanhMucByType(typeToLoad, this, new FirebasestoreManager.FirestoreCallback<List<DanhMuc>>() {
            @Override
            public void onSuccess(List<DanhMuc> data) {
                Log.d(TAG, "onSuccess được gọi");
                Log.d(TAG, "Số lượng data nhận được: " + (data == null ? "null" : data.size()));

                allCategories.clear();
                filteredCategories.clear();
                List<DanhMuc> categoriesForAdapter = new ArrayList<>();
                int selectedPosition = -1; // Vị trí cần highlight

                if (data != null && !data.isEmpty()) {
                    categoriesForAdapter.addAll(data);

                    // TÌM VỊ TRÍ CỦA CATEGORY ĐÃ CHỌN
                    for (int i = 0; i < data.size(); i++) {
                        DanhMuc dm = data.get(i);

                        // So sánh theo tên danh mục
                        if (selectedCategory != null && selectedCategory.equals(dm.getItemName())) {
                            selectedCategoryId = dm.getId();
                            selectedPosition = i; // Lưu vị trí
                            Log.d(TAG, "✓ Found selected category at position: " + i +
                                    ", ID: " + selectedCategoryId);
                        }
                    }
                }

                // Thêm item "Thêm mới"
                DanhMuc addItem = new DanhMuc(
                        "Thêm mới",
                        R.drawable.ic_add,
                        DanhMuc.TYPE_ADD,
                        0
                );
                categoriesForAdapter.add(addItem);

                final int finalSelectedPosition = selectedPosition;

                runOnUiThread(() -> {
                    danhMucAdapter.updateData(categoriesForAdapter);
                    Log.d(TAG, "Đã gọi updateData với " + categoriesForAdapter.size() + " items");
                    danhMucAdapter.setSelectedCategory(selectedCategory); // <-- QUAN TRỌNG!

                    Log.d(TAG, "Đã set selected category: " + selectedCategory);
                    // HIGHLIGHT CATEGORY ĐÃ CHỌN
                    if (finalSelectedPosition != -1 && finalSelectedPosition < categoriesForAdapter.size()) {
                        // Delay một chút để đảm bảo RecyclerView đã render xong
                        rvDanhMuc.postDelayed(() -> {
                            // Gọi onCategoryClick để highlight
                            DanhMuc selectedDanhMuc = categoriesForAdapter.get(finalSelectedPosition);
                            danhMucAdapter.clearSelection(); // Xóa selection cũ

                            // Bạn cần thêm phương thức trong adapter để set selection
                            // Hoặc gọi listener.onCategoryClick(selectedDanhMuc)
                            onCategoryClick(selectedDanhMuc);

                            // Scroll đến vị trí đã chọn
                            rvDanhMuc.scrollToPosition(finalSelectedPosition);

                            Toast.makeText(EditTransactionActivity.this,
                                    "Đã chọn: " + selectedDanhMuc.getItemName(),
                                    Toast.LENGTH_SHORT).show();
                        }, 300);
                    } else if (selectedCategory != null && !selectedCategory.isEmpty()) {
                        Log.w(TAG, "✗ Không tìm thấy category: " + selectedCategory);
                        Toast.makeText(EditTransactionActivity.this,
                                "Không tìm thấy danh mục: " + selectedCategory,
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Lỗi tải danh mục: " + e.getMessage(), e);
                runOnUiThread(() -> {
                    Toast.makeText(EditTransactionActivity.this,
                            "Lỗi tải danh mục: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void updateUI() {
        Log.d(TAG, "Updating UI...");

        // Set date
        if (selectedDate != null) {
            etDate.setText(dateFormatter.format(selectedDate.getTime()));
        }

        // Set note
        if (transactionDetail != null && !transactionDetail.isEmpty()) {
            etNote.setText(transactionDetail);
            Log.d(TAG, "Note set: " + transactionDetail);
        } else {
            etNote.setText("");
        }

        // Set amount
        if (transactionAmount > 0) {
            String amountStr = FormatUtils.formatCurrencyNoSymbol(transactionAmount);
            etAmount.setText(amountStr);
            Log.d(TAG, "Amount set: " + amountStr);
        } else {
            etAmount.setText("");
        }

        // Update button text
        if ("EXPENSE".equals(transactionType)) {
            btnSaveChanges.setText("Lưu khoản chi");
        } else {
            btnSaveChanges.setText("Lưu khoản thu");
        }

        Log.d(TAG, "UI update completed");
    }

    @Override
    public void onCategoryClick(DanhMuc item) {
        if (item.getViewType() == DanhMuc.TYPE_CATEGORY) {
            selectedCategory = item.getItemName();
            selectedCategoryId = item.getId();

            Log.d(TAG, "Category selected: " + selectedCategory + ", ID: " + selectedCategoryId);
            Toast.makeText(this, "Đã chọn: " + selectedCategory, Toast.LENGTH_SHORT).show();

            // Cập nhật UI nếu cần
        }
    }

    @Override
    public void onAddClick() {
        Toast.makeText(this, "Thêm danh mục mới", Toast.LENGTH_SHORT).show();
        // TODO: Implement add new category functionality
    }

    private void saveTransaction() {
        Log.d(TAG, "=== saveTransaction called ===");
        Log.d(TAG, "Document ID available: " + (transactionDocumentId != null && !transactionDocumentId.isEmpty()));
        Log.d(TAG, "Transaction ID: " + transactionId);

        if (!validateInput()) return;

        try {
            String note = etNote.getText().toString().trim();
            String amountStr = etAmount.getText().toString().replaceAll("[^0-9]", "");
            double amount = Double.parseDouble(amountStr);

            if (selectedDate != null) {
                transactionDate = selectedDate.getTime();
            }

            Transaction updatedTransaction = new Transaction();
            updatedTransaction.setType(transactionType);
            updatedTransaction.setAmount(amount);
            updatedTransaction.setCategory(selectedCategory);
            updatedTransaction.setDetail(note);
            updatedTransaction.setDate(transactionDate);
            updatedTransaction.setId(transactionId); // Giữ nguyên ID nếu có

            Log.d(TAG, "=== SAVING TRANSACTION ===");
            Log.d(TAG, "Document ID: " + transactionDocumentId);
            Log.d(TAG, "Amount: " + amount);
            Log.d(TAG, "Category: " + selectedCategory);
            Log.d(TAG, "Note: " + note);
            Log.d(TAG, "Date: " + (transactionDate != null ? dateFormatter.format(transactionDate) : "null"));

            // QUYẾT ĐỊNH UPDATE HAY ADD MỚI
            if (transactionDocumentId != null && !transactionDocumentId.isEmpty()) {
                Log.d(TAG, "✓ UPDATE existing transaction with documentId: " + transactionDocumentId);
                updateTransactionInFirestore(updatedTransaction);
            } else {
                Log.w(TAG, "✗ Document ID is null/empty. Will ADD as NEW transaction");

                // Nếu có transactionId > 0, có thể tìm documentId
                if (transactionId > 0) {
                    Log.d(TAG, "Transaction has ID=" + transactionId + ", trying to find document...");
                    firestoreManager.findTransactionByField("id", transactionId, new FirebasestoreManager.FirestoreCallback<List<Transaction>>() {
                        @Override
                        public void onSuccess(List<Transaction> transactions) {
                            if (transactions != null && !transactions.isEmpty()) {
                                // Tìm thấy transaction, lấy documentId đầu tiên
                                Transaction found = transactions.get(0);
                                transactionDocumentId = found.getDocumentId();
                                Log.d(TAG, "Found transaction, documentId: " + transactionDocumentId);

                                // Update transaction với documentId mới tìm được
                                updatedTransaction.setDocumentId(transactionDocumentId);
                                updateTransactionInFirestore(updatedTransaction);
                            } else {
                                // Không tìm thấy, add mới
                                Log.d(TAG, "No transaction found with ID=" + transactionId + ", adding new");
                                addTransactionToFirestore(updatedTransaction);
                            }
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Log.e(TAG, "Error finding transaction: " + e.getMessage(), e);
                            // Nếu lỗi, thêm mới
                            addTransactionToFirestore(updatedTransaction);
                        }
                    });
                } else {
                    Log.d(TAG, "Adding completely new transaction");
                    addTransactionToFirestore(updatedTransaction);
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "Error saving: " + e.getMessage(), e);
            Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateTransactionInFirestore(Transaction transaction) {
        if (transactionDocumentId == null || transactionDocumentId.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy ID giao dịch", Toast.LENGTH_SHORT).show();
            return;
        }

        firestoreManager.updateTransaction(transactionDocumentId, transaction, new FirebasestoreManager.FirestoreCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                runOnUiThread(() -> {
                    Toast.makeText(EditTransactionActivity.this,
                            "Đã cập nhật: " + selectedCategory + " - " + FormatUtils.formatCurrency(transaction.getAmount()),
                            Toast.LENGTH_SHORT).show();

                    setResult(RESULT_OK);
                    finish();
                });
            }

            @Override
            public void onFailure(Exception e) {
                runOnUiThread(() -> {
                    Log.e(TAG, "Error updating transaction: " + e.getMessage(), e);
                    Toast.makeText(EditTransactionActivity.this,
                            "Lỗi cập nhật: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void addTransactionToFirestore(Transaction transaction) {
        firestoreManager.addTransaction(transaction);

        runOnUiThread(() -> {
            Toast.makeText(EditTransactionActivity.this,
                    "Đã thêm mới: " + selectedCategory + " - " + FormatUtils.formatCurrency(transaction.getAmount()),
                    Toast.LENGTH_SHORT).show();

            setResult(RESULT_OK);
            finish();
        });
    }

    private void deleteTransaction() {
        if (transactionDocumentId == null || transactionDocumentId.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy ID giao dịch", Toast.LENGTH_SHORT).show();
            return;
        }

        new android.app.AlertDialog.Builder(this)
                .setTitle("Xóa giao dịch")
                .setMessage("Bạn có chắc chắn muốn xóa giao dịch này?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    deleteTransactionFromFirestore();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteTransactionFromFirestore() {
        firestoreManager.deleteTransaction(transactionDocumentId);

        runOnUiThread(() -> {
            Toast.makeText(this, "Đã xóa giao dịch", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        });
    }

    private boolean validateInput() {
        // Check category
        if (selectedCategory == null || selectedCategory.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn danh mục", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Check amount
        String amountStr = etAmount.getText().toString().replaceAll("[^0-9]", "");
        if (amountStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show();
            return false;
        }

        try {
            double amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                Toast.makeText(this, "Số tiền phải lớn hơn 0", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }
}