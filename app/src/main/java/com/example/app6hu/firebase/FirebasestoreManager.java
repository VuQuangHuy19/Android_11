    package com.example.app6hu.firebase;

    import android.content.Context;
    import android.util.Log;

    import androidx.annotation.NonNull;

    import com.example.app6hu.R;
    import com.example.app6hu.model.DanhMuc;
    import com.example.app6hu.model.Goal;
    import com.example.app6hu.model.Transaction;
    import com.example.app6hu.utils.App;
    import com.example.app6hu.utils.Constants;
    import com.google.android.gms.tasks.OnCompleteListener;
    import com.google.android.gms.tasks.Task;
    import com.google.firebase.Timestamp;
    import com.google.firebase.firestore.CollectionReference;
    import com.google.firebase.firestore.DocumentReference;
    import com.google.firebase.firestore.DocumentSnapshot;
    import com.google.firebase.firestore.FirebaseFirestore;
    import com.google.firebase.firestore.QueryDocumentSnapshot;
    import com.google.firebase.firestore.QuerySnapshot;

    import java.util.ArrayList;
    import java.util.HashMap;
    import java.util.List;
    import java.util.Map;

    public class FirebasestoreManager {
        private static final String TAG = "FirestoreManager";
        private final FirebaseFirestore db = FirebaseFirestore.getInstance();

        // ========== HEALTH CHECK ==========

        /** Kiểm tra kết nối Firestore */
        public void checkFirestoreConnection(FirestoreCallback<Boolean> callback) {
            db.collection("_health_check")
                    .document("ping")
                    .set(java.util.Collections.singletonMap("timestamp", System.currentTimeMillis()))
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Firestore connection OK");
                        callback.onSuccess(true);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Firestore connection FAILED: " + e.getMessage());
                        callback.onFailure(e);
                    });
        }

        // ========== TRANSACTIONS METHODS ==========

        /** Lưu giao dịch mới vào Firestore */
        public void addTransaction(Transaction transaction) {
            db.collection(Constants.COLLECTION_TRANSACTIONS)
                    .add(transaction)
                    .addOnSuccessListener(documentReference ->
                            Log.d(TAG, "Giao dịch đã thêm: " + documentReference.getId()))
                    .addOnFailureListener(e ->
                            Log.e(TAG, "Lỗi khi thêm giao dịch", e));
        }

        /** Lấy toàn bộ giao dịch */
        public void getAllTransactions(final FirestoreCallback<List<Transaction>> callback) {
            db.collection(Constants.COLLECTION_TRANSACTIONS)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            List<Transaction> list = new ArrayList<>();
                            for (QueryDocumentSnapshot doc : task.getResult()) {
                                Transaction t = doc.toObject(Transaction.class);

                                // Convert Firestore Timestamp to Date nếu cần
                                if (t.getDate() == null) {
                                    Object dateObj = doc.get(Constants.FIELD_DATE);
                                    if (dateObj instanceof Timestamp) {
                                        t.setDate(((Timestamp) dateObj).toDate());
                                    } else if (dateObj instanceof com.google.firebase.Timestamp) {
                                        t.setDate(((com.google.firebase.Timestamp) dateObj).toDate());
                                    }
                                }

                                list.add(t);
                            }
                            callback.onSuccess(list);
                        } else {
                            callback.onFailure(task.getException());
                        }
                    });
        }

        /** Xóa giao dịch theo ID */
        public void deleteTransaction(String transactionId) {
            db.collection(Constants.COLLECTION_TRANSACTIONS).document(transactionId)
                    .delete()
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Xóa giao dịch thành công"))
                    .addOnFailureListener(e -> Log.e(TAG, "Lỗi khi xóa giao dịch", e));
        }

        // ========== GOALS METHODS ==========

        /** Lưu mục tiêu mới vào Firestore */
        public void addGoal(Goal goal, FirestoreCallback<String> callback) {
            Map<String, Object> goalMap = new HashMap<>();
            goalMap.put(Constants.FIELD_GOAL_NAME, goal.getName());
            goalMap.put(Constants.FIELD_GOAL_TARGET, goal.getTargetAmount());
            goalMap.put(Constants.FIELD_GOAL_SAVED, goal.getSavedAmount());
            goalMap.put(Constants.FIELD_GOAL_DEADLINE, goal.getDeadline());
            goalMap.put(Constants.FIELD_GOAL_STATUS, goal.getStatus() != null ? goal.getStatus() : "active");
            goalMap.put("note", goal.getNote());
            goalMap.put("createdAt", goal.getCreatedAt());
            goalMap.put("color", goal.getColor());

            db.collection(Constants.COLLECTION_GOALS)
                    .add(goalMap)
                    .addOnSuccessListener(documentReference -> {
                        Log.d(TAG, "Mục tiêu đã thêm: " + documentReference.getId());
                        if (callback != null) callback.onSuccess(documentReference.getId());
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Lỗi khi thêm mục tiêu", e);
                        if (callback != null) callback.onFailure(e);
                    });
        }

        /** Lấy toàn bộ mục tiêu */
        public void getAllGoals(FirestoreCallback<List<Goal>> callback) {
            db.collection(Constants.COLLECTION_GOALS)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            List<Goal> list = new ArrayList<>();
                            for (QueryDocumentSnapshot doc : task.getResult()) {
                                Goal goal = doc.toObject(Goal.class);
                                goal.setId(doc.getId()); // Lưu document ID
                                list.add(goal);
                            }
                            callback.onSuccess(list);
                        } else {
                            callback.onFailure(task.getException());
                        }
                    });
        }

        /** Cập nhật mục tiêu */
        public void updateGoal(String goalId, Goal goal, FirestoreCallback<Void> callback) {
            Map<String, Object> updates = new HashMap<>();
            updates.put(Constants.FIELD_GOAL_NAME, goal.getName());
            updates.put(Constants.FIELD_GOAL_TARGET, goal.getTargetAmount());
            updates.put(Constants.FIELD_GOAL_SAVED, goal.getSavedAmount());
            updates.put(Constants.FIELD_GOAL_DEADLINE, goal.getDeadline());
            updates.put(Constants.FIELD_GOAL_STATUS, goal.getStatus());
            updates.put("note", goal.getNote());
            updates.put("color", goal.getColor());

            db.collection(Constants.COLLECTION_GOALS).document(goalId)
                    .update(updates)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Cập nhật mục tiêu thành công");
                        if (callback != null) callback.onSuccess(null);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Lỗi khi cập nhật mục tiêu", e);
                        if (callback != null) callback.onFailure(e);
                    });
        }

        /** Xóa mục tiêu */
        public void deleteGoal(String goalId, FirestoreCallback<Void> callback) {
            db.collection(Constants.COLLECTION_GOALS).document(goalId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Xóa mục tiêu thành công");
                        if (callback != null) callback.onSuccess(null);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Lỗi khi xóa mục tiêu", e);
                        if (callback != null) callback.onFailure(e);
                    });
        }

        /** Lấy giao dịch theo tháng và năm */
        public void getTransactionsByMonth(int month, int year, FirestoreCallback<List<Transaction>> callback) {
            db.collection(Constants.COLLECTION_TRANSACTIONS)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            List<Transaction> list = new ArrayList<>();
                            java.util.Calendar cal = java.util.Calendar.getInstance();
                            for (QueryDocumentSnapshot doc : task.getResult()) {
                                Transaction t = doc.toObject(Transaction.class);

                                // Convert Firestore Timestamp to Date nếu cần
                                if (t.getDate() == null) {
                                    Object dateObj = doc.get(Constants.FIELD_DATE);
                                    if (dateObj instanceof Timestamp) {
                                        t.setDate(((Timestamp) dateObj).toDate());
                                    } else if (dateObj instanceof com.google.firebase.Timestamp) {
                                        t.setDate(((com.google.firebase.Timestamp) dateObj).toDate());
                                    }
                                }

                                if (t.getDate() != null) {
                                    cal.setTime(t.getDate());
                                    int transMonth = cal.get(java.util.Calendar.MONTH) + 1;
                                    int transYear = cal.get(java.util.Calendar.YEAR);
                                    if (transMonth == month && transYear == year) {
                                        list.add(t);
                                    }
                                }
                            }
                            callback.onSuccess(list);
                        } else {
                            callback.onFailure(task.getException());
                        }
                    });
        }

        // ========== DANH MỤC METHODS ==========

        /** Thêm danh mục */
        public void addDanhMuc(DanhMuc danhMuc, FirestoreCallback<String> callback) {
            Map<String, Object> map = new HashMap<>();
            map.put("itemName", danhMuc.getItemName());
            map.put("icon", danhMuc.getIconName() != null ? danhMuc.getIconName() : "ic_logo");
            map.put("type", danhMuc.getType());
            map.put("viewType", danhMuc.getViewType());
            map.put("color", danhMuc.getColor());

            db.collection("DanhMuc")
                    .add(map)
                    .addOnSuccessListener(doc -> {
                        if (callback != null) callback.onSuccess(doc.getId());
                    })
                    .addOnFailureListener(e -> {
                        if (callback != null) callback.onFailure(e);
                    });
        }

        /** Lấy danh mục theo type */
        /** Lấy danh mục theo type */
    //    public void getDanhMucByType(String type, Context context, FirestoreCallback<List<DanhMuc>> callback) {
    //        db.collection("DanhMuc")
    //                .whereEqualTo("type", type)
    //                .get()
    //                .addOnCompleteListener(task -> {
    //                    if (task.isSuccessful()) {
    //                        List<DanhMuc> list = new ArrayList<>();
    //
    //                        for (QueryDocumentSnapshot doc : task.getResult()) {
    //                            try {
    //                                String name = doc.getString("itemName");
    //                                String iconName = doc.getString("icon");
    //                                String docType = doc.getString("type");
    //
    //                                Long colorLong = doc.getLong("color");
    //                                int color = colorLong != null ? colorLong.intValue() : 0;
    //
    //                                // Chuyển iconName thành resource ID - SỬ DỤNG CONTEXT TRUYỀN VÀO
    //                                int iconRes = R.drawable.ic_logo; // Mặc định
    //                                if (iconName != null && !iconName.isEmpty() && context != null) {
    //                                    try {
    //                                        // Lấy resource ID từ tên
    //                                        int resId = context.getResources()
    //                                                .getIdentifier(iconName, "drawable",
    //                                                        context.getPackageName());
    //                                        if (resId != 0) {
    //                                            iconRes = resId;
    //                                        } else {
    //                                            Log.w(TAG, "Icon not found: " + iconName +
    //                                                    ", using default");
    //                                        }
    //                                    } catch (Exception e) {
    //                                        Log.w(TAG, "Error getting icon: " + iconName);
    //                                    }
    //                                }
    //
    //                                // Tạo DanhMuc
    //                                DanhMuc danhMuc = new DanhMuc(name, iconRes, DanhMuc.TYPE_CATEGORY, color);
    //                                danhMuc.setType(docType);
    //                                danhMuc.setId(doc.getId());
    //                                danhMuc.setIconName(iconName);
    //
    //                                list.add(danhMuc);
    //                            } catch (Exception e) {
    //                                Log.e(TAG, "Error parsing document: " + e.getMessage());
    //                            }
    //                        }
    //
    //                        callback.onSuccess(list);
    //                    } else {
    //                        callback.onFailure(task.getException());
    //                    }
    //                });
    //    }
        public void getDanhMucByType(String type, Context context, FirestoreCallback<List<DanhMuc>> callback) {
            Log.d("FirebasestoreManager", "=== getDanhMucByType START ===");
            Log.d("FirebasestoreManager", "getDanhMucByType called với type: " + type);

            // SỬA TỪ "danhMuc" THÀNH "DanhMuc" (chữ D hoa)
            db.collection("DanhMuc")  // <- SỬA DÒNG NÀY
                    .whereEqualTo("type", type)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            List<DanhMuc> danhMucList = new ArrayList<>();
                            QuerySnapshot snapshot = task.getResult();

                            Log.d("FirebasestoreManager", "Tìm thấy " + snapshot.size() + " documents");

                            for (QueryDocumentSnapshot document : snapshot) {
                                Log.d("FirebasestoreManager", "Document ID: " + document.getId());
                                Log.d("FirebasestoreManager", "Data: " + document.getData());

                                // THÊM: Parse thủ công vì Firestore có field "icon" chứ không phải "iconName"
                                DanhMuc danhMuc = new DanhMuc();
                                danhMuc.setItemName(document.getString("itemName"));
                                danhMuc.setType(document.getString("type"));

                                // Lấy tên icon từ field "icon" (trong Firestore)
                                String iconName = document.getString("icon");
                                danhMuc.setIconName(iconName);  // Lưu tên icon

                                // Chuyển iconName thành resource ID
                                if (iconName != null && context != null) {
                                    try {
                                        int resId = context.getResources().getIdentifier(
                                                iconName,
                                                "drawable",
                                                context.getPackageName()
                                        );
                                        if (resId != 0) {
                                            danhMuc.setResourcesID(resId);
                                            Log.d("FirebasestoreManager", "Icon " + iconName + " -> res ID: " + resId);
                                        } else {
                                            Log.w("FirebasestoreManager", "Icon not found: " + iconName);
                                            danhMuc.setResourcesID(R.drawable.ic_logo);
                                        }
                                    } catch (Exception e) {
                                        Log.e("FirebasestoreManager", "Error converting icon", e);
                                        danhMuc.setResourcesID(R.drawable.ic_logo);
                                    }
                                } else {
                                    danhMuc.setResourcesID(R.drawable.ic_logo);
                                }

                                // Lấy các field khác
                                Long colorLong = document.getLong("color");
                                if (colorLong != null) {
                                    danhMuc.setColor(colorLong.intValue());
                                }

                                Long viewTypeLong = document.getLong("viewType");
                                if (viewTypeLong != null) {
                                    danhMuc.setViewType(viewTypeLong.intValue());
                                } else {
                                    danhMuc.setViewType(DanhMuc.TYPE_CATEGORY);  // Mặc định
                                }

                                danhMuc.setId(document.getId());
                                danhMucList.add(danhMuc);
                            }

                            Log.d("FirebasestoreManager", "Trả về " + danhMucList.size() + " items");
                            callback.onSuccess(danhMucList);
                        } else {
                            Log.e("FirebasestoreManager", "Lỗi get documents: ", task.getException());
                            callback.onFailure(task.getException());
                        }
                    });
        }
        public interface OnCategoriesLoadedListener {
            void onCategoriesLoaded(List<DanhMuc> categories);
            void onError(String error);
        }

        public interface OnCategoryDeletedListener {
            void onCategoryDeleted();
            void onError(String error);
        }
        /** Lấy tất cả danh mục (không phân type) */
        public void getAllDanhMuc(Context context,FirestoreCallback<List<DanhMuc>> callback) {
            Log.d(TAG, "getAllDanhMuc called với Context");
            db.collection("DanhMuc")
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            List<DanhMuc> list = new ArrayList<>();

                            for (QueryDocumentSnapshot doc : task.getResult()) {
                                try {
                                    String name = doc.getString("itemName");
                                    String iconName = doc.getString("icon");
                                    String docType = doc.getString("type");

                                    Long colorLong = doc.getLong("color");
                                    int color = colorLong != null ? colorLong.intValue() : 0;

                                    // Chuyển iconName thành resource ID
                                    int iconRes = R.drawable.ic_logo;
                                    if (iconName != null && !iconName.isEmpty()) {
                                        try {
                                            int resId = context.getResources()
                                                    .getIdentifier(iconName, "drawable", context.getPackageName());
                                            if (resId != 0) {
                                                iconRes = resId;
                                            }
                                        } catch (Exception e) {
                                            Log.w(TAG, "Icon not found: " + iconName);
                                        }
                                    }

                                    DanhMuc danhMuc = new DanhMuc(name, iconRes, DanhMuc.TYPE_CATEGORY, color);
                                    danhMuc.setType(docType);
                                    danhMuc.setId(doc.getId());
                                    danhMuc.setIconName(iconName);

                                    list.add(danhMuc);
                                } catch (Exception e) {
                                    Log.e(TAG, "Error parsing document: " + e.getMessage());
                                }
                            }

                            callback.onSuccess(list);
                        } else {
                            callback.onFailure(task.getException());
                        }
                    });
        }

        /** Xóa danh mục */
        public void deleteCategory(String categoryId, final OnCategoryDeletedListener listener) {
            if (categoryId == null || categoryId.isEmpty()) {
                if (listener != null) {
                    listener.onError("Category ID is null or empty");
                }
                return;
            }

            db.collection("DanhMuc").document(categoryId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Danh mục đã xóa thành công: " + categoryId);
                        if (listener != null) {
                            listener.onCategoryDeleted();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Lỗi xóa danh mục: " + e.getMessage());
                        if (listener != null) {
                            listener.onError(e.getMessage());
                        }
                    });
        }

        /** Cập nhật danh mục */
        public void updateDanhMuc(String id, DanhMuc danhMuc, FirestoreCallback<Void> callback) {
            Map<String, Object> updates = new HashMap<>();
            updates.put("itemName", danhMuc.getItemName());
            updates.put("icon", danhMuc.getIconName() != null ? danhMuc.getIconName() : "ic_logo");
            updates.put("type", danhMuc.getType());
            updates.put("color", danhMuc.getColor());

            db.collection("DanhMuc").document(id)
                    .update(updates)
                    .addOnSuccessListener(aVoid -> {
                        if (callback != null) callback.onSuccess(null);
                    })
                    .addOnFailureListener(e -> {
                        if (callback != null) callback.onFailure(e);
                    });
        }

        /** Tạo danh mục mẫu - PHIÊN BẢN MỚI VỚI KIỂM TRA KẾT NỐI */
        /** Tạo danh mục mẫu - PHIÊN BẢN MỚI VỚI KIỂM TRA KẾT NỐI */
        public void initDefaultDanhMuc(FirestoreCallback<Boolean> callback) {
            checkFirestoreConnection(new FirestoreCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean connected) {
                    if (connected) {
                        createDefaultCategories(callback);
                    } else {
                        if (callback != null) callback.onFailure(new Exception("No Firestore connection"));
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    if (callback != null) callback.onFailure(e);
                }
            });
        }

        /** Tạo danh mục mặc định (private helper) */
        private void createDefaultCategories(final FirestoreCallback<Boolean> callback) {
            String[][] expense = {
                    {"Ăn uống", "ic_food"},
                    {"Giải trí", "ic_entertain"},
                    {"Đi lại", "ic_transport"},
                    {"Quần áo", "ic_clothes"},
                    {"Mỹ phẩm", "ic_cosmetic"},
                    {"Y tế", "ic_health"},
                    {"Giáo dục", "ic_education"},
                    {"Quà", "ic_gift"},
                    {"Hóa đơn", "ic_bill"},
                    {"Tiền điện", "ic_electric"}
            };

            String[][] income = {
                    {"Lương", "ic_salary"},
                    {"Thưởng", "ic_bonus"},
                    {"Phụ cấp", "ic_allowance"},
                    {"Đầu tư", "ic_invest"}
            };

            // Kiểm tra xem đã có danh mục chưa
            db.collection("DanhMuc").limit(1).get()
                    .addOnSuccessListener(snap -> {
                        if (snap.isEmpty()) {
                            Log.d(TAG, "Tạo danh mục mặc định...");

                            final int totalCategories = expense.length + income.length;
                            final int[] categoriesCreated = {0};

                            // Thêm danh mục chi tiêu
                            for (String[] item : expense) {
                                Map<String, Object> map = new HashMap<>();
                                map.put("itemName", item[0]);
                                map.put("icon", item[1]);
                                map.put("type", "expense");
                                map.put("color", 0);
                                map.put("viewType", DanhMuc.TYPE_CATEGORY);

                                db.collection("DanhMuc").add(map)
                                        .addOnSuccessListener(docRef -> {
                                            categoriesCreated[0]++;
                                            Log.d(TAG, "Đã thêm danh mục: " + item[0]);

                                            // Kiểm tra xem đã tạo xong tất cả chưa
                                            if (categoriesCreated[0] == totalCategories && callback != null) {
                                                Log.d(TAG, "Đã tạo xong tất cả danh mục mặc định");
                                                callback.onSuccess(true);
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            categoriesCreated[0]++;
                                            Log.e(TAG, "Lỗi thêm danh mục " + item[0] + ": " + e.getMessage());

                                            // Vẫn tiếp tục dù có lỗi
                                            if (categoriesCreated[0] == totalCategories && callback != null) {
                                                callback.onSuccess(true);
                                            }
                                        });
                            }

                            // Thêm danh mục thu nhập
                            for (String[] item : income) {
                                Map<String, Object> map = new HashMap<>();
                                map.put("itemName", item[0]);
                                map.put("icon", item[1]);
                                map.put("type", "income");
                                map.put("color", 0);
                                map.put("viewType", DanhMuc.TYPE_CATEGORY);

                                db.collection("DanhMuc").add(map)
                                        .addOnSuccessListener(docRef -> {
                                            categoriesCreated[0]++;
                                            Log.d(TAG, "Đã thêm danh mục: " + item[0]);

                                            if (categoriesCreated[0] == totalCategories && callback != null) {
                                                Log.d(TAG, "Đã tạo xong tất cả danh mục mặc định");
                                                callback.onSuccess(true);
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            categoriesCreated[0]++;
                                            Log.e(TAG, "Lỗi thêm danh mục " + item[0] + ": " + e.getMessage());

                                            if (categoriesCreated[0] == totalCategories && callback != null) {
                                                callback.onSuccess(true);
                                            }
                                        });
                            }

                            // Nếu không có danh mục nào được tạo (tổng = 0)
                            if (totalCategories == 0 && callback != null) {
                                callback.onSuccess(true);
                            }

                        } else {
                            Log.d(TAG, "Danh mục đã tồn tại, bỏ qua tạo mặc định");
                            if (callback != null) callback.onSuccess(true);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Lỗi kiểm tra danh mục: " + e.getMessage());
                        if (callback != null) callback.onFailure(e);
                    });
        }
        /** Interface callback */
        public interface FirestoreCallback<T> {
            void onSuccess(T data);
            void onFailure(Exception e);
        }
    }