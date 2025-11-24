package com.example.app6hu.Fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.app6hu.Adapter.CalendarDayAdapter;
import com.example.app6hu.R;
import com.example.app6hu.firebase.FirebasestoreManager;
import com.example.app6hu.model.Transaction;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CalendarFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CalendarFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private GridView gridCalendar;
    private CalendarDayAdapter adapter;
    private List<Integer> dayList;
    private Calendar currentCalendar;
    private TextView tvMonthYear;
    private ImageView btnPrevMonth, btnNextMonth, btnCalendarIcon;
    private ListView lvTransactions;
    private TextView tvTotalIncome, tvTotalExpense, tvTotalBalance;

    // fake database
    private Map<String, List<Transaction>> transactionMap = new HashMap<>();
    private Map<String, DaySummary> dailySummary = new HashMap<>();


    private long lastClickTime = 0;
    private int lastClickPosition = -1;

    public CalendarFragment() {
        // Required empty public constructor
    }
    public static class DaySummary {
        public double income = 0;
        public double expense = 0;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CalendarFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CalendarFragment newInstance(String param1, String param2) {
        CalendarFragment fragment = new CalendarFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_calendar, container, false);

        gridCalendar = v.findViewById(R.id.gridCalendar);
        tvMonthYear = v.findViewById(R.id.tvMonthYear);
        btnPrevMonth = v.findViewById(R.id.btnPrevMonth);
        btnNextMonth = v.findViewById(R.id.btnNextMonth);
        btnCalendarIcon = v.findViewById(R.id.btnCalendarIcon);
        lvTransactions = v.findViewById(R.id.lvTransactions);
        tvTotalIncome = v.findViewById(R.id.tvTotalIncome);
        tvTotalExpense = v.findViewById(R.id.tvTotalExpense);
        tvTotalBalance = v.findViewById(R.id.tvTotalBalance);

        dayList = new ArrayList<>();
        adapter = new CalendarDayAdapter(requireContext(), dayList);
        gridCalendar.setAdapter(adapter);

        currentCalendar = Calendar.getInstance();
        //generateFakeData();
        updateCalendar();

        btnPrevMonth.setOnClickListener(view -> {
            currentCalendar.add(Calendar.MONTH, -1);
            updateCalendar();
        });

        btnNextMonth.setOnClickListener(view -> {
            currentCalendar.add(Calendar.MONTH, 1);
            updateCalendar();
        });

        btnCalendarIcon.setOnClickListener(view -> showMonthYearPicker());

        gridCalendar.setOnItemClickListener((parent, view1, position, id) -> {
            long clickTime = System.currentTimeMillis();
            if (lastClickPosition == position && clickTime - lastClickTime < 400) {
                // double click
                int day = Math.abs(dayList.get(position));
                Toast.makeText(requireContext(), "Double click: Add Transaction Day " + day, Toast.LENGTH_SHORT).show();
            } else {
                // single click
                int day = Math.abs(dayList.get(position));
                showTransactionsOfDay(day);
            }
            lastClickTime = clickTime;
            lastClickPosition = position;
        });

        return v;
    }

    private void showMonthYearPicker() {
        DatePickerDialog dialog = new DatePickerDialog(requireContext(),
                (view, year, month, dayOfMonth) -> {
                    currentCalendar.set(Calendar.YEAR, year);
                    currentCalendar.set(Calendar.MONTH, month);
                    updateCalendar();
                },
                currentCalendar.get(Calendar.YEAR),
                currentCalendar.get(Calendar.MONTH),
                currentCalendar.get(Calendar.DAY_OF_MONTH)
        );
        dialog.getDatePicker().setCalendarViewShown(false);
        dialog.show();
    }

    private void showTransactionsOfDay(int day) {
        int month = currentCalendar.get(Calendar.MONTH) + 1;
        int year = currentCalendar.get(Calendar.YEAR);

        FirebasestoreManager manager = new FirebasestoreManager();
        manager.getTransactionsByMonth(month, year, new FirebasestoreManager.FirestoreCallback<List<Transaction>>() {
            @Override
            public void onSuccess(List<Transaction> list) {

                List<Transaction> result = new ArrayList<>();
                Calendar cal = Calendar.getInstance();

                for (Transaction t : list) {
                    cal.setTime(t.getDate());
                    if (cal.get(Calendar.DAY_OF_MONTH) == day) {
                        result.add(t);
                    }
                }

                showListViewData(result, day);
            }

            @Override
            public void onFailure(Exception e) {
            }
        });
    }

    private void updateCalendar() {
        loadTransactionsFromFirebase();

        // tạo dữ liệu mới cho dayList
        List<Integer> newDays = generateDaysForMonth(currentCalendar);

        // cập nhật list trong adapter (thay vì tạo adapter mới)
        dayList.clear();
        dayList.addAll(newDays);
        adapter.notifyDataSetChanged();

        // hiển thị tháng + năm cho người dùng (1..12)
        int monthHuman = currentCalendar.get(Calendar.MONTH) + 1;
        int year = currentCalendar.get(Calendar.YEAR);
        // format đẹp: "11/2025"
        tvMonthYear.setText(String.format(Locale.getDefault(), "%02d/%d", monthHuman, year));
    }
    private String getDateKey(int year, int month, int day) {
        return String.format("%04d-%02d-%02d", year, month, day); }

    /*private void generateFakeData() {
        // Fake data cố định cho tháng 11/2025
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, 2025);
        cal.set(Calendar.MONTH, Calendar.NOVEMBER); // Tháng 11

        for (int d = 1; d <= 30; d++) { // giả lập 30 ngày
            List<Transaction> list = new ArrayList<>();

            // Thu nhập giả lập
            Transaction income = new Transaction(
                    "INCOME",
                    50000 + d * 1000,
                    "Lương",
                    "Thu nhập ngày " + d,
                    "\uD83D\uDCB0" // 💰
            );
            list.add(income);

            // Chi tiêu giả lập
            Transaction expense = new Transaction(
                    "EXPENSE",
                    10000 + d * 500,
                    "Ăn uống",
                    "Chi tiêu ngày " + d,
                    "\uD83D\uDCB8" // 💸
            );
            list.add(expense);

            transactionMap.put(getDateKey(2025, Calendar.NOVEMBER, d), list);
        }
    }*/
    private void showListViewData(List<Transaction> list, int day) {

        // Hiển thị giao dịch vào ListView
        List<String> displayList = new ArrayList<>();

        double income = 0;
        double expense = 0;

        for (Transaction t : list) {
            String line = (t.getType().equals("INCOME") ? "[Thu] " : "[Chi] ")
                    + t.getAmount()
                    + " - " + t.getCategory()
                    + " - " + t.getDetail();
            displayList.add(line);

            if (t.getType().equals("INCOME")) {
                income += t.getAmount();
            } else {
                expense += t.getAmount();
            }
        }

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(requireContext(),
                        android.R.layout.simple_list_item_1,
                        displayList);

        lvTransactions.setAdapter(adapter);

        // Cập nhật TextView tổng thu - chi - số dư
        tvTotalIncome.setText("+" + (int) income);
        tvTotalExpense.setText("-" + (int) expense);
        tvTotalBalance.setText(String.valueOf((int) (income - expense)));

        Toast.makeText(requireContext(),
                "Giao dịch ngày " + day,
                Toast.LENGTH_SHORT).show();
    }

    private void loadTransactionsFromFirebase() {
        int month = currentCalendar.get(Calendar.MONTH) + 1;  // 1..12
        int year = currentCalendar.get(Calendar.YEAR);

        FirebasestoreManager manager = new FirebasestoreManager();

        manager.getTransactionsByMonth(month, year, new FirebasestoreManager.FirestoreCallback<List<Transaction>>() {
            @Override
            public void onSuccess(List<Transaction> list) {

                dailySummary.clear();

                Calendar cal = Calendar.getInstance();

                for (Transaction t : list) {
                    cal.setTime(t.getDate());
                    int day = cal.get(Calendar.DAY_OF_MONTH);

                    String key = String.valueOf(day);

                    if (!dailySummary.containsKey(key))
                        dailySummary.put(key, new DaySummary());

                    if (t.getType().equals("INCOME"))
                        dailySummary.get(key).income += t.getAmount();
                    else
                        dailySummary.get(key).expense += t.getAmount();
                }

                adapter.setDailySummary(dailySummary);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Exception e) { }
        });
    }

    /**
     * Trả về danh sách 42 phần tử:
     * - số dương => ngày của tháng hiện tại (1..daysInMonth)
     * - số âm => ngày của tháng khác (âm để đánh dấu mờ)
     */
    private List<Integer> generateDaysForMonth(Calendar cal) {
        List<Integer> days = new ArrayList<>();

        Calendar calendar = (Calendar) cal.clone();
        calendar.set(Calendar.DAY_OF_MONTH, 1);

        int dow = calendar.get(Calendar.DAY_OF_WEEK); // 1..7
        int offset = (dow + 5) % 7; // Monday=0

        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        // prev month
        Calendar prev = (Calendar) calendar.clone();
        prev.add(Calendar.MONTH, -1);
        int prevMonthDays = prev.getActualMaximum(Calendar.DAY_OF_MONTH);

        // ADD BLANK DAYS – FIXED
        for (int i = 0; i < offset; i++) {
            days.add(-(prevMonthDays - offset + 1 + i)); // luôn chính xác số ngày
        }

        // current month
        for (int i = 1; i <= daysInMonth; i++) {
            days.add(i);
        }

        // next month
        while (days.size() < 42) {
            days.add(-(days.size() - daysInMonth - offset + 1));
        }

        return days;
    }

}