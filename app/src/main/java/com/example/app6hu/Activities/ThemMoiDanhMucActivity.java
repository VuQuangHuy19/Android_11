package com.example.app6hu.Activities;

import android.content.Intent;
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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app6hu.Adapter.ColorAdapter;
import com.example.app6hu.Adapter.IconAdapter;
import com.example.app6hu.R;
import com.example.app6hu.firebase.FirebasestoreManager;

import java.util.ArrayList;
import java.util.List;

public class ThemMoiDanhMucActivity extends AppCompatActivity {

    private EditText edtTenDanhMuc;
    private RecyclerView rcvIcons, rcvColors;
    private Button btnSave;
    private ImageView btnBack;

    private FirebasestoreManager firestoreManager;
    private IconAdapter iconAdapter;
    private ColorAdapter colorAdapter;

    private String currentIconName = "ic_logo";
    private String currentColor = "#FF0000";
    private String fragmentType; // "thu" hoặc "chi"

    private List<String> iconList = new ArrayList<>();
    private List<String> colorList = new ArrayList<>();

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

        // Lấy type từ Intent
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("type")) {
            fragmentType = intent.getStringExtra("type");
        }

        // Ánh xạ các view
        edtTenDanhMuc = findViewById(R.id.edtTenDanhMuc);
        rcvIcons = findViewById(R.id.rcvIcons);
        rcvColors = findViewById(R.id.rcvColors);
        btnSave = findViewById(R.id.btnSave);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        // Khởi tạo danh sách icons từ Firebase
        loadIconsFromFirebase();

        // Khởi tạo danh sách màu sắc
        initColorList();
        setupColorRecyclerView();

        btnSave.setOnClickListener(v -> saveCategoryToFirebase());
    }

    private void loadIconsFromFirebase() {
        firestoreManager.getAllIcons(this, new FirebasestoreManager.FirestoreCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> icons) {
                if (icons != null && !icons.isEmpty()) {
                    iconList.clear();
                    iconList.addAll(icons);

                    // Setup RecyclerView cho icons
                    setupIconRecyclerView();
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(ThemMoiDanhMucActivity.this,
                        "Lỗi tải biểu tượng: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupIconRecyclerView() {
        GridLayoutManager layoutManager = new GridLayoutManager(this, 6);
        rcvIcons.setLayoutManager(layoutManager);

        iconAdapter = new IconAdapter(this, iconList, currentIconName,
                new IconAdapter.OnIconClickListener() {
                    @Override
                    public void onIconClick(String iconName) {
                        currentIconName = iconName;
                    }
                });
        rcvIcons.setAdapter(iconAdapter);
    }

    private void initColorList() {
        // Danh sách 20 màu cơ bản (giống SuaDanhMucActivity)
        colorList.add("#FF0000");
        colorList.add("#00FF00");
        colorList.add("#0000FF");
        colorList.add("#FFFF00");
        colorList.add("#FF00FF");
        colorList.add("#00FFFF");
        colorList.add("#FFA500");
        colorList.add("#800080");
        colorList.add("#008000");
        colorList.add("#000080");
        colorList.add("#808080");
        colorList.add("#FFC0CB");
        colorList.add("#A52A2A");
        colorList.add("#FFD700");
        colorList.add("#40E0D0");
        colorList.add("#EE82EE");
        colorList.add("#F5F5DC");
        colorList.add("#FF6347");
        colorList.add("#7CFC00");
        colorList.add("#9370DB");
    }

    private void setupColorRecyclerView() {
        GridLayoutManager layoutManager = new GridLayoutManager(this, 10);
        rcvColors.setLayoutManager(layoutManager);

        colorAdapter = new ColorAdapter(this, colorList, currentColor,
                new ColorAdapter.OnColorClickListener() {
                    @Override
                    public void onColorClick(String color) {
                        currentColor = color;
                    }
                });
        rcvColors.setAdapter(colorAdapter);
    }

    private void saveCategoryToFirebase() {
        String categoryName = edtTenDanhMuc.getText().toString().trim();

        if (categoryName.isEmpty()) {
            Toast.makeText(this, "⚠️ Vui lòng nhập tên danh mục", Toast.LENGTH_SHORT).show();
            edtTenDanhMuc.requestFocus();
            return;
        }

        if (fragmentType == null) {
            Toast.makeText(this, "❌ Lỗi: Không xác định được loại danh mục", Toast.LENGTH_SHORT).show();
            return;
        }

        // Thêm danh mục mới - SỬA LẠI GỌI ĐÚNG PHƯƠNG THỨC
        firestoreManager.addCategory(categoryName, currentIconName, currentColor, fragmentType,
                new FirebasestoreManager.OnCategoryAddedListener() {
                    @Override
                    public void onCategoryAdded() {
                        runOnUiThread(() -> {
                            Toast.makeText(ThemMoiDanhMucActivity.this,
                                    "✅ Thêm danh mục thành công",
                                    Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            finish();
                        });
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> {
                            Toast.makeText(ThemMoiDanhMucActivity.this,
                                    "❌ Lỗi thêm danh mục: " + error,
                                    Toast.LENGTH_SHORT).show();
                        });
                    }
                });
    }
}