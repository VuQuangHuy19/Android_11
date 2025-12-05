package com.example.app6hu.model;

public class DanhMuc {

    public static final int TYPE_CATEGORY = 0;
    public static final int TYPE_ADD = 1;

    private String id;
    private String itemName;
    private int resourcesID;
    private int viewType;
    private int color = 0; // mặc định không tô màu

    public DanhMuc(String itemName, int resourcesID) {
        this.itemName = itemName;
        this.resourcesID = resourcesID;
        this.viewType = TYPE_CATEGORY;
    }

    public DanhMuc(String itemName, int resourcesID, int viewType) {
        this.itemName = itemName;
        this.resourcesID = resourcesID;
        this.viewType = viewType;
    }

    public DanhMuc(String itemName, int resourcesID, int viewType, int color) {
        this.itemName = itemName;
        this.resourcesID = resourcesID;
        this.viewType = viewType;
        this.color = color;
    }

    public String getItemName() {
        return itemName;
    }

    public int getResourcesID() {
        return resourcesID;
    }

    public int getViewType() {
        return viewType;
    }

    public int getColor() {
        return color;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
