package com.example.app6hu.utils;
import android.graphics.Color;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChartUltils {
    // Khai báo bảng màu để tránh random ra màu đen hoặc trắng
    public static final int[] COLORS = {
            Color.parseColor("#2196F3"), // Xanh dương (Blue)
            Color.parseColor("#F44336"), // Đỏ (Red)
            Color.parseColor("#FFC107"), // Vàng (Amber)
            Color.parseColor("#4CAF50"), // Xanh lá (Green)
            Color.parseColor("#9C27B0"), // Tím (Purple)
            Color.parseColor("#00BCD4"), // Cyan
            Color.parseColor("#FF9800"), // Cam (Orange)
            Color.parseColor("#795548"), // Nâu (Brown)
            Color.parseColor("#607D8B"), // Xanh xám (Blue Grey)
            Color.parseColor("#E91E63"), // Hồng (Pink)
            Color.parseColor("#CDDC39"), // Lime
            Color.parseColor("#3F51B5"), // Indigo
            Color.parseColor("#009688"), // Teal
            Color.parseColor("#FF5722"), // Deep Orange
            Color.parseColor("#673AB7"), // Deep Purple
            Color.parseColor("#8BC34A"), // Light Green
            Color.parseColor("#03A9F4"), // Light Blue
            Color.parseColor("#FFEB3B"), // Yellow
            Color.parseColor("#9E9E9E"), // Grey
            Color.parseColor("#C0CA33"), // Lime Dark
    };

    /** Hàm ghi nhớ màu đã cấp cho danh mục nào */
    private static final Map<String, Integer> colorMap = new HashMap<>();
    private static int nextColorIndex = 0; // con trỏ để lấy màu tiếp theo

    //Hàm lấy màu dựa trên tên
    public static int getColorByCategory(String categoryName) {
        if (categoryName == null) return Color.LTGRAY;
        if (colorMap.containsKey(categoryName)) {
            return colorMap.get(categoryName);
        }
        int color = COLORS[nextColorIndex % COLORS.length];
        colorMap.put(categoryName, color);
        nextColorIndex++;
        return color;
    }
    /** Cấu hình biểu đồ tròn (PieChart) */
    public static void setupPieChart(PieChart pieChart, List<PieEntry> entries) {
        PieDataSet dataSet = new PieDataSet(entries, "");
        List<Integer> colorsList = new ArrayList<>();
        for (PieEntry entry : entries) {
           colorsList.add(getColorByCategory(entry.getLabel()));
        }
        dataSet.setColors(colorsList);
        dataSet.setValueTextSize(10f);
        dataSet.setValueTextColor(Color.WHITE);

        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);
        pieChart.setDrawEntryLabels(false);
        pieChart.getDescription().setEnabled(false);
        pieChart.getLegend().setEnabled(true);
        pieChart.animateY(800);
        pieChart.invalidate();
    }

    /** Cấu hình biểu đồ cột (BarChart) */
    public static void setupBarChart(BarChart barChart, List<BarEntry> entries, List<String> months, int color) {
        BarDataSet dataSet = new BarDataSet(entries,"Chi tiết");
        dataSet.setColor(color);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(9f);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.7f);
        barChart.setData(barData);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(months));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);

        barChart.getDescription().setEnabled(false);
        barChart.getLegend().setEnabled(false);
        barChart.animateY(1000);
        barChart.invalidate();
    }

}
