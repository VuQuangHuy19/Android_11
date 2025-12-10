package com.example.app6hu.utils;

import static android.content.ContentValues.TAG;

import android.util.Log;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FormatUtils {

    /** Định dạng tiền Việt Nam: 12.345.678đ */
    public static String formatCurrency(double amount) {
        DecimalFormat formatter = new DecimalFormat("#,###");
        return formatter.format(amount) + "đ";
    }

    /** Định dạng tiền với dấu +/- tùy theo giá trị */
    public static String formatCurrencyWithSign(double amount) {
        if (amount >= 0) {
            return "+" + formatCurrency(amount);
        } else {
            return "-" + formatCurrency(Math.abs(amount));
        }
    }

    /** Định dạng ngày: "dd/MM/yyyy" */
    public static String formatDate(Date date) {
        if (date == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return sdf.format(date);
    }

    /** Định dạng ngày tháng: "Tháng MM/yyyy" */
    public static String formatMonthYear(Date date) {
        if (date == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("MM/yyyy", Locale.getDefault());
        return sdf.format(date);
    }

    /** Định dạng ngày tháng năm: "Ngày dd Tháng MM năm yyyy" */
    public static String formatFullDate(Date date) {
        if (date == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("'Ngày' dd 'Tháng' MM 'năm' yyyy", Locale.getDefault());
        return sdf.format(date);
    }

    /** Định dạng số phần trăm: 12.34% */
    public static String formatPercentage(double value) {
        DecimalFormat formatter = new DecimalFormat("#,##0.00");
        return formatter.format(value) + "%";
    }

    /** Định dạng số với dấu phẩy phân cách hàng nghìn */
    public static String formatNumber(double number) {
        DecimalFormat formatter = new DecimalFormat("#,###");
        return formatter.format(number);
    }

    /** Định dạng số với số thập phân */
    public static String formatDecimal(double number, int decimalPlaces) {
        String pattern = "#,###.";
        for (int i = 0; i < decimalPlaces; i++) {
            pattern += "0";
        }
        DecimalFormat formatter = new DecimalFormat(pattern);
        return formatter.format(number);
    }
    public static String formatCurrencyNoSymbol(double amount) {
        try {
            // Sử dụng Locale.US để có dấu phân cách là dấu chấm (.)
            DecimalFormat formatter = new DecimalFormat("#,###", DecimalFormatSymbols.getInstance(Locale.US));
            return formatter.format(amount);
        } catch (Exception e) {
            // Fallback nếu có lỗi
            return String.format(Locale.US, "%.0f", amount);
        }
    }
    public static int safeCastToInt(long id) {
        if (id > Integer.MAX_VALUE) {
            Log.w(TAG, "Transaction ID too large: " + id);
            return (int) (id % Integer.MAX_VALUE); // hoặc return 0;
        }
        return (int) id;
    }
}