package com.example.app6hu.firebase;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.app6hu.model.DanhMuc;
import com.example.app6hu.model.Goal;
import com.example.app6hu.model.Transaction;
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
public class FirebasestoreManager {private static final String TAG = "FirestoreManager";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

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

    // ========== CATEGORIES METHODS ==========

    /** Lấy danh sách danh mục từ Firebase */
    public void getCategories(OnCategoriesLoadedListener listener) {
        db.collection("danh_muc")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<DanhMuc> categories = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            String name = doc.getString("name");
                            String iconName = doc.getString("icon");
                            
                            // Lấy resource ID của icon (mặc định là ic_logo nếu không tìm thấy)
                            int iconResId = com.example.app6hu.R.drawable.ic_logo;
                            
                            DanhMuc category = new DanhMuc(name, iconResId, DanhMuc.TYPE_CATEGORY);
                            category.setId(doc.getId());
                            categories.add(category);
                        }
                        listener.onCategoriesLoaded(categories);
                    } else {
                        listener.onError(task.getException() != null ? 
                            task.getException().getMessage() : "Lỗi không xác định");
                    }
                });
    }

    /** Thêm danh mục mới vào Firebase */
    public void addCategory(String categoryName, String iconName, OnCategoryAddedListener listener) {
        Map<String, Object> category = new HashMap<>();
        category.put("name", categoryName);
        category.put("icon", iconName);
        category.put("createdAt", com.google.firebase.Timestamp.now());

        db.collection("danh_muc")
                .add(category)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Danh mục đã thêm: " + documentReference.getId());
                    if (listener != null) {
                        listener.onCategoryAdded(documentReference.getId());
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi khi thêm danh mục", e);
                    if (listener != null) {
                        listener.onError(e.getMessage());
                    }
                });
    }

    /** Xóa danh mục từ Firebase */
    public void deleteCategory(String categoryId, OnCategoryDeletedListener listener) {
        db.collection("danh_muc").document(categoryId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Xóa danh mục thành công");
                    if (listener != null) {
                        listener.onCategoryDeleted();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi khi xóa danh mục", e);
                    if (listener != null) {
                        listener.onError(e.getMessage());
                    }
                });
    }

    /** Interface callback cho danh mục */
    public interface OnCategoriesLoadedListener {
        void onCategoriesLoaded(List<DanhMuc> categories);
        void onError(String error);
    }

    public interface OnCategoryAddedListener {
        void onCategoryAdded(String categoryId);
        void onError(String error);
    }

    public interface OnCategoryDeletedListener {
        void onCategoryDeleted();
        void onError(String error);
    }

    /** Interface callback */
    public interface FirestoreCallback<T> {
        void onSuccess(T data);
        void onFailure(Exception e);
    }
}