package com.beladsoft.phone_background.Objects;

public class HomeObjects {
    public String itemID,itemImageUrl,itemTitle;
    public int viewType;

    public HomeObjects(String itemID, String itemTitle, int viewType) {
        this.itemID = itemID;
        this.itemTitle = itemTitle;
        this.viewType = viewType;
    }

    public HomeObjects() {
    }
}
