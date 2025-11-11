package com.example.app6hu.utils;

public class Constants {
    // Firestore collections
    public static final String COLLECTION_TRANSACTIONS = "transactions";
    public static final String COLLECTION_GOALS = "goals";
    public static final String COLLECTION_TUITION = "tuition";
    public static final String COLLECTION_STATISTICS = "statistics";
    public static final String COLLECTION_CATEGORIES = "categories";

    // Transaction fields
    public static final String FIELD_TYPE = "type";
    public static final String FIELD_AMOUNT = "amount";
    public static final String FIELD_CATEGORY = "category";
    public static final String FIELD_DETAIL = "detail";
    public static final String FIELD_DATE = "date";
    public static final String FIELD_ICON = "icon";

    // Goal fields
    public static final String FIELD_GOAL_NAME = "name";
    public static final String FIELD_GOAL_TARGET = "targetAmount";
    public static final String FIELD_GOAL_SAVED = "savedAmount";
    public static final String FIELD_GOAL_DEADLINE = "deadline";
    public static final String FIELD_GOAL_STATUS = "status";

    // Notifications
    public static final String CHANNEL_ID = "expense_manager_channel";
    public static final String CHANNEL_NAME = "Chi tiêu sinh viên HAUI";
    public static final int NOTIFY_ID_WARNING = 1001;
}
