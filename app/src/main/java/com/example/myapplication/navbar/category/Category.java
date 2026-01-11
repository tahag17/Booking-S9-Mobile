package com.example.myapplication.navbar.category;

public class Category {

    private String displayName;
    private String technicalName;
    private int iconResId; // drawable resource for icon
    private boolean activated;

    public Category(String displayName, String technicalName, int iconResId, boolean activated) {
        this.displayName = displayName;
        this.technicalName = technicalName;
        this.iconResId = iconResId;
        this.activated = activated;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getTechnicalName() {
        return technicalName;
    }

    public int getIconResId() {
        return iconResId;
    }

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }


}
