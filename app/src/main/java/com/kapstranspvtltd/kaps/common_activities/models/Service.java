package com.kapstranspvtltd.kaps.common_activities.models;

public class Service {
    private String subCatId;
    private String subCatName;
    private String image;
    private String pricePerHour;
    private String serviceBasePrice;

    public Service(String subCatId, String subCatName, String image,
                   String pricePerHour, String serviceBasePrice) {
        this.subCatId = subCatId;
        this.subCatName = subCatName;
        this.image = image;
        this.pricePerHour = pricePerHour;
        this.serviceBasePrice = serviceBasePrice;
    }

    // Getters
    public String getSubCatId() { return subCatId; }
    public String getSubCatName() { return subCatName; }
    public String getImage() { return image; }
    public String getPricePerHour() { return pricePerHour; }
    public String getServiceBasePrice() { return serviceBasePrice; }

    // Setters
    public void setSubCatId(String subCatId) { this.subCatId = subCatId; }
    public void setSubCatName(String subCatName) { this.subCatName = subCatName; }
    public void setImage(String image) { this.image = image; }
    public void setPricePerHour(String pricePerHour) { this.pricePerHour = pricePerHour; }
    public void setServiceBasePrice(String serviceBasePrice) { this.serviceBasePrice = serviceBasePrice; }
}