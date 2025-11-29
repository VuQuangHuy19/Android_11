package com.example.app6hu.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {
    public static boolean isExpired(String deadline) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date today = new Date();
            Date end = sdf.parse(deadline);
            return today.after(end);
        } catch (Exception e) {
            return false;
        }
    }
}
