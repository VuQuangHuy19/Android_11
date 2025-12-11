package com.example.app6hu.utils;

import java.text.NumberFormat;
import java.util.Locale;

public class MoneyUtils {
    public static String format(long money) {
        return NumberFormat.getInstance(new Locale("vi", "VN"))
                .format(money) + " đ";
    }
}
