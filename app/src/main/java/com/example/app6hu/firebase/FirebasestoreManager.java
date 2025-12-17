package com.example.app6hu.firebase;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.app6hu.MainActivity;
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
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
    import com.google.firebase.firestore.QueryDocumentSnapshot;
    import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
    import java.util.Calendar;
    import java.util.Date;
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

                                // LƯU DOCUMENT ID
                                t.setDocumentId(doc.getId());

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
                            Calendar cal = Calendar.getInstance();

                            for (QueryDocumentSnapshot doc : task.getResult()) {
                                Transaction t = new Transaction(); // Tạo mới thay vì toObject()

                                // LẤY TẤT CẢ FIELD THỦ CÔNG
                                t.setDocumentId(doc.getId()); // QUAN TRỌNG: Document ID từ Firestore

                                // Lấy các field khác
                                if (doc.contains("id")) {
                                    Object idObj = doc.get("id");
                                    if (idObj instanceof Long) {
                                        t.setId((Long) idObj);
                                    } else if (idObj instanceof Integer) {
                                        t.setId(((Integer) idObj).longValue());
                                    } else if (idObj instanceof Double) {
                                        t.setId(((Double) idObj).longValue());
                                    }
                                }

                                t.setType(doc.getString(Constants.FIELD_TYPE));
                                t.setAmount(doc.getDouble(Constants.FIELD_AMOUNT));
                                t.setCategory(doc.getString(Constants.FIELD_CATEGORY));
                                t.setDetail(doc.getString(Constants.FIELD_DETAIL));

                                // Xử lý date
                                Object dateObj = doc.get(Constants.FIELD_DATE);
                                if (dateObj instanceof Timestamp) {
                                    t.setDate(((Timestamp) dateObj).toDate());
                                } else if (dateObj instanceof Date) {
                                    t.setDate((Date) dateObj);
                                } else {
                                    t.setDate(new Date());
                                }

                                // Kiểm tra và thêm vào list nếu đúng tháng/năm
                                cal.setTime(t.getDate());
                                int transMonth = cal.get(Calendar.MONTH) + 1;
                                int transYear = cal.get(Calendar.YEAR);

                                Log.d(TAG, "Loaded Transaction: " +
                                        "DocID=" + t.getDocumentId() +
                                        ", ID=" + t.getId() +
                                        ", Month=" + transMonth +
                                        ", Year=" + transYear);

                                if (transMonth == month && transYear == year) {
                                    list.add(t);
                                }
                            }
                            callback.onSuccess(list);
                        } else {
                            callback.onFailure(task.getException());
                        }
                    });
        }

        //Tìm trans bằng id
        public void findTransactionByField(String fieldName, Object value, FirestoreCallback<List<Transaction>> callback) {
            db.collection(Constants.COLLECTION_TRANSACTIONS)
                    .whereEqualTo(fieldName, value)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            List<Transaction> list = new ArrayList<>();
                            for (QueryDocumentSnapshot doc : task.getResult()) {
                                Transaction t = new Transaction();
                                t.setDocumentId(doc.getId());

                                if (doc.contains("id")) {
                                    Object idObj = doc.get("id");
                                    if (idObj instanceof Long) {
                                        t.setId((Long) idObj);
                                    }
                                }

                                t.setType(doc.getString(Constants.FIELD_TYPE));
                                t.setAmount(doc.getDouble(Constants.FIELD_AMOUNT));
                                t.setCategory(doc.getString(Constants.FIELD_CATEGORY));
                                t.setDetail(doc.getString(Constants.FIELD_DETAIL));

                                Object dateObj = doc.get(Constants.FIELD_DATE);
                                if (dateObj instanceof Timestamp) {
                                    t.setDate(((Timestamp) dateObj).toDate());
                                } else if (dateObj instanceof Date) {
                                    t.setDate((Date) dateObj);
                                }

                                list.add(t);
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

        //Cập nhật giao dịch từ FB
        /** Cập nhật giao dịch */
        public void updateTransaction(String transactionId, Transaction updatedTransaction, FirestoreCallback<Void> callback) {
            if (transactionId == null || transactionId.isEmpty()) {
                if (callback != null) callback.onFailure(new Exception("Transaction ID is null"));
                return;
            }

            Map<String, Object> updates = new HashMap<>();
            updates.put(Constants.FIELD_TYPE, updatedTransaction.getType());
            updates.put(Constants.FIELD_AMOUNT, updatedTransaction.getAmount());
            updates.put(Constants.FIELD_CATEGORY, updatedTransaction.getCategory());
            updates.put(Constants.FIELD_DETAIL, updatedTransaction.getDetail());
            updates.put(Constants.FIELD_DATE, updatedTransaction.getDate());

            db.collection(Constants.COLLECTION_TRANSACTIONS).document(transactionId)
                    .update(updates)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Cập nhật giao dịch thành công: " + transactionId);
                        if (callback != null) callback.onSuccess(null);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Lỗi khi cập nhật giao dịch", e);
                        if (callback != null) callback.onFailure(e);
                    });
        }
        /** Interface callback */
        public interface FirestoreCallback<T> {
            void onSuccess(T data);
            void onFailure(Exception e);
        }

        // Interface cho callback
        public interface OnCategoryUpdatedListener {
            void onCategoryUpdated();
            void onError(String error);
        }

        public interface OnCategoryAddedListener {
            void onCategoryAdded();
            void onError(String error);
        }

        public interface OnCategoryDeletedListener {
            void onCategoryDeleted();
            void onError(String error);
        }


        // ========== DANH MỤC METHODS ==========

        /** Lấy tất cả danh mục */
        public void getAllDanhMuc(Context context, FirestoreCallback<List<DanhMuc>> callback) {
            Log.d(TAG, "getAllDanhMuc called với Context");
            db.collection("DanhMuc")
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            List<DanhMuc> list = new ArrayList<>();

                            for (QueryDocumentSnapshot doc : task.getResult()) {
                                try {
                                    DanhMuc danhMuc = parseDanhMucFromDocument(doc, context);
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

        /** Parse DanhMuc từ Firestore document */
        private DanhMuc parseDanhMucFromDocument(DocumentSnapshot doc, Context context) {
            String name = doc.getString("itemName");
            String iconName = doc.getString("icon"); // Lưu ý: trong Firestore field là "icon", không phải "iconName"
            String type = doc.getString("type");
            String id = doc.getId();

            // Lấy màu sắc
            Long colorLong = doc.getLong("color");
            int color = colorLong != null ? colorLong.intValue() : 0;

            // Lấy viewType
            Long viewTypeLong = doc.getLong("viewType");
            int viewType = viewTypeLong != null ? viewTypeLong.intValue() : DanhMuc.TYPE_CATEGORY;

            // Lấy resource ID từ icon name
            int iconRes = R.drawable.ic_logo; // Mặc định
            if (iconName != null && !iconName.isEmpty() && context != null) {
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

            // Tạo đối tượng DanhMuc
            DanhMuc danhMuc = new DanhMuc(name, iconRes, viewType, color);
            danhMuc.setType(type);
            danhMuc.setId(id);
            danhMuc.setIconName(iconName); // Lưu tên icon

            return danhMuc;
        }

        /** Lấy danh mục theo type */
        public void getDanhMucByType(String type, Context context, FirestoreCallback<List<DanhMuc>> callback) {
            Log.d(TAG, "getDanhMucByType called với type: " + type);

            db.collection("DanhMuc")
                    .whereEqualTo("type", type)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            List<DanhMuc> danhMucList = new ArrayList<>();

                            for (QueryDocumentSnapshot doc : task.getResult()) {
                                try {
                                    DanhMuc danhMuc = parseDanhMucFromDocument(doc, context);
                                    danhMucList.add(danhMuc);
                                } catch (Exception e) {
                                    Log.e(TAG, "Error parsing document: " + e.getMessage());
                                }
                            }

                            Log.d(TAG, "Trả về " + danhMucList.size() + " danh mục");
                            callback.onSuccess(danhMucList);
                        } else {
                            Log.e(TAG, "Lỗi get documents: ", task.getException());
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
        public void updateCategory(String categoryId, String newName, String iconName, String colorHex, String type,
                                   OnCategoryUpdatedListener listener) {
            if (categoryId == null || categoryId.isEmpty()) {
                listener.onError("ID danh mục không hợp lệ");
                return;
            }

            // Chuyển đổi hex color sang int
            int colorInt;
            try {
                colorInt = Color.parseColor(colorHex);
            } catch (Exception e) {
                colorInt = Color.BLACK; // Màu đen mặc định
            }

            Map<String, Object> updates = new HashMap<>();
            updates.put("itemName", newName);
            updates.put("icon", iconName); // Lưu ý: field trong Firestore là "icon"
            updates.put("type", type);
            updates.put("color", colorInt);
            updates.put("viewType", DanhMuc.TYPE_CATEGORY);

            db.collection("DanhMuc").document(categoryId)
                    .update(updates)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Danh mục đã cập nhật thành công: " + categoryId);
                        listener.onCategoryUpdated();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Lỗi cập nhật danh mục: " + e.getMessage());
                        listener.onError(e.getMessage());
                    });
        }

        /** Thêm danh mục mới */
        public void addCategory(String name, String iconName, String colorHex, String type,
                                OnCategoryAddedListener listener) {
            // Chuyển đổi hex color sang int
            int colorInt;
            try {
                colorInt = Color.parseColor(colorHex);
            } catch (Exception e) {
                colorInt = Color.BLACK; // Màu đen mặc định
            }

            Map<String, Object> categoryData = new HashMap<>();
            categoryData.put("itemName", name);
            categoryData.put("icon", iconName); // Lưu ý: field trong Firestore là "icon"
            categoryData.put("type", type);
            categoryData.put("color", colorInt);
            categoryData.put("viewType", DanhMuc.TYPE_CATEGORY);

            // Tạo document mới với ID tự động
            db.collection("DanhMuc")
                    .add(categoryData)
                    .addOnSuccessListener(documentReference -> {
                        Log.d(TAG, "Danh mục đã thêm thành công với ID: " + documentReference.getId());
                        listener.onCategoryAdded();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Lỗi thêm danh mục: " + e.getMessage());
                        listener.onError(e.getMessage());
                    });
        }




        /** Lấy tất cả icons có sẵn */
        public void getAllIcons(Context context, FirestoreCallback<List<String>> callback) {
            List<String> icons = new ArrayList<>();

            // Danh sách icons mặc định
            icons.add("ic_logo");
            icons.add("ic_food");
            icons.add("ic_shopping");
            icons.add("ic_transport");
            icons.add("ic_entertain");
            icons.add("ic_health");
            icons.add("ic_education");
            icons.add("ic_bill");
            icons.add("ic_salary");
            icons.add("ic_gift");
            icons.add("ic_other");

            icons.add("ic_allowance");
            icons.add("ic_arrow_left");
            icons.add("ic_arrow-right");
            icons.add("ic_beaty");
            icons.add("ic_beer");
            icons.add("ic_bike");
            icons.add("ic_bonus");
            icons.add("ic_bus");
            icons.add("ic_calendar");
            icons.add("ic_chart");
            icons.add("ic_check");
            icons.add("ic_clothes");
            icons.add("ic_coffee");
            icons.add("ic_commission");
            icons.add("ic_cosmetic");
            icons.add("ic_default");
            icons.add("ic_delete");
            icons.add("ic_dividends");
            icons.add("ic_edit");
            icons.add("ic_electric");
            icons.add("ic_email");
            icons.add("ic_fastfood");
            icons.add("ic_freelance");
            icons.add("ic_gas");
            icons.add("ic_groceries");
            icons.add("ic_gym");
            icons.add("ic_home");
            icons.add("ic_house");
            icons.add("ic_internet");
            icons.add("ic_invest");
            icons.add("ic_launcher_background");
            icons.add("ic_launcher_foreground");
            icons.add("ic_lock");
            icons.add("ic_movie");
            icons.add("ic_music");
            icons.add("ic_notifications");
            icons.add("ic_parking");
            icons.add("ic_pen");
            icons.add("ic_person");
            icons.add("ic_pharmacy");
            icons.add("ic_plus");
            icons.add("ic_rent");
            icons.add("ic_restaurant");
            icons.add("ic_royalty");
            icons.add("ic_saving");
            icons.add("ic_school");
            icons.add("ic_selling");
            icons.add("ic_setting");
            icons.add("ic_sports");
            icons.add("ic_taxi");
            icons.add("ic_wallet");
            // Có thể thêm logic lấy từ Firestore nếu cần
            callback.onSuccess(icons);
        }

        // ========== NEW: DELETE ALL TRANSACTIONS ==========

        /**
         * Xóa TẤT CẢ giao dịch trong collection
         */
        public void deleteAllTransactions(FirestoreCallback<Void> callback) {
            db.collection(Constants.COLLECTION_TRANSACTIONS)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            int totalDocuments = task.getResult().size();
                            Log.d(TAG, "Found " + totalDocuments + " transactions to delete");

                            if (totalDocuments == 0) {
                                // Không có gì để xóa
                                Log.d(TAG, "No transactions to delete");
                                callback.onSuccess(null);
                                return;
                            }

                            // Sử dụng batch delete để xóa nhiều document cùng lúc
                            WriteBatch batch = db.batch();

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                batch.delete(document.getReference());
                            }

                            // Thực thi batch delete
                            batch.commit()
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d(TAG, "✅ Successfully deleted ALL " + totalDocuments + " transactions");
                                        callback.onSuccess(null);
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "❌ Error deleting transactions: " + e.getMessage());
                                        callback.onFailure(e);
                                    });

                        } else {
                            Log.e(TAG, "Error getting transactions: ", task.getException());
                            callback.onFailure(task.getException());
                        }
                    });
        }


    }