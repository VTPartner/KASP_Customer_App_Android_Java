package com.kapstranspvtltd.kaps.activities.models;

import org.json.JSONException;
import org.json.JSONObject;

public class GuidelineModel {
    private String title;
    private String description;
    // Add other fields as needed

    public static GuidelineModel fromJson(JSONObject json) throws JSONException {
        GuidelineModel guideline = new GuidelineModel();
        // Parse JSON and set fields
        // Adjust according to your JSON structure
//        guideline.title = json.optString("guide_line", "");
        guideline.description = json.optString("guide_line", "");
        return guideline;
    }

    // Getters
    public String getTitle() { return title; }
    public String getDescription() { return description; }
}