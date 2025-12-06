package com.example.app6hu.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app6hu.Adapter.ThemDanhMucAdapter;
import com.example.app6hu.R;
import com.example.app6hu.firebase.FirebasestoreManager;
import com.example.app6hu.model.DanhMuc;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class ThemDanhMucActivity extends AppCompatActivity {

    private RecyclerView rcvDanhMuc;
    private ThemDanhMucAdapter adapter;
    private List<DanhMuc> danhMucList;
    private FirebasestoreManager firestoreManager;
    private String fragmentType; // Biến lưu từ fragment nào gọi

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_them_danh_muc);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.themdanhmucactivity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        rcvDanhMuc = findViewById(R.id.rcvDanhMuc);
        firestoreManager = new FirebasestoreManager();

        // Lấy type từ Intent
        fragmentType = getIntent().getStringExtra("fromFragment");

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        ImageView btnAdd = findViewById(R.id.btnAddDanhMuc);
        btnAdd.setOnClickListener(v -> {
            // Chuyển sang Activity tạo danh mục mới với type tương ứng
            Intent intent = new Intent(ThemDanhMucActivity.this, ThemMoiDanhMucActivity.class);
            if (fragmentType != null) {
                intent.putExtra("type", fragmentType);
            }
            startActivity(intent);
        });

        // Khởi tạo danh sách
        danhMucList = new ArrayList<>();

        // Sửa lại Adapter để hỗ trợ click
        adapter = new ThemDanhMucAdapter(this, danhMucList, new ThemDanhMucAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DanhMuc item, int position) {
                // Nhấn vào danh mục → mở sửa danh mục
                Intent intent = new Intent(ThemDanhMucActivity.this, SuaDanhMucActivity.class);
                intent.putExtra("danhMucId", item.getId());
                intent.putExtra("danhMucName", item.getItemName());
                intent.putExtra("danhMucType", item.getType());
                intent.putExtra("iconName", item.getIconName());
                intent.putExtra("color", item.getColor());
                startActivity(intent);
            }
        });

        rcvDanhMuc.setLayoutManager(new LinearLayoutManager(this));
        rcvDanhMuc.setAdapter(adapter);

        // Lấy danh mục từ Firebase - TRUYỀN CONTEXT
        loadCategoriesFromFirebase();

        // Vuốt để xóa
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(RecyclerView recyclerView,
                                  RecyclerView.ViewHolder viewHolder,
                                  RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int pos = viewHolder.getAdapterPosition();
                showDeleteDialog(pos);
            }
        });

        itemTouchHelper.attachToRecyclerView(rcvDanhMuc);
    }

    private void loadCategoriesFromFirebase() {
        // Sử dụng FirestoreCallback thay vì OnCategoriesLoadedListener
        firestoreManager.getAllDanhMuc(this, new FirebasestoreManager.FirestoreCallback<List<DanhMuc>>() {
            @Override
            public void onSuccess(List<DanhMuc> categories) {
                danhMucList.clear();

                if (categories != null && !categories.isEmpty()) {
                    // Filter theo type nếu cần
                    if (fragmentType != null) {
                        for (DanhMuc category : categories) {
                            if (fragmentType.equals(category.getType())) {
                                danhMucList.add(category);
                            }
                        }
                    } else {
                        // Hiển thị tất cả
                        danhMucList.addAll(categories);
                    }

                    Log.d("ThemDanhMucActivity", "Đã tải " + danhMucList.size() + " danh mục");
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(ThemDanhMucActivity.this,
                        "Lỗi tải danh mục: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
                Log.e("ThemDanhMucActivity", "Lỗi: " + e.getMessage());
            }
        });
    }

    private void showDeleteDialog(int position) {
        if (position < 0 || position >= danhMucList.size()) {
            return;
        }

        DanhMuc category = danhMucList.get(position);

        new AlertDialog.Builder(this)
                .setTitle("Xóa danh mục")
                .setMessage("Bạn có muốn xóa danh mục '" + category.getItemName() + "' không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    if (category.getId() != null && !category.getId().isEmpty()) {
                        deleteCategoryFromFirebase(category.getId(), position);
                    } else {
                        Toast.makeText(this, "Không thể xóa, ID danh mục không hợp lệ", Toast.LENGTH_SHORT).show();
                        adapter.notifyItemChanged(position);
                    }
                })
                .setNegativeButton("Hủy", (dialog, which) -> {
                    // Khôi phục item đã swipe
                    adapter.notifyItemChanged(position);
                })
                .show();
    }

    private void deleteCategoryFromFirebase(String categoryId, int position) {
        firestoreManager.deleteCategory(categoryId, new FirebasestoreManager.OnCategoryDeletedListener() {
            @Override
            public void onCategoryDeleted() {
                runOnUiThread(() -> {
                    Toast.makeText(ThemDanhMucActivity.this,
                            "Đã xóa danh mục thành công",
                            Toast.LENGTH_SHORT).show();

                    // Xóa khỏi danh sách local
                    if (position >= 0 && position < danhMucList.size()) {
                        danhMucList.remove(position);
                        adapter.notifyItemRemoved(position);

                        // Cập nhật lại các item phía sau
                        if (position < danhMucList.size()) {
                            adapter.notifyItemRangeChanged(position, danhMucList.size() - position);
                        }
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(ThemDanhMucActivity.this,
                            "Lỗi xóa danh mục: " + error,
                            Toast.LENGTH_SHORT).show();

                    // Khôi phục item trong list
                    if (position >= 0 && position < danhMucList.size()) {
                        adapter.notifyItemChanged(position);
                    }
                });
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Tải lại danh mục khi quay lại activity
        loadCategoriesFromFirebase();
    }
}