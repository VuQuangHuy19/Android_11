package com.example.app6hu.Fragments;

import android.os.Bundle;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.app6hu.R;
import com.example.app6hu.model.DanhMuc;

import java.util.ArrayList;
import java.util.List;

public class TransactionFragment extends Fragment {

    private Button btnChi, btnThu;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_transaction, container, false);

        btnChi = view.findViewById(R.id.over_btn);
        btnThu = view.findViewById(R.id.chart_btn);

        // ⭐ Mặc định mở Chi tiêu
        setSelectedTab(true);
        loadFragment(new ExpenseFragment());

        // ⭐ Chi tiêu
        btnChi.setOnClickListener(v -> {
            if (!btnChi.isEnabled()) return; // đang được chọn → không làm gì
            setSelectedTab(true);
            loadFragment(new ExpenseFragment());
        });

        // ⭐ Thu nhập
        btnThu.setOnClickListener(v -> {
            if (!btnThu.isEnabled()) return;
            setSelectedTab(false);
            loadFragment(new IncomeFragment());
        });

        return view;
    }


    // ⭐ Hàm load fragment con
    private void loadFragment(Fragment fragment) {
        getChildFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                .replace(R.id.fragOverView, fragment)
                .commit();
    }

    // ⭐ Cập nhật UI + disable nút
    private void setSelectedTab(boolean isExpense) {
        if (isExpense) {
            // Chi tiêu được chọn
            btnChi.setBackgroundColor(getResources().getColor(R.color.orange));
            btnChi.setEnabled(false);

            btnThu.setBackgroundColor(getResources().getColor(R.color.gray));
            btnThu.setEnabled(true);
        } else {
            // Thu nhập được chọn
            btnThu.setBackgroundColor(getResources().getColor(R.color.orange));
            btnThu.setEnabled(false);

            btnChi.setBackgroundColor(getResources().getColor(R.color.gray));
            btnChi.setEnabled(true);
        }
    }
}

