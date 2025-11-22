package com.example.app6hu.model;



public class DanhMuc {
    public static final int TYPE_CATEGORY = 0;
    public static final int TYPE_ADD = 1;

    private String itemName;
    private int resourcesID;
    private int viewType;

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

    public String getItemName() {
        return itemName;
    }

    public int getResourcesID() {
        return resourcesID;
    }

    public int getViewType() {
        return viewType;
    }
}
