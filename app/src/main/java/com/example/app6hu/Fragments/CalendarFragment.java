package com.example.app6hu.Fragments;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.app6hu.Adapter.CalendarDayAdapter;
import com.example.app6hu.Adapter.CalendarEntryAdapter;
import com.example.app6hu.R;
import com.example.app6hu.firebase.FirebasestoreManager;
import com.example.app6hu.model.Calendars;
import com.example.app6hu.model.Transaction;
import com.example.app6hu.utils.FormatUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CalendarFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;
    private ProgressBar progressBar;
    private GridView gridCalendar;
    private Map<String, Map<String, DaySummary>> monthlyCache = new HashMap<>();
    private CalendarDayAdapter adapter;
    private List<Integer> dayList;
    private Calendar currentCalendar;
    private TextView tvMonthYear;
    private ImageView btnPrevMonth, btnNextMonth, btnCalendarIcon;
    private ListView lvTransactions;
    private TextView tvTotalIncome, tvTotalExpense, tvTotalBalance;

    private Map<String, List<Transaction>> transactionMap = new HashMap<>();
    private Map<String, DaySummary> dailySummary = new HashMap<>();
    private List<Calendars> calendarEntries = new ArrayList<>();
    private CalendarEntryAdapter entryAdapter;

    // Map lưu vị trí đầu tiên của mỗi ngày trong list
    private Map<Integer, Integer> dayPositionMap = new HashMap<>();

    private long lastClickTime = 0;
    private int lastClickPosition = -1;

    public CalendarFragment() {
        // Required empty public constructor
    }

    public static class DaySummary {
        public double income = 0;
        public double expense = 0;
    }

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
        progressBar = v.findViewById(R.id.progressBar);

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

        calendarEntries = new ArrayList<>();
        entryAdapter = new CalendarEntryAdapter(requireContext(), calendarEntries);
        lvTransactions.setAdapter(entryAdapter);

        currentCalendar = Calendar.getInstance();
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
                int day = Math.abs(dayList.get(position));
                Toast.makeText(requireContext(), "Double click: Add Transaction Day " + day, Toast.LENGTH_SHORT).show();
            } else {
                int day = Math.abs(dayList.get(position));
                scrollToDay(day);
            }
            lastClickTime = clickTime;
            lastClickPosition = position;
        });

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateCalendar();
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

    private void scrollToDay(int day) {
        if (dayPositionMap.containsKey(day)) {
            int position = dayPositionMap.get(day);

            // Scroll đến vị trí của ngày
            lvTransactions.smoothScrollToPosition(position);

            // Sau khi scroll xong, highlight item đầu tiên của ngày
            lvTransactions.postDelayed(() -> {
                // Tính toán vị trí thực tế trên màn hình
                int firstVisible = lvTransactions.getFirstVisiblePosition();
                int lastVisible = lvTransactions.getLastVisiblePosition();

                if (position >= firstVisible && position <= lastVisible) {
                    // Item đã hiển thị trên màn hình
                    View view = lvTransactions.getChildAt(position - firstVisible);
                    if (view != null) {
                        // Highlight tạm thời
                        view.setBackgroundColor(Color.parseColor("#E3F2FD"));

                        // Xóa highlight sau 2 giây
                        view.postDelayed(() -> {
                            view.setBackgroundColor(Color.TRANSPARENT);
                        }, 2000);
                    }
                }
            }, 300); // Delay để đảm bảo scroll đã hoàn thành

            // Tính và hiển thị tổng của ngày được chọn
            calculateAndDisplayDayTotals(day);

            Toast.makeText(requireContext(), "Đã chuyển đến ngày " + day, Toast.LENGTH_SHORT).show();
        } else {
            lvTransactions.smoothScrollToPosition(0);
            Toast.makeText(requireContext(), "Ngày " + day + " không có giao dịch", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateSelectedDay(int selectedDay) {
        // Có thể thêm logic highlight ngày được chọn ở đây
        // Ví dụ: lưu lại ngày được chọn và cập nhật adapter
    }

    private void calculateAndDisplayDayTotals(int day) {
        double dayIncome = 0;
        double dayExpense = 0;

        for (Calendars entry : calendarEntries) {
            if (entry.getDay() == day) {
                if (entry.isExpense()) {
                    dayExpense += entry.getAmount();
                } else {
                    dayIncome += entry.getAmount();
                }
            }
        }

        displayDayTotals(dayIncome, dayExpense, day);
    }

    private void updateCalendar() {
        List<Integer> newDays = generateDaysForMonth(currentCalendar);
        dayList.clear();
        dayList.addAll(newDays);
        adapter.notifyDataSetChanged();

        int monthHuman = currentCalendar.get(Calendar.MONTH) + 1;
        int year = currentCalendar.get(Calendar.YEAR);
        tvMonthYear.setText(String.format(Locale.getDefault(), "%02d/%d", monthHuman, year));

        loadTransactionsFromFirebase();
    }

    private void loadTransactionsFromFirebase() {
        String cacheKey = currentCalendar.get(Calendar.YEAR) + "-" + (currentCalendar.get(Calendar.MONTH) + 1);

        if (monthlyCache.containsKey(cacheKey)) {
            dailySummary = monthlyCache.get(cacheKey);
            adapter.setDailySummary(dailySummary);
            adapter.notifyDataSetChanged();
            calculateAndDisplayMonthlyTotals();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        calendarEntries.clear();
        dayPositionMap.clear(); // Xóa map cũ

        int month = currentCalendar.get(Calendar.MONTH) + 1;
        int year = currentCalendar.get(Calendar.YEAR);

        FirebasestoreManager manager = new FirebasestoreManager();

        manager.getTransactionsByMonth(month, year, new FirebasestoreManager.FirestoreCallback<List<Transaction>>() {
            @Override
            public void onSuccess(List<Transaction> list) {
                progressBar.setVisibility(View.GONE);

                dailySummary.clear();
                calendarEntries.clear();
                dayPositionMap.clear();
                double totalMonthIncome = 0;
                double totalMonthExpense = 0;

                Calendar cal = Calendar.getInstance();
                Map<Integer, List<Transaction>> transactionsByDay = new HashMap<>();

                // Nhóm transaction theo ngày
                for (Transaction t : list) {
                    cal.setTime(t.getDate());
                    int day = cal.get(Calendar.DAY_OF_MONTH);

                    if (!transactionsByDay.containsKey(day)) {
                        transactionsByDay.put(day, new ArrayList<>());
                    }
                    transactionsByDay.get(day).add(t);

                    String key = String.valueOf(day);
                    if (!dailySummary.containsKey(key))
                        dailySummary.put(key, new DaySummary());

                    if (t.getType().equals("INCOME")) {
                        dailySummary.get(key).income += t.getAmount();
                        totalMonthIncome += t.getAmount();
                    } else {
                        dailySummary.get(key).expense += t.getAmount();
                        totalMonthExpense += t.getAmount();
                    }
                }

                // Sắp xếp các ngày theo thứ tự tăng dần
                List<Integer> sortedDays = new ArrayList<>(transactionsByDay.keySet());
                java.util.Collections.sort(sortedDays);

                // Chuyển đổi transactions thành Calendars entries và lưu vị trí
                for (int day : sortedDays) {
                    List<Transaction> dayTransactions = transactionsByDay.get(day);

                    // Lưu vị trí đầu tiên của ngày này
                    dayPositionMap.put(day, calendarEntries.size());

                    // Lấy thứ trong tuần
                    cal.set(Calendar.DAY_OF_MONTH, day);
                    cal.set(Calendar.MONTH, month - 1);
                    cal.set(Calendar.YEAR, year);
                    int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
                    String dayName = getDayName(dayOfWeek);

                    for (Transaction t : dayTransactions) {
                        Calendars calendarEntry = new Calendars();
                        calendarEntry.setDay(day);
                        calendarEntry.setDayName(dayName + ", " + day + "/" + month);
                        calendarEntry.setExpense(t.getType().equals("EXPENSE"));
                        calendarEntry.setAmount(t.getAmount());
                        calendarEntry.setCategory(t.getCategory());
                        calendarEntry.setDescription(t.getDetail());
                        calendarEntry.setIconResId(getIconForCategory(t.getCategory()));

                        calendarEntries.add(calendarEntry);
                    }
                }

                monthlyCache.put(cacheKey, new HashMap<>(dailySummary));
                adapter.setDailySummary(dailySummary);
                adapter.notifyDataSetChanged();

                // Cập nhật ListView với tất cả giao dịch
                entryAdapter.notifyDataSetChanged();
                lvTransactions.setAdapter(entryAdapter);

                displayMonthlyTotals(totalMonthIncome, totalMonthExpense);
            }

            @Override
            public void onFailure(Exception e) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                displayMonthlyTotals(0, 0);
            }
        });
    }

    private String getDayName(int dayOfWeek) {
        switch (dayOfWeek) {
            case Calendar.SUNDAY: return "CN";
            case Calendar.MONDAY: return "T2";
            case Calendar.TUESDAY: return "T3";
            case Calendar.WEDNESDAY: return "T4";
            case Calendar.THURSDAY: return "T5";
            case Calendar.FRIDAY: return "T6";
            case Calendar.SATURDAY: return "T7";
            default: return "";
        }
    }

    private int getIconForCategory(String category) {
        // Map category name to icon resource
        switch (category) {
            case "Ăn uống": return R.drawable.ic_food;
            case "Lương": return R.drawable.ic_salary;
            case "Giải trí": return R.drawable.ic_entertain;
            case "Đi lại": return R.drawable.ic_transport;
            case "Quần áo": return R.drawable.ic_clothes;
            case "Mỹ phẩm": return R.drawable.ic_cosmetic;
            case "Y tế": return R.drawable.ic_health;
            case "Giáo dục": return R.drawable.ic_education;
            case "Quà": return R.drawable.ic_gift;
            case "Hóa đơn": return R.drawable.ic_bill;
            case "Tiền điện": return R.drawable.ic_electric;
            case "Thưởng": return R.drawable.ic_bonus;
            case "Phụ cấp": return R.drawable.ic_allowance;
            case "Đầu tư": return R.drawable.ic_invest;
            default: return R.drawable.ic_home;
        }
    }

    private void displayMonthlyTotals(double totalIncome, double totalExpense) {
        double totalBalance = totalIncome - totalExpense;

        tvTotalIncome.setText("+" + FormatUtils.formatCurrency(totalIncome));
        tvTotalExpense.setText("-" + FormatUtils.formatCurrency(totalExpense));

        String balanceText = FormatUtils.formatCurrency(Math.abs(totalBalance));
        if (totalBalance > 0) {
            tvTotalBalance.setText("+" + balanceText);
            tvTotalBalance.setTextColor(getResources().getColor(android.R.color.holo_green_dark, null));
        } else if (totalBalance < 0) {
            tvTotalBalance.setText("-" + balanceText);
            tvTotalBalance.setTextColor(getResources().getColor(android.R.color.holo_red_dark, null));
        } else {
            tvTotalBalance.setText(FormatUtils.formatCurrency(0));
            tvTotalBalance.setTextColor(Color.BLACK);
        }
    }

    private void displayDayTotals(double dayIncome, double dayExpense, int day) {
        // Hiển thị tổng của ngày được click
        tvTotalIncome.setText("+" + FormatUtils.formatCurrency(dayIncome));
        tvTotalExpense.setText("-" + FormatUtils.formatCurrency(dayExpense));

        double dayBalance = dayIncome - dayExpense;
        String balanceText = FormatUtils.formatCurrency(Math.abs(dayBalance));
        if (dayBalance > 0) {
            tvTotalBalance.setText("+" + balanceText);
            tvTotalBalance.setTextColor(getResources().getColor(android.R.color.holo_green_dark, null));
        } else if (dayBalance < 0) {
            tvTotalBalance.setText("-" + balanceText);
            tvTotalBalance.setTextColor(getResources().getColor(android.R.color.holo_red_dark, null));
        } else {
            tvTotalBalance.setText(FormatUtils.formatCurrency(0));
            tvTotalBalance.setTextColor(Color.BLACK);
        }
    }

    private void calculateAndDisplayMonthlyTotals() {
        double totalMonthIncome = 0;
        double totalMonthExpense = 0;

        for (DaySummary summary : dailySummary.values()) {
            totalMonthIncome += summary.income;
            totalMonthExpense += summary.expense;
        }

        displayMonthlyTotals(totalMonthIncome, totalMonthExpense);
    }

    private List<Integer> generateDaysForMonth(Calendar cal) {
        List<Integer> days = new ArrayList<>();

        Calendar calendar = (Calendar) cal.clone();
        calendar.set(Calendar.DAY_OF_MONTH, 1);

        int dow = calendar.get(Calendar.DAY_OF_WEEK);
        int offset = (dow + 5) % 7; // Monday=0

        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        // prev month
        Calendar prev = (Calendar) calendar.clone();
        prev.add(Calendar.MONTH, -1);
        int prevMonthDays = prev.getActualMaximum(Calendar.DAY_OF_MONTH);

        // ADD BLANK DAYS – CHỈ 5 TUẦN (35 ô)
        for (int i = 0; i < offset; i++) {
            days.add(-(prevMonthDays - offset + 1 + i));
        }

        // current month
        for (int i = 1; i <= daysInMonth; i++) {
            days.add(i);
        }

        // next month - CHỈ ĐẾN KHI ĐỦ 35 Ô
        while (days.size() < 35) {
            days.add(-(days.size() - daysInMonth - offset + 1));
        }

        return days;
    }
}