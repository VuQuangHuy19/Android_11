package com.example.app6hu.Activities;

import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.example.app6hu.R;
import com.example.app6hu.firebase.RealtimeDatabaseManager;
import com.example.app6hu.model.HocPhi;

import java.util.UUID;

public class AddHocPhiActivity extends AppCompatActivity {

    EditText edtTenHocPhi, edtDonGiaTin, edtSoTinChi, edtNgayDong;
    CheckBox cbNoMon;
    Button btnLuu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_hoc_phi);

        edtTenHocPhi = findViewById(R.id.edtTenHocPhi);
        edtDonGiaTin = findViewById(R.id.edtDonGiaTin);
        edtSoTinChi = findViewById(R.id.edtSoTinChi);
        edtNgayDong = findViewById(R.id.edtNgayDong);
        cbNoMon = findViewById(R.id.cbNoMon);
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

        long donGia = Long.parseLong(donGiaStr);
        int soTin = Integer.parseInt(soTinStr);

        // ✅ HỆ SỐ NỢ MÔN
        double heSo = noMon ? 1.5 : 1.0;

        // ✅ TÍNH TIỀN
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

        RealtimeDatabaseManager.db()
                .child("hoc_phi")
                .child(id)
                .setValue(hocPhi)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "✅ Đã thêm học phí: " + soTien + " đ", Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "❌ Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}
