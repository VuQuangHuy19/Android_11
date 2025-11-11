package com.example.app6hu.utils;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
public class FormatUtils {

    /** Định dạng tiền Việt Nam: 12.345.678đ */
    public static String formatCurrency(double amount) {
        DecimalFormat formatter = new DecimalFormat("#,###");
        return formatter.format(amount) + "đ";
    }

    /** Định dạng ngày: "yyyy-MM-dd" → "dd/MM/yyyy" */
    public static String formatDate(Date date) {
        if (date == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return sdf.format(date);
    }
}
