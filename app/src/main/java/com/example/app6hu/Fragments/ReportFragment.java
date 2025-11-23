package com.example.app6hu.Fragments;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app6hu.Activities.ReportDetail;
import com.example.app6hu.Adapter.ReportItemAdapter;
import com.example.app6hu.R;
import com.example.app6hu.firebase.FirebasestoreManager;
import com.example.app6hu.model.ReportItem;
import com.example.app6hu.model.Transaction;
import com.example.app6hu.utils.ExpenseForecastUtils;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ReportFragment extends Fragment {

    private TextView tvMonthYear, tvIncome, tvExpense, tvBalance, tabExpense, tabIncome;
    private TextView tvForecastAmount, tvForecastTrend, tvForecastMessage, tvWarningMessage;
    private ImageView btnPrevMonth, btnNextMonth;
    private PieChart pieChart;
    private RecyclerView recyclerReport;
    private View cardWarning;
    private Button btnTestData;
    private boolean showingExpense = true;
    private int currentMonth, currentYear;
    private ReportItemAdapter adapter;
    private FirebasestoreManager firestoreManager;

    public ReportFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_report, container, false);
        firestoreManager = new FirebasestoreManager();
        initViews(v);
        initTime();
        setupListeners();
        updateUI();
        loadForecast();
        return v;
    }

    private void initViews(View v) {
        tvMonthYear = v.findViewById(R.id.tvMonthYear);
        tvIncome = v.findViewById(R.id.tvIncome);
        tvExpense = v.findViewById(R.id.tvExpense);
        tvBalance = v.findViewById(R.id.tvBalance);
        btnPrevMonth = v.findViewById(R.id.btnPrevMonth);
        btnNextMonth = v.findViewById(R.id.btnNextMonth);
        pieChart = v.findViewById(R.id.pieChart);
        recyclerReport = v.findViewById(R.id.recyclerDetails);
        tabExpense = v.findViewById(R.id.tabExpense);
        tabIncome = v.findViewById(R.id.tabIncome);
        tvForecastAmount = v.findViewById(R.id.tvForecastAmount);
        tvForecastTrend = v.findViewById(R.id.tvForecastTrend);
        tvForecastMessage = v.findViewById(R.id.tvForecastMessage);
        cardWarning = v.findViewById(R.id.cardWarning);
        tvWarningMessage = v.findViewById(R.id.tvWarningMessage);
        btnTestData = v.findViewById(R.id.btnTestData);
        
        // Button test data (tạm thời để test)
        if (btnTestData != null) {
            btnTestData.setOnClickListener(v1 -> addTestData());
        }
    }

    // --- Lấy thời gian hiện tại ---
    private void initTime() {
        Calendar c = Calendar.getInstance();
        currentMonth = c.get(Calendar.MONTH) + 1;
        currentYear = c.get(Calendar.YEAR);
    }

    // --- Gắn sự kiện ---
    private void setupListeners() {
        btnPrevMonth.setOnClickListener(v -> changeMonth(-1));
        btnNextMonth.setOnClickListener(v -> changeMonth(1));

        tabExpense.setOnClickListener(v -> {
            showingExpense = true;
            updateUI();
        });

        tabIncome.setOnClickListener(v -> {
            showingExpense = false;
            updateUI();
        });
    }

    // --- Chuyển tháng ---
    private void changeMonth(int diff) {
        currentMonth += diff;
        if (currentMonth < 1) {
            currentMonth = 12;
            currentYear--;
        } else if (currentMonth > 12) {
            currentMonth = 1;
            currentYear++;
        }
        updateUI();
    }

    // --- Cập nhật toàn giao diện ---
    private void updateUI() {
        tvMonthYear.setText("Tháng " + currentMonth + ", " + currentYear);

        // Dữ liệu giả
        long income = 12000000;
        long expense = 8000000;
        long balance = income - expense;
        DecimalFormat df = new DecimalFormat("#,###");

        tvIncome.setText("+ " + df.format(income) + "đ");
        tvExpense.setText("- " + df.format(expense) + "đ");
        tvBalance.setText((balance >= 0 ? "+" : "-") + df.format(Math.abs(balance)) + "đ");

        updateTabColors();
        setupPieChart();
        setupRecycler();
        checkExpenseWarning();
    }

    // --- Cập nhật màu tab (dùng ContextCompat.getColor để tránh deprecated) ---
    // ---Anh em chú ý chỗ này dùng dùng ContextCompat.getColor() cho màu, thay cho getColor()---
    private void updateTabColors() {
        if (showingExpense) {
            tabExpense.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.red));
            tabExpense.setTextColor(Color.WHITE);
            tabIncome.setBackgroundColor(Color.TRANSPARENT);
            tabIncome.setTextColor(ContextCompat.getColor(getContext(), R.color.gray));
        } else {
            tabIncome.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.green));
            tabIncome.setTextColor(Color.WHITE);
            tabExpense.setBackgroundColor(Color.TRANSPARENT);
            tabExpense.setTextColor(ContextCompat.getColor(getContext(), R.color.gray));
        }
    }

    // --- Biểu đồ tròn ---
    private void setupPieChart() {
        List<PieEntry> entries = new ArrayList<>();

        if (showingExpense) {
            entries.add(new PieEntry(3000000, "Ăn uống"));
            entries.add(new PieEntry(2500000, "Đi lại"));
            entries.add(new PieEntry(1500000, "Giải trí"));
            entries.add(new PieEntry(1000000, "Khác"));
        } else {
            entries.add(new PieEntry(6000000, "Lương"));
            entries.add(new PieEntry(3000000, "Thưởng"));
            entries.add(new PieEntry(3000000, "Khác"));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(new int[]{
                Color.parseColor("#FF7043"),
                Color.parseColor("#29B6F6"),
                Color.parseColor("#66BB6A"),
                Color.parseColor("#FFD54F")
        });
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.WHITE);

        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.getDescription().setEnabled(false);
        pieChart.setCenterText(showingExpense ? "Chi tiêu" : "Thu nhập");
        pieChart.setHoleRadius(45f);
        pieChart.setTransparentCircleRadius(50f);
        pieChart.animateY(1000);
        pieChart.invalidate();
    }

    // --- RecyclerView ---
    private void setupRecycler() {
        recyclerReport.setLayoutManager(new LinearLayoutManager(getContext()));
        List<ReportItem> list = new ArrayList<>();

        if (showingExpense) {
            list.add(new ReportItem("Ăn uống", 3000000, "#FF7043"));
            list.add(new ReportItem("Đi lại", 2500000, "#29B6F6"));
            list.add(new ReportItem("Giải trí", 1500000, "#66BB6A"));
            list.add(new ReportItem("Khác", 1000000, "#FFD54F"));
        } else {
            list.add(new ReportItem("Lương", 6000000, "#4CAF50"));
            list.add(new ReportItem("Thưởng", 3000000, "#2196F3"));
            list.add(new ReportItem("Khác", 3000000, "#FFC107"));
        }

        adapter = new ReportItemAdapter(list, showingExpense, getContext(), item -> {
            Intent intent = new Intent(getContext(), ReportDetail.class);
            intent.putExtra("categoryName", item.getName());
            intent.putExtra("isExpense", showingExpense);
            startActivity(intent);
        });

        recyclerReport.setAdapter(adapter);
    }

    // --- Dự báo chi tiêu ---
    private void loadForecast() {
        // Lấy dữ liệu giao dịch các tháng trước để dự báo
        firestoreManager.getAllTransactions(new FirebasestoreManager.FirestoreCallback<List<Transaction>>() {
            @Override
            public void onSuccess(List<Transaction> transactions) {
                if (transactions != null && !transactions.isEmpty()) {
                    // Lọc chỉ lấy giao dịch có date và type hợp lệ
                    List<Transaction> validTransactions = new ArrayList<>();
                    for (Transaction t : transactions) {
                        if (t != null && t.getDate() != null && t.getType() != null) {
                            validTransactions.add(t);
                        }
                    }
                    
                    if (!validTransactions.isEmpty()) {
                        ExpenseForecastUtils.ForecastResult forecast = ExpenseForecastUtils.forecastNextMonth(validTransactions);
                        String trend = ExpenseForecastUtils.calculateTrend(validTransactions);
                        
                        DecimalFormat df = new DecimalFormat("#,###");
                        tvForecastAmount.setText("Dự báo: " + df.format(forecast.getForecastAmount()) + "đ");
                        tvForecastTrend.setText("Xu hướng: " + trend);
                        tvForecastMessage.setText(forecast.getMessage());
                    } else {
                        tvForecastAmount.setText("Dự báo: Chưa có dữ liệu hợp lệ");
                        tvForecastTrend.setText("Xu hướng: Không đủ dữ liệu");
                        tvForecastMessage.setText("Các giao dịch cần có ngày và loại để dự báo");
                    }
                } else {
                    tvForecastAmount.setText("Dự báo: Chưa có dữ liệu");
                    tvForecastTrend.setText("Xu hướng: Không đủ dữ liệu");
                    tvForecastMessage.setText("Vui lòng thêm giao dịch để có dự báo chính xác");
                }
            }

            @Override
            public void onFailure(Exception e) {
                tvForecastAmount.setText("Dự báo: Lỗi tải dữ liệu");
                tvForecastTrend.setText("Xu hướng: Không xác định");
                tvForecastMessage.setText("Không thể tải dữ liệu để dự báo: " + (e != null ? e.getMessage() : "Unknown error"));
            }
        });
    }

    // --- Kiểm tra cảnh báo chi tiêu ---
    private void checkExpenseWarning() {
        firestoreManager.getAllTransactions(new FirebasestoreManager.FirestoreCallback<List<Transaction>>() {
            @Override
            public void onSuccess(List<Transaction> transactions) {
                ExpenseForecastUtils.WarningResult warning = ExpenseForecastUtils.checkExpenseWarning(
                        transactions, currentMonth, currentYear);
                
                if (warning.isOverLimit()) {
                    cardWarning.setVisibility(View.VISIBLE);
                    tvWarningMessage.setText(warning.getMessage());
                } else {
                    cardWarning.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Exception e) {
                cardWarning.setVisibility(View.GONE);
            }
        });
    }

    // --- Thêm dữ liệu test để kiểm tra dự báo và cảnh báo ---
    private void addTestData() {
        Calendar cal = Calendar.getInstance();
        int totalCount = 0;

        // ===== TEST CẢNH BÁO =====
        // Tạo dữ liệu cho các tháng trước với số tiền THẤP (trung bình ~3-4 triệu/tháng)
        // Tháng 2 tháng trước
        cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -2);
        for (int i = 1; i <= 8; i++) {
            cal.set(Calendar.DAY_OF_MONTH, i * 3);
            Transaction t = new Transaction();
            t.setType("EXPENSE");
            t.setAmount(300000 + i * 50000); // 350k, 400k, 450k, ..., 700k
            t.setCategory("Ăn uống");
            t.setDetail("Chi tiêu tháng " + (cal.get(Calendar.MONTH) + 1) + " - " + i);
            t.setDate(new Date(cal.getTimeInMillis()));
            t.setIcon("🍔");
            firestoreManager.addTransaction(t);
            totalCount++;
        }

        // Tháng 1 tháng trước
        cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -1);
        for (int i = 1; i <= 10; i++) {
            cal.set(Calendar.DAY_OF_MONTH, i * 2);
            Transaction t = new Transaction();
            t.setType("EXPENSE");
            t.setAmount(400000 + i * 40000); // 440k, 480k, 520k, ..., 800k
            t.setCategory("Đi lại");
            t.setDetail("Chi tiêu tháng " + (cal.get(Calendar.MONTH) + 1) + " - " + i);
            t.setDate(new Date(cal.getTimeInMillis()));
            t.setIcon("🚗");
            firestoreManager.addTransaction(t);
            totalCount++;
        }

        // ===== TẠO DỮ LIỆU THÁNG HIỆN TẠI VỚI SỐ TIỀN CAO (để trigger cảnh báo) =====
        // Tổng tháng trước: ~3.5-4 triệu
        // Tạo tháng này với tổng ~6-7 triệu (vượt quá 50-70%)
        cal = Calendar.getInstance();
        int daysPassed = cal.get(Calendar.DAY_OF_MONTH);
        
        // Tạo nhiều giao dịch để tổng tiền cao hơn trung bình
        for (int i = 1; i <= Math.min(daysPassed, 10); i++) {
            cal.set(Calendar.DAY_OF_MONTH, i * 2);
            Transaction t = new Transaction();
            t.setType("EXPENSE");
            t.setAmount(500000 + i * 100000); // 600k, 700k, 800k, ..., 1.5tr
            t.setCategory("Giải trí");
            t.setDetail("Chi tiêu tháng này (test cảnh báo) " + i);
            t.setDate(new Date(cal.getTimeInMillis()));
            t.setIcon("🎮");
            firestoreManager.addTransaction(t);
            totalCount++;
        }

        // Thêm vài giao dịch lớn nữa để đảm bảo vượt quá
        cal = Calendar.getInstance();
        for (int i = 1; i <= 3; i++) {
            cal.set(Calendar.DAY_OF_MONTH, i * 5);
            Transaction t = new Transaction();
            t.setType("EXPENSE");
            t.setAmount(1500000 + i * 200000); // 1.7tr, 1.9tr, 2.1tr
            t.setCategory("Mua sắm");
            t.setDetail("Chi tiêu lớn tháng này " + i);
            t.setDate(new Date(cal.getTimeInMillis()));
            t.setIcon("🛍️");
            firestoreManager.addTransaction(t);
            totalCount++;
        }

        Toast.makeText(getContext(), 
                "✅ Đã thêm " + totalCount + " giao dịch test!\n\n" +
                "📊 Dữ liệu test:\n" +
                "• Tháng trước: ~3-4 triệu\n" +
                "• Tháng này: ~6-7 triệu\n" +
                "• ⚠️ Cảnh báo sẽ hiển thị!\n\n" +
                "Vui lòng chờ 3 giây để dữ liệu được lưu...", 
                Toast.LENGTH_LONG).show();

        // Tự động refresh sau 3 giây (để Firestore kịp lưu)
        new android.os.Handler().postDelayed(() -> {
            loadForecast();
            checkExpenseWarning();
            Toast.makeText(getContext(), "🔄 Đã refresh dữ liệu! Kiểm tra phần cảnh báo phía trên.", 
                    Toast.LENGTH_SHORT).show();
        }, 3000);
    }
}
