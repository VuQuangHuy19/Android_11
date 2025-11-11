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

    private long lastClickTime = 0;
    private int lastClickPosition = -1;

    public CalendarFragment() {
        // Required empty public constructor
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
        generateFakeData();
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
        String key = getDateKey(currentCalendar.get(Calendar.YEAR),
                currentCalendar.get(Calendar.MONTH),
                day);

        List<Transaction> list = transactionMap.getOrDefault(key, new ArrayList<>());
        List<String> displayList = new ArrayList<>();
        double totalIncome = 0, totalExpense = 0;

        displayList.add(String.format("Ngày %d/%d/%d - Thu chi tổng", day,
                currentCalendar.get(Calendar.MONTH)+1,
                currentCalendar.get(Calendar.YEAR)));

        for (Transaction t : list) {
            String str = String.format("%s - %s - %s - %s", t.icon, t.category, t.detail,
                    t.type.equals("INCOME") ? "+" + t.amount : "-" + t.amount);
            displayList.add(str);
            if (t.type.equals("INCOME")) totalIncome += t.amount;
            else totalExpense += t.amount;
        }

        lvTransactions.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_list_item_1, displayList));
        tvTotalIncome.setText(String.format("%.0fđ", totalIncome));
        tvTotalExpense.setText(String.format("%.0fđ", totalExpense));
        tvTotalBalance.setText(String.format("%.0fđ", totalIncome - totalExpense));
    }

    private void updateCalendar() {
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

    private void generateFakeData() {
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
    }

    // Thêm constructor cho Transaction
    static class Transaction {
        String type;
        double amount;
        String category;
        String detail;
        String icon;

        // Constructor đầy đủ
        public Transaction(String type, double amount, String category, String detail, String icon) {
            this.type = type;
            this.amount = amount;
            this.category = category;
            this.detail = detail;
            this.icon = icon;
        }

        public Transaction() {} // default
    }

    /**
     * Trả về danh sách 42 phần tử:
     * - số dương => ngày của tháng hiện tại (1..daysInMonth)
     * - số âm => ngày của tháng khác (âm để đánh dấu mờ)
     */
    private List<Integer> generateDaysForMonth(Calendar cal) {
        List<Integer> days = new ArrayList<>();

        // clone để không mutate cal bên ngoài
        Calendar calendar = (Calendar) cal.clone();
        calendar.set(Calendar.DAY_OF_MONTH, 1);

        // Lấy thứ của ngày 1 (Calendar.SUNDAY=1 ... SATURDAY=7)
        int dow = calendar.get(Calendar.DAY_OF_WEEK); // 1..7

        // Chúng ta muốn Monday = 0 ... Sunday = 6 (cột 0 = Mon)
        int offset = (dow + 5) % 7; // transform: Sun(1)->6, Mon(2)->0, Tue(3)->1, ...

        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        // Prev month days: clone và lùi 1 tháng
        Calendar prev = (Calendar) calendar.clone();
        prev.add(Calendar.MONTH, -1);
        int prevMonthDays = prev.getActualMaximum(Calendar.DAY_OF_MONTH);

        // Add trailing days from prev month (as negative numbers, to mark them)
        for (int i = offset - 1; i >= 0; i--) {
            int dayFromPrev = prevMonthDays - i;
            days.add(-dayFromPrev); // negative => month before
        }

        // Add current month days
        for (int i = 1; i <= daysInMonth; i++) {
            days.add(i);
        }

        // Add leading days for next month until we have 42 cells (6x7)
        int total = days.size();
        int need = 42 - total;
        for (int i = 1; i <= need; i++) {
            days.add(-i); // negative => month after (use -i)
        }

        return days;
    }
}