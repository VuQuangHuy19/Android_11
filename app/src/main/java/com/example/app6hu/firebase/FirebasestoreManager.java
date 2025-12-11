package com.example.app6hu.firebase;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.app6hu.model.Transaction;
import com.example.app6hu.utils.Constants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
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

    public void getAllTransactions(final FirestoreCallback<List<Transaction>> callback) {
        db.collection(Constants.COLLECTION_TRANSACTIONS)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Transaction> list = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            Transaction t = doc.toObject(Transaction.class);
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

    /** Interface callback */
    public interface FirestoreCallback<T> {
        void onSuccess(T data);
        void onFailure(Exception e);
    }

}