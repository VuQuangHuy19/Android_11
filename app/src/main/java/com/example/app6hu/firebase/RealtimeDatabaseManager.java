package com.example.app6hu.firebase;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RealtimeDatabaseManager {

    private static FirebaseDatabase database;

    public static DatabaseReference db() {
        if (database == null) {
            database = FirebaseDatabase.getInstance();
        }
        return database.getReference(); // root database
    }
}
