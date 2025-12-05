package com.example.app6hu.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        ImageView btnAdd = findViewById(R.id.btnAddDanhMuc);
        btnAdd.setOnClickListener(v -> {
            // Chuyển sang Activity tạo danh mục mới
            startActivity(new Intent(ThemDanhMucActivity.this, ThemMoiDanhMucActivity.class));
        });

        // Khởi tạo danh sách
        danhMucList = new ArrayList<>();

        adapter = new ThemDanhMucAdapter(this, danhMucList, (item, position) -> {
            // Nhấn vào danh mục → mở sửa danh mục
            Intent intent = new Intent(ThemDanhMucActivity.this, SuaDanhMucActivity.class);
            intent.putExtra("tenDanhMuc", item.getItemName());
            startActivity(intent);
        });

        rcvDanhMuc.setLayoutManager(new LinearLayoutManager(this));
        rcvDanhMuc.setAdapter(adapter);
        
        // Lấy danh mục từ Firebase
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
        firestoreManager.getCategories(new FirebasestoreManager.OnCategoriesLoadedListener() {
            @Override
            public void onCategoriesLoaded(List<DanhMuc> categories) {
                danhMucList.clear();
                danhMucList.addAll(categories);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(ThemDanhMucActivity.this, "Lỗi tải danh mục: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDeleteDialog(int position) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa danh mục")
                .setMessage("Bạn có muốn xóa danh mục này không?")
                .setPositiveButton("Ok", (dialog, which) -> {
                    DanhMuc category = danhMucList.get(position);
                    if (category.getId() != null) {
                        deleteCategoryFromFirebase(category.getId(), position);
                    } else {
                        adapter.removeItem(position);
                    }
                })
                .setNegativeButton("Bỏ qua", (dialog, which) -> adapter.notifyItemChanged(position))
                .show();
    }

    private void deleteCategoryFromFirebase(String categoryId, int position) {
        firestoreManager.deleteCategory(categoryId, new FirebasestoreManager.OnCategoryDeletedListener() {
            @Override
            public void onCategoryDeleted() {
                Toast.makeText(ThemDanhMucActivity.this, "Đã xóa danh mục", Toast.LENGTH_SHORT).show();
                adapter.removeItem(position);
            }

            @Override
            public void onError(String error) {
                Toast.makeText(ThemDanhMucActivity.this, "Lỗi xóa danh mục: " + error, Toast.LENGTH_SHORT).show();
                adapter.notifyItemChanged(position);
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
