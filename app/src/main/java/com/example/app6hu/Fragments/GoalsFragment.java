package com.example.app6hu.Fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app6hu.Adapter.GoalAdapter;
import com.example.app6hu.R;
import com.example.app6hu.firebase.FirebasestoreManager;
import com.example.app6hu.model.Goal;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GoalsFragment extends Fragment {

    private TextView tvSummaryAmount, tvSummaryLabel;
    private ProgressBar progressTotal;
    private ChipGroup chipGroupFilter;
    private Chip chipAll, chipActive, chipCompleted;
    private RecyclerView rvGoals;
    private LinearLayout layoutEmpty;
    private FloatingActionButton fabAddGoal;

    private GoalAdapter adapter;
    private FirebasestoreManager firestoreManager;
    private List<Goal> allGoals = new ArrayList<>();
    private String currentFilter = "all"; // all, active, completed

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firestoreManager = new FirebasestoreManager();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_saving, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupRecyclerView();
        setupListeners();
        loadGoals();
    }

    private void initViews(View view) {
        tvSummaryLabel = view.findViewById(R.id.tvSummaryLabel);
        tvSummaryAmount = view.findViewById(R.id.tvSummaryAmount);
        progressTotal = view.findViewById(R.id.progressTotal);
        chipGroupFilter = view.findViewById(R.id.chipGroupFilter);
        chipAll = view.findViewById(R.id.chipAll);
        chipActive = view.findViewById(R.id.chipActive);
        chipCompleted = view.findViewById(R.id.chipCompleted);
        rvGoals = view.findViewById(R.id.rvGoals);
        layoutEmpty = view.findViewById(R.id.layoutEmpty);
        fabAddGoal = view.findViewById(R.id.fabAddGoal);
    }

    private void setupRecyclerView() {
        rvGoals.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new GoalAdapter(new ArrayList<>(), new GoalAdapter.OnGoalClickListener() {
            @Override
            public void onGoalClick(Goal goal) {
                showEditGoalDialog(goal);
            }

            @Override
            public void onGoalLongClick(Goal goal) {
                showDeleteConfirmDialog(goal);
            }
        });
        rvGoals.setAdapter(adapter);
    }

    private void setupListeners() {
        fabAddGoal.setOnClickListener(v -> showAddGoalDialog());

        chipAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                currentFilter = "all";
                filterGoals();
            }
        });

        chipActive.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                currentFilter = "active";
                filterGoals();
            }
        });

        chipCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                currentFilter = "completed";
                filterGoals();
            }
        });
    }

    private void loadGoals() {
        firestoreManager.getAllGoals(new FirebasestoreManager.FirestoreCallback<List<Goal>>() {
            @Override
            public void onSuccess(List<Goal> goals) {
                allGoals = goals;
                updateSummary();
                filterGoals();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getContext(), "Lỗi khi tải mục tiêu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterGoals() {
        List<Goal> filtered = new ArrayList<>();
        for (Goal goal : allGoals) {
            String status = goal.getStatus() != null ? goal.getStatus() : "active";
            if (currentFilter.equals("all")) {
                filtered.add(goal);
            } else if (currentFilter.equals("active") && "active".equals(status)) {
                filtered.add(goal);
            } else if (currentFilter.equals("completed") && "completed".equals(status)) {
                filtered.add(goal);
            }
        }
        adapter.updateGoals(filtered);
        layoutEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void updateSummary() {
        long totalSaved = 0;
        long totalTarget = 0;
        for (Goal goal : allGoals) {
            if (!"completed".equals(goal.getStatus())) {
                totalSaved += goal.getSavedAmount();
                totalTarget += goal.getTargetAmount();
            }
        }

        java.text.DecimalFormat df = new java.text.DecimalFormat("#,###");
        tvSummaryAmount.setText("Đã tiết kiệm: " + df.format(totalSaved) + "đ / " + df.format(totalTarget) + "đ");

        int progress = 0;
        if (totalTarget > 0) {
            progress = (int) ((totalSaved * 100) / totalTarget);
            if (progress > 100) progress = 100;
        }
        progressTotal.setProgress(progress);
    }

    private void showAddGoalDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_goal, null);
        TextInputEditText etName = dialogView.findViewById(R.id.etGoalName);
        TextInputEditText etTarget = dialogView.findViewById(R.id.etTargetAmount);
        TextInputEditText etDeadline = dialogView.findViewById(R.id.etDeadline);
        TextInputEditText etNote = dialogView.findViewById(R.id.etNote);

        new AlertDialog.Builder(getContext())
                .setTitle("Thêm mục tiêu tiết kiệm")
                .setView(dialogView)
                .setPositiveButton("Thêm", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String targetStr = etTarget.getText().toString().trim();
                    String deadline = etDeadline.getText().toString().trim();
                    String note = etNote.getText().toString().trim();

                    if (TextUtils.isEmpty(name)) {
                        Toast.makeText(getContext(), "Vui lòng nhập tên mục tiêu", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (TextUtils.isEmpty(targetStr)) {
                        Toast.makeText(getContext(), "Vui lòng nhập số tiền mục tiêu", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        long target = Long.parseLong(targetStr.replaceAll("[^0-9]", ""));
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                        String createdAt = sdf.format(new Date());

                        Goal goal = new Goal(name, target, 0, deadline, note, "active", createdAt, "#018786");
                        firestoreManager.addGoal(goal, new FirebasestoreManager.FirestoreCallback<String>() {
                            @Override
                            public void onSuccess(String goalId) {
                                Toast.makeText(getContext(), "Thêm mục tiêu thành công", Toast.LENGTH_SHORT).show();
                                loadGoals();
                            }

                            @Override
                            public void onFailure(Exception e) {
                                Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    } catch (NumberFormatException e) {
                        Toast.makeText(getContext(), "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showEditGoalDialog(Goal goal) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_goal, null);
        TextInputEditText etName = dialogView.findViewById(R.id.etGoalName);
        TextInputEditText etTarget = dialogView.findViewById(R.id.etTargetAmount);
        TextInputEditText etSaved = dialogView.findViewById(R.id.etSavedAmount);
        TextInputEditText etDeadline = dialogView.findViewById(R.id.etDeadline);
        TextInputEditText etNote = dialogView.findViewById(R.id.etNote);

        etName.setText(goal.getName());
        etTarget.setText(String.valueOf(goal.getTargetAmount()));
        etSaved.setText(String.valueOf(goal.getSavedAmount()));
        etDeadline.setText(goal.getDeadline());
        etNote.setText(goal.getNote());

        new AlertDialog.Builder(getContext())
                .setTitle("Chỉnh sửa mục tiêu")
                .setView(dialogView)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String targetStr = etTarget.getText().toString().trim();
                    String savedStr = etSaved.getText().toString().trim();
                    String deadline = etDeadline.getText().toString().trim();
                    String note = etNote.getText().toString().trim();

                    if (TextUtils.isEmpty(name) || TextUtils.isEmpty(targetStr) || TextUtils.isEmpty(savedStr)) {
                        Toast.makeText(getContext(), "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        long target = Long.parseLong(targetStr.replaceAll("[^0-9]", ""));
                        long saved = Long.parseLong(savedStr.replaceAll("[^0-9]", ""));

                        goal.setName(name);
                        goal.setTargetAmount(target);
                        goal.setSavedAmount(saved);
                        goal.setDeadline(deadline);
                        goal.setNote(note);

                        if (saved >= target) {
                            goal.setStatus("completed");
                        } else {
                            goal.setStatus("active");
                        }

                        // Cập nhật mục tiêu
                        if (goal.getId() != null && !goal.getId().isEmpty()) {
                            firestoreManager.updateGoal(goal.getId(), goal, new FirebasestoreManager.FirestoreCallback<Void>() {
                                @Override
                                public void onSuccess(Void result) {
                                    Toast.makeText(getContext(), "Cập nhật mục tiêu thành công", Toast.LENGTH_SHORT).show();
                                    loadGoals();
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Toast.makeText(getContext(), "Không tìm thấy ID mục tiêu", Toast.LENGTH_SHORT).show();
                        }
                    } catch (NumberFormatException e) {
                        Toast.makeText(getContext(), "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showDeleteConfirmDialog(Goal goal) {
        new AlertDialog.Builder(getContext())
                .setTitle("Xóa mục tiêu")
                .setMessage("Bạn có chắc muốn xóa mục tiêu \"" + goal.getName() + "\"?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    if (goal.getId() != null && !goal.getId().isEmpty()) {
                        firestoreManager.deleteGoal(goal.getId(), new FirebasestoreManager.FirestoreCallback<Void>() {
                            @Override
                            public void onSuccess(Void result) {
                                Toast.makeText(getContext(), "Xóa mục tiêu thành công", Toast.LENGTH_SHORT).show();
                                loadGoals();
                            }

                            @Override
                            public void onFailure(Exception e) {
                                Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(getContext(), "Không tìm thấy ID mục tiêu", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadGoals();
    }
}
