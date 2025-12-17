        package com.example.app6hu;

        import android.content.Intent;
        import android.net.Uri;
        import android.os.Bundle;
        import android.os.Handler;
        import android.os.Looper;
        import android.util.Log;
        import android.widget.Toast;

        import androidx.activity.EdgeToEdge;
        import androidx.appcompat.app.AlertDialog;
        import androidx.appcompat.app.AppCompatActivity;
        import androidx.core.graphics.Insets;
        import androidx.core.view.ViewCompat;
        import androidx.core.view.WindowInsetsCompat;

        import com.example.app6hu.Activities.Home;
        import com.example.app6hu.firebase.FirebasestoreManager;
        import com.example.app6hu.model.DanhMuc;
        import com.google.firebase.FirebaseApp;
        import com.google.firebase.firestore.DocumentSnapshot;
        import com.google.firebase.firestore.FirebaseFirestore;

        import java.util.List;

        public class MainActivity extends AppCompatActivity {
            private static final String TAG = "MainActivity";
            private FirebasestoreManager manager;
            private boolean isInitialized = false;
            private Handler handler = new Handler(Looper.getMainLooper());

            @Override
            protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                EdgeToEdge.enable(this);
                setContentView(R.layout.activity_main);

                ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
                    Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                    return insets;
                });
                testFirestoreData();
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("DanhMuc").get()
                        .addOnCompleteListener(task -> {
                            Log.d("TEST", "Firestore test result: " +
                                    task.isSuccessful() + ", docs: " +
                                    (task.isSuccessful() ? task.getResult().size() : 0));
                        });
                // Kiểm tra Firebase initialization
                try {
                    FirebaseApp.initializeApp(this);
                    Log.d(TAG, "Firebase initialized successfully");
                } catch (Exception e) {
                    Log.e(TAG, "Firebase initialization failed: " + e.getMessage());
                    showFirebaseErrorDialog();
                    return;
                }

                manager = new FirebasestoreManager();

                // Sử dụng delay để tránh race condition
                handler.postDelayed(() -> {
                    checkFirebaseConnection();
                }, 1000);
            }
            private void testFirestoreData() {
                FirebaseFirestore db = FirebaseFirestore.getInstance();

                Log.d("DEBUG", "=== KIỂM TRA FIRESTORE ===");

                // Test 1: Kiểm tra collection DanhMuc
                db.collection("DanhMuc")
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Log.d("DEBUG", " Collection DanhMuc tồn tại");
                                Log.d("DEBUG", "Số lượng documents: " + task.getResult().size());

                                for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                                    Log.d("DEBUG", "Document ID: " + doc.getId());
                                    Log.d("DEBUG", "Data: " + doc.getData());

                                    // Kiểm tra từng field cụ thể
                                    Log.d("DEBUG", "  itemName: " + doc.getString("itemName"));
                                    Log.d("DEBUG", "  type: " + doc.getString("type"));
                                    Log.d("DEBUG", "  icon: " + doc.getString("icon"));
                                    Log.d("DEBUG", "  resourcesID: " + doc.get("resourcesID"));
                                }
                            } else {
                                Log.e("DEBUG", " Lỗi truy cập DanhMuc: " + task.getException());
                            }
                        });

                // Test 2: Kiểm tra với filter type = "expense"
                db.collection("DanhMuc")
                        .whereEqualTo("type", "expense")
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Log.d("DEBUG", "✅ Query type='expense' thành công");
                                Log.d("DEBUG", "Số documents type='expense': " + task.getResult().size());

                                if (task.getResult().isEmpty()) {
                                    Log.w("DEBUG", "KHÔNG có documents nào với type='expense'");
                                    Log.w("DEBUG", "Kiểm tra lại giá trị field 'type' trong Firestore");
                                }
                            } else {
                                Log.e("DEBUG", " Lỗi query type='expense': " + task.getException());
                            }
                        });
            }
            private void checkFirebaseConnection() {
                manager.checkFirestoreConnection(new FirebasestoreManager.FirestoreCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean connected) {
                        if (connected) {
                            Log.d(TAG, "Firestore connection successful");
                            initializeCategories();
                        } else {
                            showFirebaseErrorDialog();
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.e(TAG, "Firestore connection failed: " + e.getMessage());
                        // Vẫn cho vào app với dữ liệu cục bộ
                        showOfflineModeDialog();
                    }
                });
            }

            private void showFirebaseErrorDialog() {
                new AlertDialog.Builder(this)
                        .setTitle("Lỗi kết nối Firebase")
                        .setMessage("Không thể kết nối đến Firebase. Vui lòng kiểm tra:\n\n" +
                                "1. Kết nối internet\n" +
                                "2. File google-services.json\n" +
                                "3. Cấu hình Firebase trong Console\n\n" +
                                "Bạn có thể sử dụng ứng dụng ở chế độ offline.")
                        .setPositiveButton("Vào ứng dụng", (dialog, which) -> {
                            navigateToHome();
                        })
                        .setNegativeButton("Thoát", (dialog, which) -> {
                            finish();
                        })
                        .setNeutralButton("Kiểm tra Firebase", (dialog, which) -> {
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                                    Uri.parse("https://console.firebase.google.com/"));
                            startActivity(browserIntent);
                        })
                        .show();
            }

            private void showOfflineModeDialog() {
                new AlertDialog.Builder(this)
                        .setTitle("Chế độ Offline")
                        .setMessage("Ứng dụng sẽ hoạt động ở chế độ offline. Bạn vẫn có thể thêm/sửa danh mục nhưng dữ liệu sẽ không được đồng bộ.")
                        .setPositiveButton("Đồng ý", (dialog, which) -> {
                            navigateToHome();
                        })
                        .show();
            }

            private void initializeCategories() {
                // Sử dụng context từ activity
                manager.getAllDanhMuc(this, new FirebasestoreManager.FirestoreCallback<List<DanhMuc>>() {
                    @Override
                    public void onSuccess(List<DanhMuc> danhMucList) {
                        Log.d(TAG, "Loaded " + danhMucList.size() + " categories");
                        if (danhMucList.isEmpty()) {
                            createDefaultCategoriesDirectly();
                        } else {
                            navigateToHome();
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.e(TAG, "Failed to load categories: " + e.getMessage());
                        // Vẫn vào home dù có lỗi
                        navigateToHome();
                    }
                });
            }

            private void createDefaultCategoriesDirectly() {
                // Gọi mà không cần context vì initDefaultDanhMuc không cần
                manager.initDefaultDanhMuc(new FirebasestoreManager.FirestoreCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean success) {
                        Log.d(TAG, "Default categories created: " + success);
                        navigateToHome();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.e(TAG, "Failed to create default categories: " + e.getMessage());
                        navigateToHome();
                    }
                });
            }

            private void navigateToHome() {
                if (!isInitialized) {
                    isInitialized = true;
                    startActivity(new Intent(this, Home.class));
                    finish();
                }
            }

            @Override
            protected void onDestroy() {
                super.onDestroy();
                handler.removeCallbacksAndMessages(null);
            }
        }