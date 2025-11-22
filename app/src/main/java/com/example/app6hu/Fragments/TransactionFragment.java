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

    public TransactionFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // ⭐ Inflate đúng
        View view = inflater.inflate(R.layout.fragment_transaction, container, false);

        // ⭐ Ánh xạ đúng
        btnChi = view.findViewById(R.id.over_btn);
        btnThu = view.findViewById(R.id.chart_btn);

        // ⭐ Mặc định mở Chi (hoặc đổi tùy bạn muốn)
        loadFragment(new ExpenseFragment());

        // ⭐ Xử lý nút Chi
        btnChi.setOnClickListener(v -> {
            updateTabUI(true);
            loadFragment(new ExpenseFragment());
        });

        // ⭐ Xử lý nút Thu
        btnThu.setOnClickListener(v -> {
            updateTabUI(false);
            loadFragment(new IncomeFragment());
        });

        return view;
    }

    // ⭐ Load fragment con
    private void loadFragment(Fragment fragment) {
        getChildFragmentManager()
                .beginTransaction()
                .setCustomAnimations(
                        R.anim.fade_in,  // fragment vào
                        R.anim.fade_out  // fragment ra
                )
                .replace(R.id.fragOverView, fragment)
                .commit();
    }
    private void updateTabUI(boolean isExpenseSelected) {
        if (isExpenseSelected) {
            btnChi.setBackgroundColor(getResources().getColor(R.color.orange));
            btnThu.setBackgroundColor(getResources().getColor(R.color.gray));
        } else {
            btnThu.setBackgroundColor(getResources().getColor(R.color.orange));
            btnChi.setBackgroundColor(getResources().getColor(R.color.gray));
        }
    }



}
