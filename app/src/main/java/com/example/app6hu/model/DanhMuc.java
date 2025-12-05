package com.example.app6hu.model;

public class DanhMuc {
    // Constants
    public static final int TYPE_CATEGORY = 0;
    public static final int TYPE_ADD = 1;

    // Fields
    private String itemName;
    private int resourcesID;
    private int viewType;
    private int color = 0;
    private String type;       // "expense" hoặc "income"
    private String id;         // Firestore document ID
    private String iconName;   // Tên icon (string) để load từ Firestore

    // Constructors
    public DanhMuc(String itemName, int resourcesID) {
        this(itemName, resourcesID, TYPE_CATEGORY, 0);
    }

    public DanhMuc(String itemName, int resourcesID, int viewType) {
        this(itemName, resourcesID, viewType, 0);
    }
    public DanhMuc() {
        // Constructor rỗng BẮT BUỘC cho Firestore mapping
    }
    public DanhMuc(String itemName, int resourcesID, int viewType, int color) {
        this.itemName = itemName;
        this.resourcesID = resourcesID;
        this.viewType = viewType;
        this.color = color;
    }

    // Getters và Setters
    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public int getResourcesID() {
        return resourcesID;
    }

    public void setResourcesID(int resourcesID) {
        this.resourcesID = resourcesID;
    }

    public int getViewType() {
        return viewType;
    }

    public void setViewType(int viewType) {
        this.viewType = viewType;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIconName() {
        return iconName;
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
    }

    // Helper method để check xem có phải item Add không
    public boolean isAddItem() {
        return viewType == TYPE_ADD;
    }
}