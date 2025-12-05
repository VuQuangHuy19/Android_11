package com.example.app6hu.utils;

import android.content.Context;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

public class FirebaseTest {

    private static final String TAG = "FirebaseTest";

    public static void testFirebaseConnection(Context context) {
        try {
            Log.d(TAG, "=== FIREBASE CONNECTION TEST ===");

            // 1. Kiểm tra FirebaseApp
            for (FirebaseApp app : FirebaseApp.getApps(context)) {
                FirebaseOptions options = app.getOptions();
                Log.d(TAG, "App Name: " + app.getName());
                Log.d(TAG, "Project ID: " + options.getProjectId());
                Log.d(TAG, "Application ID: " + options.getApplicationId());
                Log.d(TAG, "API Key: " + options.getApiKey());
            }

            // 2. Kiểm tra Firestore
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            FirebaseFirestoreSettings settings = db.getFirestoreSettings();

            if (settings != null) {
                Log.d(TAG, "Firestore Host: " + settings.getHost());
                Log.d(TAG, "Firestore SSL Enabled: " + settings.isSslEnabled());
            }

            // 3. Test kết nối đơn giản
            db.collection("test").document("ping")
                    .set(java.util.Collections.singletonMap("timestamp", System.currentTimeMillis()))
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Firestore write SUCCESS!");
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Firestore write FAILED: " + e.getMessage());
                        e.printStackTrace();
                    });

        } catch (Exception e) {
            Log.e(TAG, "Firebase test error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}