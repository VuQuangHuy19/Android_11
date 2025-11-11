package com.example.app6hu.Adapter;
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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
public class ReportAdapter {

    private TextView tvMonthYear, tvIncome, tvExpense, tvBalance, tabExpense, tabIncome;
    private ImageView btnPrevMonth, btnNextMonth;
    private PieChart pieChart;
    private RecyclerView recyclerReport;
    private boolean showingExpense = true;
    private int currentMonth, currentYear;
    private ReportItemAdapter adapter;
}
