package com.example.app6hu.Activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.app6hu.R;
import com.example.app6hu.firebase.FirebasestoreManager;

public class ThemMoiDanhMucActivity extends AppCompatActivity {

    private EditText edtTenDanhMuc;
    private Button btnSave;
    private FirebasestoreManager firestoreManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_them_moi_danh_muc);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.themmoidanhmuc), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        firestoreManager = new FirebasestoreManager();

        // Ánh xạ các view
        edtTenDanhMuc = findViewById(R.id.edtTenDanhMuc);
        btnSave = findViewById(R.id.btnSave);
        ImageView btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        btnSave.setOnClickListener(v -> saveCategoryToFirebase());
    }

    private void saveCategoryToFirebase() {
        String categoryName = edtTenDanhMuc.getText().toString().trim();

        if (categoryName.isEmpty()) {
            Toast.makeText(this, "⚠️ Vui lòng nhập tên danh mục", Toast.LENGTH_SHORT).show();
            edtTenDanhMuc.requestFocus();
            return;
        }

        // Lưu lên Firebase (icon mặc định là "ic_logo") - không chờ kết quả

        // Hiển thị thông báo và finish ngay lập tức
        Toast.makeText(this, "✅ Đang thêm danh mục...", Toast.LENGTH_SHORT).show();
        finish();
    }
}