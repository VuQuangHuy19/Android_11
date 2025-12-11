package com.example.app6hu.Fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.opengl.EGLExt;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.NumberPicker;
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
import com.example.app6hu.firebase.FirebasestoreManager;
import com.example.app6hu.model.ReportItem;
import com.example.app6hu.model.Transaction;
import com.example.app6hu.utils.ChartUltils;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportFragment extends Fragment {

    private TextView tvMonthYear, tvIncome, tvExpense, tvBalance, tabExpense, tabIncome;
    private ImageView btnPrevMonth, btnNextMonth;
    private PieChart pieChart;
    private RecyclerView recyclerReport;
    private boolean showingExpense = true;
    private int currentMonth, currentYear;
    private ReportItemAdapter adapter;
    private FirebasestoreManager firestore;
    private List<Transaction> allTransactions = new ArrayList<>();

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
        initRecycler();
        firestore = new FirebasestoreManager();
        setupListeners();
        loadDataFromFirebase();
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

    // --- Tối ưu Recycler ---
    private void initRecycler(){
        recyclerReport.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ReportItemAdapter(new ArrayList<>(), showingExpense, getContext(), item -> {
            Intent intent = new Intent(getContext(), ReportDetail.class);
            intent.putExtra("categoryName", item.getName());
            intent.putExtra("isExpense", showingExpense);
            startActivity(intent);
        });
        recyclerReport.setAdapter(adapter);
    }

    // --- Gắn sự kiện ---
    private void setupListeners() {
        btnPrevMonth.setOnClickListener(v -> changeMonth(-1));
        btnNextMonth.setOnClickListener(v -> changeMonth(1));
        tvMonthYear.setOnClickListener(v -> showMonthYearPicker());

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

    // Lấy dữ liệu từ Firebase
    private void loadDataFromFirebase(){
        firestore.getAllTransactions(new FirebasestoreManager.FirestoreCallback<List<Transaction>>() {
            @Override
            public void onSuccess(List<Transaction> data) {
                allTransactions.clear();
                allTransactions.addAll(data);
                updateUI();
            }

            @Override
            public void onFailure(Exception e) {
                // Hien thi loi
            }
        });
    }
    // --- Cập nhật toàn giao diện ---
    private void updateUI() {
        if (getContext() == null) return;
        tvMonthYear.setText("Tháng " + currentMonth + ", " + currentYear);
        List<Transaction> monthList = filterByMonth();
        long totalIncome = 0;
        long totalExpense = 0;

        for (Transaction t : monthList){
            if (t.getType().equals("INCOME"))
                totalIncome += t.getAmount();
            else
                totalExpense += t.getAmount();
        }

        long balance = totalIncome - totalExpense;
        DecimalFormat df = new DecimalFormat("#,###");

        tvIncome.setText("+ " + df.format(totalIncome) + "đ");
        tvExpense.setText("- " + df.format(totalExpense) + "đ");
        tvBalance.setText((balance >= 0 ? "+" : "-") + df.format(Math.abs(balance)) + "đ");

        updateTabColors();
        setupPieChart(monthList);
        setupRecyclerData(monthList);
    }

    // Lọc giao dịch theo tháng - năm
    private List<Transaction> filterByMonth(){
        List<Transaction> list = new ArrayList<>();

        for (Transaction t : allTransactions){
            Calendar cal = Calendar.getInstance();
            cal.setTime(t.getDate());

            int m = cal.get(Calendar.MONTH) + 1;
            int y = cal.get(Calendar.YEAR);

            if (m == currentMonth && y == currentYear)
                list.add(t);
        }
        return list;
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
    private void setupPieChart(List<Transaction> monthList) {
        pieChart.clear();
        List<PieEntry> entries = new ArrayList<>();
        Map<String, Float> map = new HashMap<>();

        for (Transaction t : monthList){
            // lấy đúng loại tab đang chọn
            if (showingExpense && !t.getType().equals("EXPENSE")) continue;
            if (!showingExpense && !t.getType().equals("INCOME")) continue;

            map.put(t.getCategory(), map.getOrDefault(t.getCategory(), 0f) + (float) t.getAmount());
        }

        for (String cat: map.keySet()){
            entries.add(new PieEntry(map.get(cat), cat));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        List<Integer> colors = new ArrayList<>();
        for (PieEntry entry : entries) {
            colors.add(ChartUltils.getColorByCategory(entry.getLabel()));
        }
        dataSet.setColors(colors);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.WHITE);

        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.getDescription().setEnabled(false);
        pieChart.setCenterText(showingExpense ? "Chi tiêu" : "Thu nhập");
        pieChart.setHoleRadius(45f);
        pieChart.setTransparentCircleRadius(50f);
        pieChart.animateY(800);
        pieChart.invalidate();
    }

    // --- RecyclerView ---
    private void setupRecyclerData(List<Transaction> list) {
        Map<String, Long> map = new HashMap<>();
        List<ReportItem> reportList = new ArrayList<>();

        for (Transaction t : list){
            if (showingExpense && !t.getType().equals("EXPENSE")) continue;
            if (!showingExpense && !t.getType().equals("INCOME")) continue;
            map.put(t.getCategory(), map.getOrDefault(t.getCategory(), 0L) + (long) t.getAmount());
        }
        for (String cat : map.keySet()){
            reportList.add(new ReportItem(cat, map.get(cat), "#29B6F6"));
        }
        adapter.updateData(reportList, showingExpense);
    }

    // Chọn tháng - năm (DatePicker)
    private void showMonthYearPicker(){
        if (getContext() == null) return;
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_month_year_picker, null);
        NumberPicker npMonth = view.findViewById(R.id.npMonth);
        NumberPicker npYear = view.findViewById(R.id.npYear);
        TextView btnOk = view.findViewById(R.id.btnOk);
        TextView btnCancel = view.findViewById(R.id.btnCancel);

        // setup value
        npMonth.setMinValue(1);
        npMonth.setMaxValue(12);
        npMonth.setValue(currentMonth);
        npYear.setMinValue(2000);
        npYear.setMaxValue(2100);
        npYear.setValue(currentYear);

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(view)
                .create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnOk.setOnClickListener(v -> {
            currentMonth = npMonth.getValue();
            currentYear = npYear.getValue();
            updateUI();
            dialog.dismiss();
        });

        dialog.show();
    }
}
