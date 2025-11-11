package com.example.app6hu.Fragments;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app6hu.Activities.ReportDetail;
import com.example.app6hu.Adapter.ReportItemAdapter;
import com.example.app6hu.R;
import com.example.app6hu.model.ReportItem;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ReportFragment extends Fragment {

    private TextView tvMonthYear, tvIncome, tvExpense, tvBalance, tabExpense, tabIncome;
    private ImageView btnPrevMonth, btnNextMonth;
    private PieChart pieChart;
    private RecyclerView recyclerReport;
    private boolean showingExpense = true;
    private int currentMonth, currentYear;
    private ReportItemAdapter adapter;

    public ReportFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_report, container, false);
        initViews(v);
        initTime();
        setupListeners();
        updateUI();
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
}
