package com.example.app6hu.Fragments;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.example.app6hu.MainActivity;
import com.example.app6hu.R;
import com.example.app6hu.model.Transaction;
import com.example.app6hu.utils.Export;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SettingsFragment extends Fragment {

    private Spinner themeSpinner, languageSpinner;
    private Button btnApply;
    private String selectedTheme, selectedLanguage;

    public SettingsFragment() {
        // Required empty constructor
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Button btnExport = view.findViewById(R.id.btn_export_report);
        themeSpinner = view.findViewById(R.id.theme_spinner);
        languageSpinner = view.findViewById(R.id.language_spinner);
        btnApply = view.findViewById(R.id.btn_apply);

        // ------------------------
        // Spinner: Chủ đề (Sáng / Tối)
        // ------------------------
        ArrayAdapter<CharSequence> themeAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.theme_options,
                android.R.layout.simple_spinner_item
        );
        themeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        themeSpinner.setAdapter(themeAdapter);

        themeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                selectedTheme = parent.getItemAtPosition(position).toString();
            }

            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        // ------------------------
        // Spinner: Ngôn ngữ (VN / EN)
        // ------------------------
        ArrayAdapter<CharSequence> langAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.language_options,
                android.R.layout.simple_spinner_item
        );
        langAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        languageSpinner.setAdapter(langAdapter);

        languageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                selectedLanguage = parent.getItemAtPosition(position).toString();
            }

            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Apply settings button
        btnApply.setOnClickListener(v -> applySettings());

        btnExport.setOnClickListener(v -> {
            loadTransactions(data -> {
                if (data == null || data.isEmpty()) {
                    Toast.makeText(getContext(), "Không có dữ liệu để xuất!", Toast.LENGTH_SHORT).show();
                    return;
                }

                File pdfFile = Export.exportReportPDF(requireContext(), data);

                if (pdfFile != null) {
                    Toast.makeText(getContext(), "Xuất thành công: " + pdfFile.getPath(), Toast.LENGTH_LONG).show();
                }
            });
        });

    }

    private void applySettings() {

        // Áp dụng chế độ sáng / tối
        if ("Chế độ sáng".equals(selectedTheme)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else if ("Chế độ tối".equals(selectedTheme)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }

        // Áp dụng ngôn ngữ
        if ("English".equals(selectedLanguage)) {
            setLocale("en");
        } else {
            setLocale("vi");
        }

        // Reload lại app
        requireActivity().recreate();

        Intent intent = new Intent(requireActivity(), MainActivity.class);
        startActivity(intent);
    }

    private void setLocale(String langCode) {
        Locale locale = new Locale(langCode);
        Locale.setDefault(locale);

        Configuration config = new Configuration();
        config.setLocale(locale);

        requireActivity().getResources().updateConfiguration(
                config,
                requireActivity().getResources().getDisplayMetrics()
        );
    }

    private void loadTransactions(FirebaseCallback callback) {

        // Vì bạn không dùng đăng nhập FirebaseAuth, bạn tự đặt userId cố định
        String userId = "default_user";

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("transactions")
                .child(userId);

        ref.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<Transaction> list = new ArrayList<>();

                for (DataSnapshot snap : task.getResult().getChildren()) {
                    Transaction t = snap.getValue(Transaction.class);
                    if (t != null) list.add(t);
                }

                callback.onResult(list);
            } else {
                callback.onResult(null);
            }
        });
    }


    interface FirebaseCallback {
        void onResult(List<Transaction> list);
    }



}
