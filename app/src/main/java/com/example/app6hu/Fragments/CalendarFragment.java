package com.example.app6hu.Fragments;

import static android.content.ContentValues.TAG;

import static com.example.app6hu.utils.FormatUtils.safeCastToInt;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.app6hu.Activities.EditTransactionActivity;
import com.example.app6hu.Activities.FindTransctionActivity;
import com.example.app6hu.Adapter.CalendarDayAdapter;
import com.example.app6hu.Adapter.CalendarEntryAdapter;
import com.example.app6hu.R;
import com.example.app6hu.firebase.FirebasestoreManager;
import com.example.app6hu.model.Calendars;
import com.example.app6hu.model.Transaction;
import com.example.app6hu.utils.FormatUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
    private ImageView btnPrevMonth, btnNextMonth, btnCalendarIcon, btnsearch;
    private ListView lvTransactions;
    private TextView tvTotalIncome, tvTotalExpense, tvTotalBalance;

    private Map<String, List<Transaction>> transactionMap = new HashMap<>();
    private Map<String, DaySummary> dailySummary = new HashMap<>();
    private List<Calendars> calendarEntries = new ArrayList<>();
    private CalendarEntryAdapter entryAdapter;

    // Map lưu vị trí đầu tiên của mỗi ngày trong list
    private Map<Integer, Integer> dayPositionMap = new HashMap<>();

    // Biến xử lý double click
    private long lastListItemClickTime = 0;
    private int lastListItemClickPosition = -1;
    private int currentHighlightedPosition = -1;

    // Biến lưu tổng tháng
    private double monthlyTotalIncome = 0;
    private double monthlyTotalExpense = 0;

    private long lastClickTime = 0;
    private int lastClickPosition = -1;

    // Thêm biến để kiểm soát force reload
    private boolean forceReload = false;

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

        btnsearch = v.findViewById(R.id.btnSearch);
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
            // Xóa cache của tháng mới để force reload
            String newCacheKey = currentCalendar.get(Calendar.YEAR) + "-" + (currentCalendar.get(Calendar.MONTH) + 1);
            monthlyCache.remove(newCacheKey);
            updateCalendar();
        });

        btnNextMonth.setOnClickListener(view -> {
            currentCalendar.add(Calendar.MONTH, 1);
            // Xóa cache của tháng mới để force reload
            String newCacheKey = currentCalendar.get(Calendar.YEAR) + "-" + (currentCalendar.get(Calendar.MONTH) + 1);
            monthlyCache.remove(newCacheKey);
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

        // Xử lý click cho ListView (giao dịch)
        lvTransactions.setOnItemClickListener((parent, view, position, id) -> {
            long clickTime = System.currentTimeMillis();

            // Kiểm tra double click (400ms)
            if (lastListItemClickPosition == position && clickTime - lastListItemClickTime < 400) {
                // Double click - mở EditTransaction
                if (position < calendarEntries.size()) {
                    Calendars selectedEntry = calendarEntries.get(position);
                    openEditTransaction(selectedEntry);
                }
            } else {
                // Single click - chỉ highlight
                view.setBackgroundColor(Color.parseColor("#F0F8FF"));
                view.postDelayed(() -> {
                    view.setBackgroundColor(Color.TRANSPARENT);
                }, 200);
            }

            lastListItemClickTime = clickTime;
            lastListItemClickPosition = position;
        });

        btnsearch.setOnClickListener(view -> {
            Intent intent = new Intent(requireContext(), FindTransctionActivity.class);
            startActivity(intent);
        });

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Khi quay lại Fragment, load lại dữ liệu tháng hiện tại
        forceReload = true;
        updateCalendar();
    }

    private void showMonthYearPicker() {
        DatePickerDialog dialog = new DatePickerDialog(requireContext(),
                (view, year, month, dayOfMonth) -> {
                    currentCalendar.set(Calendar.YEAR, year);
                    currentCalendar.set(Calendar.MONTH, month);
                    forceReload = true; // BẬT force reload
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

            // Kiểm tra xem item đã hiển thị chưa
            int firstVisible = lvTransactions.getFirstVisiblePosition();
            int lastVisible = lvTransactions.getLastVisiblePosition();

            if (position >= firstVisible && position <= lastVisible) {
                // Item đã hiển thị, chỉ cần highlight
                highlightDayItem(position);
            } else {
                // Item chưa hiển thị, scroll đến nó trước
                smoothScrollToPositionExact(position);

                // Delay để đợi scroll hoàn thành rồi highlight
                lvTransactions.postDelayed(() -> {
                    highlightDayItem(position);
                }, 350);
            }

            // Hiển thị tổng ngày
            showDaySummary(day);

        } else {
            lvTransactions.smoothScrollToPosition(0);
            Toast.makeText(requireContext(), "Ngày " + day + " không có giao dịch",
                    Toast.LENGTH_SHORT).show();
        }
    }

    // Sửa phương thức này để không dùng trên ListView object


    private void highlightDayItem(int position) {
        // Reset tất cả highlight trước đó
        resetAllHighlights();

        // Tìm view tại vị trí này
        int firstVisible = lvTransactions.getFirstVisiblePosition();
        int lastVisible = lvTransactions.getLastVisiblePosition();

        if (position >= firstVisible && position <= lastVisible) {
            View view = lvTransactions.getChildAt(position - firstVisible);
            if (view != null) {
                // Highlight với animation
                highlightViewWithAnimation(view);

                // Lưu vị trí đang được highlight
                saveHighlightedPosition(position);
            }
        }
    }

    private void highlightViewWithAnimation(View view) {
        // Sử dụng ValueAnimator để tạo hiệu ứng mượt mà
        ValueAnimator animator = ValueAnimator.ofArgb(
                Color.TRANSPARENT,
                Color.parseColor("#E3F2FD")
        );
        animator.setDuration(300);
        animator.addUpdateListener(animation -> {
            view.setBackgroundColor((int) animation.getAnimatedValue());
        });
        animator.start();

        // Tự động xóa highlight sau 3 giây
        view.postDelayed(() -> {
            if (view.getBackground() != null) {
                ValueAnimator fadeOut = ValueAnimator.ofArgb(
                        Color.parseColor("#E3F2FD"),
                        Color.TRANSPARENT
                );
                fadeOut.setDuration(500);
                fadeOut.addUpdateListener(animation -> {
                    view.setBackgroundColor((int) animation.getAnimatedValue());
                });
                fadeOut.start();
            }
        }, 3000);
    }

    private void saveHighlightedPosition(int position) {
        // Bạn có thể lưu position này nếu cần
        currentHighlightedPosition = position;
    }

    private void resetAllHighlights() {
        int firstVisible = lvTransactions.getFirstVisiblePosition();
        int lastVisible = lvTransactions.getLastVisiblePosition();

        for (int i = firstVisible; i <= lastVisible; i++) {
            View view = lvTransactions.getChildAt(i - firstVisible);
            if (view != null) {
                view.setBackgroundColor(Color.TRANSPARENT);
            }
        }
    }

    private void smoothScrollToPositionExact(int position) {
        // Đảm bảo ListView đã layout xong
        lvTransactions.post(() -> {
            // Scroll đến vị trí và đặt item ở đầu list
            lvTransactions.setSelectionFromTop(position, lvTransactions.getPaddingTop());

            // Nếu muốn có animation mượt mà
            lvTransactions.postDelayed(() -> {
                lvTransactions.smoothScrollToPosition(position);
            }, 100);
        });
    }

    private void showDaySummary(int day) {
        double dayIncome = 0;
        double dayExpense = 0;

        // Tính tổng nhanh từ dailySummary
        String key = String.valueOf(day);
        if (dailySummary.containsKey(key)) {
            DaySummary summary = dailySummary.get(key);
            dayIncome = summary.income;
            dayExpense = summary.expense;
        }

        double dayBalance = dayIncome - dayExpense;
        String daySummary = String.format(Locale.getDefault(),
                "Ngày %d: Thu: %s, Chi: %s, Tổng: %s",
                day,
                FormatUtils.formatCurrency(dayIncome),
                FormatUtils.formatCurrency(dayExpense),
                FormatUtils.formatCurrency(dayBalance));

        Toast.makeText(requireContext(), daySummary, Toast.LENGTH_SHORT).show();
    }


    // Mở EditTransactionActivity khi double click
    // Mở EditTransactionActivity khi double click
    private void openEditTransaction(Calendars calendarEntry) {
        if (calendarEntry.getTransactionId() == 0) {
            Toast.makeText(requireContext(), "Không thể chỉnh sửa giao dịch này", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "=== Opening Edit Transaction ===");
        Log.d(TAG, "Transaction ID: " + calendarEntry.getTransactionId());
        Log.d(TAG, "Document ID: " + calendarEntry.getDocumentId());
        Log.d(TAG, "Category: " + calendarEntry.getCategory());
        Log.d(TAG, "Amount: " + calendarEntry.getAmount());

        // DEBUG: Kiểm tra documentId có hợp lệ không
        if (calendarEntry.getDocumentId() == null || calendarEntry.getDocumentId().isEmpty()) {
            Log.e(TAG, "✗✗✗ ERROR: Calendars entry has NO documentId!");
            Log.e(TAG, "DocumentId is null or empty!");
        } else if (calendarEntry.getDocumentId().startsWith("temp_")) {
            Log.w(TAG, "⚠️ WARNING: DocumentId is temporary: " + calendarEntry.getDocumentId());
        }

        Intent intent = new Intent(requireContext(), EditTransactionActivity.class);

        // THÊM DEBUG VÀO MỖI putExtra
        Log.d(TAG, "Putting Extra: transaction_document_id = " + calendarEntry.getDocumentId());
        intent.putExtra("transaction_document_id", calendarEntry.getDocumentId());

        Log.d(TAG, "Putting Extra: transaction_id = " + calendarEntry.getTransactionId());
        intent.putExtra("transaction_id", calendarEntry.getTransactionId());

        Log.d(TAG, "Putting Extra: transaction_type = " + (calendarEntry.isExpense() ? "EXPENSE" : "INCOME"));
        intent.putExtra("transaction_type", calendarEntry.isExpense() ? "EXPENSE" : "INCOME");

        intent.putExtra("transaction_amount", calendarEntry.getAmount());
        intent.putExtra("transaction_category", calendarEntry.getCategory()); // Tên danh mục
        intent.putExtra("transaction_category_id", ""); // THÊM CATEGORY ID NẾU CÓ
        intent.putExtra("transaction_detail", calendarEntry.getDescription());
        intent.putExtra("transaction_day", calendarEntry.getDay());

        Log.d(TAG, "Starting EditTransactionActivity with documentId: " + calendarEntry.getDocumentId());
        Log.d(TAG, "Starting EditTransactionActivity...");
        Log.d(TAG, "Category to highlight: " + calendarEntry.getCategory());
        startActivityForResult(intent, 100);
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
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            // Refresh data khi quay lại từ EditTransactionActivity
            forceReload = true;
            updateCalendar();
        }
    }
    private void loadTransactionsFromFirebase() {
        String cacheKey = currentCalendar.get(Calendar.YEAR) + "-" + (currentCalendar.get(Calendar.MONTH) + 1);

        if (!forceReload && monthlyCache.containsKey(cacheKey)) {
            dailySummary = monthlyCache.get(cacheKey);
            adapter.setDailySummary(dailySummary);
            adapter.notifyDataSetChanged();
            calculateAndDisplayMonthlyTotals();
            loadCalendarEntriesFromCache(cacheKey);
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        calendarEntries.clear();
        dayPositionMap.clear();
        monthlyTotalIncome = 0;
        monthlyTotalExpense = 0;

        int month = currentCalendar.get(Calendar.MONTH) + 1;
        int year = currentCalendar.get(Calendar.YEAR);

        FirebasestoreManager manager = new FirebasestoreManager();

        manager.getTransactionsByMonth(month, year, new FirebasestoreManager.FirestoreCallback<List<Transaction>>() {
            @Override
            public void onSuccess(List<Transaction> list) {
                progressBar.setVisibility(View.GONE);
                forceReload = false;

                dailySummary.clear();
                calendarEntries.clear();
                dayPositionMap.clear();
                monthlyTotalIncome = 0;
                monthlyTotalExpense = 0;

                Calendar cal = Calendar.getInstance();
                Map<Integer, List<Transaction>> transactionsByDay = new HashMap<>();

                // Nhóm transaction theo ngày
                for (Transaction t : list) {
                    // Debug từng transaction
                    Log.d(TAG, "Processing Transaction: " +
                            "ID=" + t.getId() +
                            ", DocumentID=" + t.getDocumentId() +
                            ", Category=" + t.getCategory());
                    if (t.getDate() == null) {
                        Log.w(TAG, "Transaction has null date, skipping or setting current date");
                        continue; // Hoặc set t.setDate(new Date()) nếu muốn
                    }

                    // Validate date của transaction
                    Date validDate = validateTransactionDate(t.getDate());
                    if (!validDate.equals(t.getDate())) {
                        t.setDate(validDate); // Cập nhật date hợp lệ
                    }

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
                        monthlyTotalIncome += t.getAmount();
                    } else {
                        dailySummary.get(key).expense += t.getAmount();
                        monthlyTotalExpense += t.getAmount();
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

                    // Format ngày hiển thị
                    String formattedDay = String.format(Locale.getDefault(), "%s, %02d/%02d",
                            dayName, day, month);

                    for (Transaction t : dayTransactions) {
                        Calendars calendarEntry = new Calendars();
                        calendarEntry.setDay(day);
                        calendarEntry.setDayName(formattedDay);
                        calendarEntry.setExpense(t.getType().equals("EXPENSE"));
                        calendarEntry.setAmount(t.getAmount());
                        calendarEntry.setCategory(t.getCategory());
                        calendarEntry.setDescription(t.getDetail());
                        calendarEntry.setIconResId(getIconForCategory(t.getCategory()));

                        // QUAN TRỌNG: Lưu transaction ID và date
                        calendarEntry.setTransactionId(safeCastToInt(t.getId()));
                        calendarEntry.setTransactionDate(t.getDate());

                        // Lấy documentId từ Transaction (đã được set trong FirebasestoreManager)
                        if (t.getDocumentId() != null && !t.getDocumentId().isEmpty()) {
                            calendarEntry.setDocumentId(t.getDocumentId());
                            Log.d(TAG, "✓ Set documentId: " + t.getDocumentId());
                        } else {
                            // Fallback nếu không có documentId

                            calendarEntry.setDocumentId("temp_" + t.getId());
                            Log.e(TAG, "✗ ERROR: Transaction has no documentId! ID=" + t.getId());
                        }

                        calendarEntries.add(calendarEntry);

                        // Debug log
                        System.out.println("Added transaction: " + t.getCategory() +
                                ", Amount: " + t.getAmount() +
                                ", Document ID: " + calendarEntry.getDocumentId());
                    }
                }

                // Lưu vào cache
                monthlyCache.put(cacheKey, new HashMap<>(dailySummary));

                adapter.setDailySummary(dailySummary);
                adapter.notifyDataSetChanged();

                // Cập nhật ListView với tất cả giao dịch
                entryAdapter.notifyDataSetChanged();
                lvTransactions.setAdapter(entryAdapter);

                displayMonthlyTotals(monthlyTotalIncome, monthlyTotalExpense);

                // Hiển thị thông báo nếu không có dữ liệu
                if (calendarEntries.isEmpty()) {
                    Toast.makeText(requireContext(), "Tháng này không có giao dịch", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(),
                            "Đã tải " + calendarEntries.size() + " giao dịch",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Exception e) {
                progressBar.setVisibility(View.GONE);
                forceReload = false;
                Toast.makeText(requireContext(), "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                displayMonthlyTotals(0, 0);
            }
        });
    }

    // Thêm phương thức để load calendar entries từ cache
    private void loadCalendarEntriesFromCache(String cacheKey) {
        // Nếu bạn muốn cache cả calendar entries, cần thêm logic ở đây
        // Hiện tại chỉ reload từ Firebase
        // Bạn có thể tạo thêm một cache riêng cho calendarEntries nếu cần
    }

    private String getDayName(int dayOfWeek) {
        switch (dayOfWeek) {
            case Calendar.SUNDAY: return "CN";
            case Calendar.MONDAY: return "Thứ 2";
            case Calendar.TUESDAY: return "Thứ 3";
            case Calendar.WEDNESDAY: return "Thứ 4";
            case Calendar.THURSDAY: return "Thứ 5";
            case Calendar.FRIDAY: return "Thứ 6";
            case Calendar.SATURDAY: return "Thứ 7";
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

        // LUÔN HIỂN THỊ TỔNG THÁNG (dùng FormatUtils)
        tvTotalIncome.setText(FormatUtils.formatCurrencyWithSign(totalIncome));
        tvTotalExpense.setText(FormatUtils.formatCurrencyWithSign(-totalExpense)); // Dấu âm

        String balanceText = FormatUtils.formatCurrencyWithSign(totalBalance);
        tvTotalBalance.setText(balanceText);

        // Đặt màu theo giá trị
        if (totalBalance > 0) {
            tvTotalBalance.setTextColor(getResources().getColor(android.R.color.holo_green_dark, null));
        } else if (totalBalance < 0) {
            tvTotalBalance.setTextColor(getResources().getColor(android.R.color.holo_red_dark, null));
        } else {
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

        monthlyTotalIncome = totalMonthIncome;
        monthlyTotalExpense = totalMonthExpense;

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
    // Thêm method này vào CalendarFragment
    private Date validateTransactionDate(Date date) {
        if (date == null) {
            return new Date(); // Trả về ngày hiện tại nếu null
        }

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int year = cal.get(Calendar.YEAR);

        // Nếu năm < 1970 (không hợp lệ cho Firebase)
        if (year < 1970) {
            Log.w(TAG, "Invalid year in transaction date: " + year + ", using current date");
            return new Date(); // Trả về ngày hiện tại
        }

        return date;
    }

    // Thêm TAG cho logging
    private static final String TAG = "CalendarFragment";
    // Thêm phương thức để clear cache khi cần
    public void clearCache() {
        monthlyCache.clear();
        forceReload = true;
        updateCalendar();
    }
}