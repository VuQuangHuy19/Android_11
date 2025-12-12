package com.example.app6hu.Activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
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
import com.example.app6hu.model.DanhMuc;

import java.util.ArrayList;
import java.util.List;

public class SuaDanhMucActivity extends AppCompatActivity {

    private EditText edtTenDanhMuc;
    private RecyclerView rcvIcons, rcvColors;
    private Button btnUpdate;
    private ImageView btnBack, btnDelete;

    private FirebasestoreManager firestoreManager;
    private IconAdapter iconAdapter;
    private ColorAdapter colorAdapter;

    private String danhMucId;
    private String currentIconName = "ic_logo";
    private String currentColor = "#FF0000";
    private  int currentColorInt;

    private List<String> iconList = new ArrayList<>();
    private List<String> colorList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sua_danh_muc);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.suadanhmucactivity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Ánh xạ view
        edtTenDanhMuc = findViewById(R.id.edtTenDanhMuc);
        rcvIcons = findViewById(R.id.rcvIcons);
        rcvColors = findViewById(R.id.rcvColors);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnBack = findViewById(R.id.btnBack);
        btnDelete = findViewById(R.id.btnDelete);

        firestoreManager = new FirebasestoreManager();

        // Nhận dữ liệu từ Intent
        Intent intent = getIntent();
        if (intent != null) {
            danhMucId = intent.getStringExtra("danhMucId");
            String danhMucName = intent.getStringExtra("danhMucName");
            String iconName = intent.getStringExtra("iconName");
            String color = intent.getStringExtra("color");

            // Hiển thị dữ liệu hiện tại
            edtTenDanhMuc.setText(danhMucName);
            if (iconName != null && !iconName.isEmpty()) {
                currentIconName = iconName;
            }
            if (color != null && !color.isEmpty()) {
                currentColor = color;
                try {
                    currentColorInt = Color.parseColor(color);
                } catch (Exception e) {
                    currentColorInt = Color.RED;
                }
            }
        }

        // Khởi tạo danh sách icons từ Firebase
        loadIconsFromFirebase();

        // Khởi tạo danh sách màu sắc
        initColorList();
        setupColorRecyclerView();

        // Setup listeners
        setupListeners();
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
                Toast.makeText(SuaDanhMucActivity.this,
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
        // Danh sách 20 màu cơ bản
        colorList.add("#FF0000"); // Red
        colorList.add("#00FF00"); // Green
        colorList.add("#0000FF"); // Blue
        colorList.add("#FFFF00"); // Yellow
        colorList.add("#FF00FF"); // Magenta
        colorList.add("#00FFFF"); // Cyan
        colorList.add("#FFA500"); // Orange
        colorList.add("#800080"); // Purple
        colorList.add("#008000"); // Dark Green
        colorList.add("#000080"); // Navy
        colorList.add("#808080"); // Gray
        colorList.add("#FFC0CB"); // Pink
        colorList.add("#A52A2A"); // Brown
        colorList.add("#FFD700"); // Gold
        colorList.add("#40E0D0"); // Turquoise
        colorList.add("#EE82EE"); // Violet
        colorList.add("#F5F5DC"); // Beige
        colorList.add("#FF6347"); // Tomato
        colorList.add("#7CFC00"); // Lawn Green
        colorList.add("#9370DB"); // Medium Purple
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

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnDelete.setOnClickListener(v -> showDeleteConfirmation());

        btnUpdate.setOnClickListener(v -> updateCategory());
    }

    private void showDeleteConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Xóa danh mục")
                .setMessage("Bạn có chắc chắn muốn xóa danh mục này không?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteCategory())
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteCategory() {
        if (danhMucId != null && !danhMucId.isEmpty()) {
            firestoreManager.deleteCategory(danhMucId, new FirebasestoreManager.OnCategoryDeletedListener() {
                @Override
                public void onCategoryDeleted() {
                    runOnUiThread(() -> {
                        Toast.makeText(SuaDanhMucActivity.this,
                                "Đã xóa danh mục thành công",
                                Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    });
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        Toast.makeText(SuaDanhMucActivity.this,
                                "Lỗi xóa danh mục: " + error,
                                Toast.LENGTH_SHORT).show();
                    });
                }
            });
        }
    }

    private void updateCategory() {
        String categoryName = edtTenDanhMuc.getText().toString().trim();

        if (categoryName.isEmpty()) {
            Toast.makeText(this, "⚠️ Vui lòng nhập tên danh mục", Toast.LENGTH_SHORT).show();
            edtTenDanhMuc.requestFocus();
            return;
        }

        // Lấy type từ Intent
        String type = getIntent().getStringExtra("danhMucType");

        // Cập nhật danh mục
        firestoreManager.updateCategory(danhMucId, categoryName, currentIconName, currentColor, type,
                new FirebasestoreManager.OnCategoryUpdatedListener() {
                    @Override
                    public void onCategoryUpdated() {
                        runOnUiThread(() -> {
                            Toast.makeText(SuaDanhMucActivity.this,
                                    "✅ Cập nhật danh mục thành công",
                                    Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            finish();
                        });
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> {
                            Toast.makeText(SuaDanhMucActivity.this,
                                    "❌ Lỗi cập nhật: " + error,
                                    Toast.LENGTH_SHORT).show();
                        });
                    }
                });
    }
}