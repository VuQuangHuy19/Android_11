package com.example.app6hu.Activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.app6hu.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EditHocPhiActivity extends AppCompatActivity {

    private EditText edtTenHocPhi, edtDonGiaTin, edtSoTinChi, edtHeSo, edtNgayDong;
    private CheckBox cbNoMon;
    private Button btnCapNhat;

    private FirebaseFirestore db;
    private String docId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_hoc_phi);

        edtTenHocPhi = findViewById(R.id.edtTenHocPhi);
        edtDonGiaTin = findViewById(R.id.edtDonGiaTin);
        edtSoTinChi = findViewById(R.id.edtSoTinChi);
        edtHeSo = findViewById(R.id.edtHeSo);
        edtNgayDong = findViewById(R.id.edtNgayDong);
        cbNoMon = findViewById(R.id.cbNoMon);
        btnCapNhat = findViewById(R.id.btnCapNhat);

        db = FirebaseFirestore.getInstance();
        docId = getIntent().getStringExtra("DOC_ID");

        cbNoMon.setOnCheckedChangeListener((buttonView, isChecked) -> {
            edtHeSo.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        if (docId != null) {
            loadHocPhi();
        }
        edtNgayDong.setFocusable(false);
        edtNgayDong.setClickable(true);

        edtNgayDong.setOnClickListener(v -> showDatePicker());
        btnCapNhat.setOnClickListener(v -> updateHocPhi());
    }
    private void loadHocPhi() {
        db.collection("hoc_phi")
                .document(docId)
                .get()
                .addOnSuccessListener(doc -> {

                    if (!doc.exists()) {
                        Toast.makeText(this,
                                "Không tìm thấy học phí",
                                Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    edtTenHocPhi.setText(doc.getString("tenHocPhi"));
                    edtDonGiaTin.setText(String.valueOf(doc.getLong("donGiaTin")));
                    edtSoTinChi.setText(String.valueOf(doc.getLong("soTinChi")));
                    edtNgayDong.setText(doc.getString("ngayDong"));

                    Boolean noMon = doc.getBoolean("noMon");
                    cbNoMon.setChecked(noMon != null && noMon);

                    Double heSo = doc.getDouble("heSo");
                    if (noMon != null && noMon && heSo != null) {
                        edtHeSo.setText(String.valueOf(heSo));
                        edtHeSo.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Lỗi load dữ liệu",
                                Toast.LENGTH_SHORT).show()
                );
    }

    private void updateHocPhi() {

        String tenHocPhi = edtTenHocPhi.getText().toString().trim();
        double donGiaTin = Double.parseDouble(edtDonGiaTin.getText().toString());
        if (edtDonGiaTin.getText().toString().isEmpty()) return;
        int soTinChi = Integer.parseInt(edtSoTinChi.getText().toString());
        String ngayDong = edtNgayDong.getText().toString().trim();

        boolean noMon = cbNoMon.isChecked();
        double heSo = 1.0;

        if (noMon) {
            heSo = Double.parseDouble(edtHeSo.getText().toString());
        }

        Map<String, Object> update = new HashMap<>();
        update.put("tenHocPhi", tenHocPhi);
        update.put("donGiaTin", donGiaTin);
        update.put("soTinChi", soTinChi);
        update.put("ngayDong", ngayDong);
        update.put("noMon", noMon);
        update.put("heSo", heSo);

        db.collection("hoc_phi")
                .document(docId)
                .update(update)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this,
                            "✅ Cập nhật thành công",
                            Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "❌ Cập nhật thất bại",
                                Toast.LENGTH_SHORT).show()
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
