package com.kapstranspvtltd.kaps.activities.models;

public class SliderModel {
    private String imageUrl;
    private String title;
    private String description;

    public SliderModel(String imageUrl, String title, String description) {
        this.imageUrl = imageUrl;
        this.title = title;
        this.description = description;
    }

    // Getters
    public String getImageUrl() { return imageUrl; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
}