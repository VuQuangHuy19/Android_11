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
import java.util.List;
public class ReportDetail extends AppCompatActivity {
    private BarChart barChart;
    private RecyclerView recyclerMonthlySummary;
    private TextView tvTitle;
    private ImageView btnBack;
    private FirebasestoreManager firestoreManager;
    private MonthlySummaryAdapter adapter;
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

        barChart = findViewById(R.id.barChart);
        recyclerMonthlySummary = findViewById(R.id.recyclerMonthlySummary);
        tvTitle = findViewById(R.id.tvTitle);
        btnBack = findViewById(R.id.btnBack);

        String category = getIntent().getStringExtra("category");
        boolean isExpense = getIntent().getBooleanExtra("isExpense", true);

        tvTitle.setText("Chi tiết: " + category);
        btnBack.setOnClickListener(v -> finish());

        firestoreManager = new FirebasestoreManager();



        setupBarChart(isExpense, category);
        setupRecycler();
    }

    private void setupBarChart(boolean isExpense, String catagory) {
        List<BarEntry> entries = new ArrayList<>();
        List<String> months = new ArrayList<>();

        // Giả lập dữ liệu 12 tháng
        for (int i = 1; i <= 12; i++) {
            entries.add(new BarEntry(i, (float) (Math.random() * 20000000)));
            months.add("T" + i);
        }

        BarDataSet dataSet = new BarDataSet(entries, "Số tiền theo tháng");
        dataSet.setColor(isExpense ? Color.parseColor("#F44336") : Color.parseColor("#2196F3"));
        dataSet.setValueTextColor(Color.TRANSPARENT); // Ẩn số liệu trên cột
        dataSet.setDrawValues(false); // Không hiển thị số tiền trên đầu cột

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.8f);

        barChart.setData(barData);
        barChart.getDescription().setEnabled(false);
        barChart.getLegend().setEnabled(false);

        // Cấu hình trục X
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(months));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(true);

        // ✅ Ẩn trục Y bên phải
        barChart.getAxisRight().setEnabled(false);

        // ✅ Giữ lại trục Y bên trái làm thước đo
        barChart.getAxisLeft().setDrawGridLines(true);
        barChart.getAxisLeft().setDrawAxisLine(true);
        barChart.getAxisLeft().setTextColor(Color.DKGRAY);

        // Làm mịn animation
        barChart.animateY(1000);
        barChart.invalidate();
    }

    private void setupRecycler() {
        recyclerMonthlySummary.setLayoutManager(new LinearLayoutManager(this));
        List<MonthlySummary> list = new ArrayList<>();

        // Giả lập dữ liệu
        for (int i = 1; i <= 12; i++) {
            list.add(new MonthlySummary("Tháng " + i, (long) (Math.random() * 20000000)));
        }

        adapter = new MonthlySummaryAdapter(list);
        recyclerMonthlySummary.setAdapter(adapter);
    }
}
