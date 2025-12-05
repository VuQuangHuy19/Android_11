package com.example.app6hu.Activities;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app6hu.Adapter.MonthlySummaryAdapter;
import com.example.app6hu.R;
import com.example.app6hu.model.MonthlySummary;
import com.example.app6hu.model.Transaction;
import com.example.app6hu.utils.ChartUltils;  // Import ChartUtils
import com.example.app6hu.utils.Constants;  // Import Constants (nếu cần cho màu sắc hoặc query)
import com.example.app6hu.utils.FormatUtils;  // Import FormatUtils (sử dụng trong adapter)
import com.example.app6hu.utils.NotificationUtils;  // Import NotificationUtils cho lỗi
import com.example.app6hu.firebase.FirebasestoreManager;  // Giả định bạn có FirestoreManager
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
//import com.github.mikephil.charting.data.BarValue;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
public class ReportDetail extends AppCompatActivity {
    private BarChart barChart;
    private RecyclerView recyclerMonthlySummary;
    private TextView tvTitle;
    private ImageView btnBack;
    private FirebasestoreManager firestoreManager;
    private List<Transaction> allTransactions = new ArrayList<>();
    private List<MonthlySummary> monthlySummaryList = new ArrayList<>();
    private MonthlySummaryAdapter adapter;
    private String categoryName;
    private boolean isExpense;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_report_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_report_detail), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();

        categoryName = getIntent().getStringExtra("categoryName");
        isExpense = getIntent().getBooleanExtra("isExpense", true);

        tvTitle.setText("Chi tiết: " + categoryName);
        firestoreManager = new FirebasestoreManager();
        btnBack.setOnClickListener(v -> finish());
        loadDataFromFirebase();
    }

    private void initViews() {
        barChart = findViewById(R.id.barChart);
        recyclerMonthlySummary = findViewById(R.id.recyclerMonthlySummary);
        tvTitle = findViewById(R.id.tvTitle);
        btnBack = findViewById(R.id.btnBack);
        recyclerMonthlySummary.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadDataFromFirebase() {
        firestoreManager.getAllTransactions(new FirebasestoreManager.FirestoreCallback<List<Transaction>>() {
            @Override
            public void onSuccess(List<Transaction> data) {
                allTransactions = data;
                calculateMonthlySummary();
            }

            @Override
            public void onFailure(Exception e) {

            }
        });
    }

    // Nhóm giao dịch theo 12 tháng
    private void calculateMonthlySummary(){
        // Map: month -> total amount
        HashMap<Integer, Long> map = new HashMap<>();
        for (int i = 1; i<= 12; i++) map.put(i, 0L);
        Calendar cal = Calendar.getInstance();
        for (Transaction t : allTransactions){
            // Lọc theo loại
            if (isExpense && !t.getType().equals("EXPENSE")) continue;
            if (!isExpense && !t.getType().equals("INCOME")) continue;

            // Lọc theo category
            if (!t.getCategory().equals(categoryName)) continue;
            cal.setTime(t.getDate());
            int month = cal.get(Calendar.MONTH) + 1;
            long current = map.get(month);
            map.put(month, current + (long) t.getAmount());
        }

        // Tạo danh sách để hiển thị RecyclerView
        monthlySummaryList.clear();
        for (int i = 1; i <= 12; i++){
            monthlySummaryList.add(new MonthlySummary("Tháng " + i, map.get(i)));
        }
        showRecycler();
        showBarChart(map);
    }

    // Hiển thị RecyclerView
    private void showRecycler(){
        adapter = new MonthlySummaryAdapter(monthlySummaryList);
        recyclerMonthlySummary.setAdapter(adapter);
    }

    // Vẽ BarChart bằng dữ liệu
    private void showBarChart(HashMap<Integer, Long> map){
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        for (int i = 1; i <= 12; i++){
            entries.add(new BarEntry(i, map.get(i)));
            labels.add("T" + i);
        }

        BarDataSet dataSet = new BarDataSet(entries, "Theo tháng");
        dataSet.setDrawValues(false);
        dataSet.setColor(isExpense ? Color.parseColor("#F44336") : Color.parseColor("#2196F3"));

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.8f);
        barChart.setData(data);
        barChart.getLegend().setEnabled(false);
        barChart.getDescription().setEnabled(false);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);

        barChart.getAxisRight().setEnabled(false);
        barChart.getAxisLeft().setTextColor(Color.DKGRAY);
        barChart.animateY(900);
        barChart.invalidate();
    }
}
