package com.example.app6hu.utils;

import android.content.Context;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

public class FirebaseDebugHelper {

    private static final String TAG = "FirebaseDebug";

    public static void debugFirebaseSetup(Context context) {
        try {
            // 1. Kiểm tra google-services.json
            Log.d(TAG, "=== FIREBASE DEBUG START ===");

            // 2. Kiểm tra tất cả FirebaseApp instances
            for (FirebaseApp app : FirebaseApp.getApps(context)) {
                FirebaseOptions options = app.getOptions();
                Log.d(TAG, "App Name: " + app.getName());
                Log.d(TAG, "Project ID: " + options.getProjectId());
                Log.d(TAG, "Application ID: " + options.getApplicationId());
                Log.d(TAG, "Database URL: " + options.getDatabaseUrl());
                Log.d(TAG, "API Key: " + options.getApiKey());
                Log.d(TAG, "Storage Bucket: " + options.getStorageBucket());
            }

            // 3. Kiểm tra Firestore settings
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            FirebaseFirestoreSettings settings = db.getFirestoreSettings();
            Log.d(TAG, "Firestore Settings:");
            Log.d(TAG, " - Host: " + (settings != null ? settings.getHost() : "null"));
            Log.d(TAG, " - SSL enabled: " + (settings != null ? settings.isSslEnabled() : "null"));
            Log.d(TAG, " - Persistence enabled: " + (settings != null ? settings.isPersistenceEnabled() : "null"));

            // 4. Test Firestore connection với timeout
            testFirestoreConnectionWithTimeout(db);

        } catch (Exception e) {
            Log.e(TAG, "Debug error: " + e.getMessage(), e);
        }
    }

    private static void testFirestoreConnectionWithTimeout(FirebaseFirestore db) {
        // Test đơn giản: lấy 1 document
        db.collection("_test")
                .document("ping")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Log.d(TAG, "Firestore connection: SUCCESS");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Firestore connection: FAILED - " + e.getMessage());
                });
    }

    public static void checkGoogleServicesJson(Context context) {
        try {
            int resId = context.getResources().getIdentifier(
                    "google_services_json_raw", "raw", context.getPackageName());

            if (resId != 0) {
                Log.d(TAG, "google-services.json found in resources");
            } else {
                Log.e(TAG, "google-services.json NOT found in resources!");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking google-services.json: " + e.getMessage());
        }
    }
}