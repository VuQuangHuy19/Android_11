package com.example.app6hu.firebase;

import androidx.annotation.Nullable;

import com.example.app6hu.model.HocPhi;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class FirestoreHocPhiManager {

    private static final String COLLECTION_NAME = "hoc_phi";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface HocPhiListener {
        void onData(List<HocPhi> list, long tongTien);
    }

    // Lắng nghe realtime tất cả học phí
    public void listenAll(HocPhiListener listener) {
        db.collection(COLLECTION_NAME)
                .addSnapshotListener((snapshot, e) -> {

                    if (e != null || snapshot == null) {
                        return;
                    }

                    List<HocPhi> result = new ArrayList<>();
                    long tongTien = 0;

                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        HocPhi hp = doc.toObject(HocPhi.class);
                        if (hp != null) {
                            result.add(hp);

                            if (!"DA_DONG".equals(hp.getTrangThai())) {
                                tongTien += hp.getSoTien();
                            }
                        }
                    }

                    listener.onData(result, tongTien);
                });
    }


    // Lấy collection "hoc_phi"
    public CollectionReference collection(String ten) {
        return db.collection(ten);
    }

    // Thêm hoặc cập nhật học phí
    public void save(HocPhi hocPhi) {
        collection(COLLECTION_NAME).document(hocPhi.getId()).set(hocPhi);
    }

    // Xóa học phí
    public void delete(String id) {
        collection(COLLECTION_NAME).document(id).delete();
    }
}

