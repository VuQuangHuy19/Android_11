package com.example.app6hu.Activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.example.app6hu.R;
import com.example.app6hu.firebase.FirebasestoreManager;
import com.example.app6hu.firebase.FirestoreHocPhiManager;
import com.example.app6hu.model.HocPhi;
import com.example.app6hu.utils.MoneyUtils;

import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;

public class AddHocPhiActivity extends AppCompatActivity {

    FirestoreHocPhiManager manager = new FirestoreHocPhiManager();
    EditText edtTenHocPhi, edtDonGiaTin, edtSoTinChi, edtNgayDong;
    EditText edtHeSo;
    CheckBox cbNoMon;
    Button btnLuu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_hoc_phi);

        edtHeSo = findViewById(R.id.edtHeSo);
        edtTenHocPhi = findViewById(R.id.edtTenHocPhi);
        edtDonGiaTin = findViewById(R.id.edtDonGiaTin);
        edtSoTinChi = findViewById(R.id.edtSoTinChi);
        edtNgayDong = findViewById(R.id.edtNgayDong);
        cbNoMon = findViewById(R.id.cbNoMon);
        cbNoMon.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                edtHeSo.setVisibility(View.VISIBLE);
                edtHeSo.setHint("1.5"); // gợi ý mặc định
            } else {
                edtHeSo.setVisibility(View.GONE);
                edtHeSo.setText("");
            }
        });
        edtNgayDong.setFocusable(false);
        edtNgayDong.setClickable(true);

        edtNgayDong.setOnClickListener(v -> showDatePicker());

        edtDonGiaTin.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) { // khi rời ô nhập
                String text = edtDonGiaTin.getText().toString();

                if (!text.isEmpty()) {
                    long value = Long.parseLong(text);
                    edtDonGiaTin.setText(MoneyUtils.format(value));
                }
            }
        });

        btnLuu = findViewById(R.id.btnLuuHocPhi);

        btnLuu.setOnClickListener(v -> saveHocPhi());
    }

    private void saveHocPhi() {
        String ten = edtTenHocPhi.getText().toString().trim();
        String donGiaStr = edtDonGiaTin.getText().toString().trim();
        String soTinStr = edtSoTinChi.getText().toString().trim();
        String ngay = edtNgayDong.getText().toString().trim();
        boolean noMon = cbNoMon.isChecked();

        if (ten.isEmpty() || donGiaStr.isEmpty() || soTinStr.isEmpty() || ngay.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ", Toast.LENGTH_SHORT).show();
            return;
        }

        long donGia = Long.parseLong(donGiaStr.replace(".", "").replace(" đ", ""));
        int soTin = Integer.parseInt(soTinStr);
        double heSo;

        if (cbNoMon.isChecked()) {
            String heSoStr = edtHeSo.getText().toString().trim();

            if (heSoStr.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập hệ số nợ môn!", Toast.LENGTH_SHORT).show();
                return;
            }

            heSo = Double.parseDouble(heSoStr);
        } else {
            heSo = 1.0;
        }

        long soTien = (long) (donGia * soTin * heSo);


        String id = UUID.randomUUID().toString();

        HocPhi hocPhi = new HocPhi(
                id,
                ten,
                donGia,
                soTin,
                noMon,
                soTien,
                ngay,
                "CHUA_DONG"
        );

        manager.collection("hoc_phi")
                .document(id)  // id tự tạo của bạn
                .set(hocPhi)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "✅ Đã thêm học phí: " + soTien + "", Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "❌ Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, y, m, d) -> {
                    // format dd/MM/yyyy
                    String date = String.format(Locale.getDefault(),
                            "%02d/%02d/%04d", d, m + 1, y);
                    edtNgayDong.setText(date);
                },
                year, month, day
        );

        datePickerDialog.show();
    }

}
